/* 
 * Copyright (C) 2014 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.shandian.blacklight.support.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.common.DynamicGridLayout;
import us.shandian.blacklight.ui.common.HackyHorizontalScrollView;
import us.shandian.blacklight.ui.common.ImageActivity;
import us.shandian.blacklight.ui.comments.CommentOnActivity;
import us.shandian.blacklight.ui.comments.ReplyToActivity;
import us.shandian.blacklight.ui.statuses.RepostActivity;
import us.shandian.blacklight.ui.statuses.SingleActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;
import static us.shandian.blacklight.receiver.ConnectivityReceiver.isWIFI;

/*
  This is a common adapter for all kinds of weibo data (Statuses / Comments)
  Adapting them to ListViews.
  They share one common layout
*/
public class WeiboAdapter extends BaseAdapter implements AbsListView.RecyclerListener, AbsListView.OnScrollListener, 
										View.OnClickListener, View.OnLongClickListener
{
	private static final String TAG = WeiboAdapter.class.getSimpleName();

	private static final int TAG_MSG = R.id.weibo_content;
	private static final int TAG_ID = R.id.card;

	private static final View.OnClickListener sImageListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			MessageModel msg = (MessageModel) v.getTag(TAG_MSG);
			int id = Integer.parseInt(v.getTag(TAG_ID).toString());
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), ImageActivity.class);
			i.putExtra("model", msg);
			i.putExtra("defaultId", id);
			v.getContext().startActivity(i);
		}
	};

	private static final View.OnClickListener sAvatarListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			MessageModel msg = (MessageModel) v.getTag(TAG_MSG);
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", msg.user);
			v.getContext().startActivity(i);
		}
	};
	
	private MessageListModel mList;
	private LayoutInflater mInflater;
	private StatusTimeUtils mTimeUtils;
	private UserApiCache mUserApi;
	private HomeTimeLineApiCache mHomeApi;
	private LoginApiCache mLogin;
	
	private String mUid;
	
	private int mGray;
	
	private Context mContext;

	private ArrayList<AbsListView.OnScrollListener> mListeners = new ArrayList<AbsListView.OnScrollListener>();
	
	private boolean mBindOrig;
	private boolean mShowCommentStatus;
	private boolean mScrolling = false;
	private boolean mAutoNoPic = false;
	
	public WeiboAdapter(Context context, AbsListView listView, MessageListModel list, boolean bindOrig, boolean showCommentStatus) {
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTimeUtils = StatusTimeUtils.instance(context);
		mUserApi = new UserApiCache(context);
		mHomeApi = new HomeTimeLineApiCache(context);
		mLogin = new LoginApiCache(context);
		mGray = context.getResources().getColor(R.color.light_gray);
		mUid = mLogin.getUid();
		mContext = context;
		mBindOrig = bindOrig;
		mShowCommentStatus = showCommentStatus;
		mAutoNoPic = Settings.getInstance(context).getBoolean(Settings.AUTO_NOPIC, true);
		
		listView.setRecyclerListener(this);
		listView.setOnScrollListener(this);
	}
	
	@Override
	public int getCount() {
		return mList.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mList.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			MessageModel msg = mList.get(position);
			
			return bindView(msg, convertView);
		}
	}

	@Override
	public void onMovedToScrapHeap(View v) {
		if (v.getTag() instanceof ViewHolder) {
			ViewHolder h = (ViewHolder) v.getTag();
			
			h.getAvatar().setImageBitmap(null);
			h.getAvatar().setTag(true);
			h.getAvatar().setOnClickListener(null);
			h.getCommentAndRetweet().setVisibility(View.VISIBLE);
			h.getSwipe().close(false);
			
			LinearLayout container = h.getContainer();
			
			for (int i = 0; i < 9; i++) {
				ImageView iv = (ImageView) container.getChildAt(i);
				iv.setImageBitmap(null);
				iv.setVisibility(View.VISIBLE);
				iv.setOnClickListener(null);
				iv.setTag(true);
			}
			
			h.getScroll().setVisibility(View.GONE);
			h.getOriginParent().setVisibility(View.GONE);
			
			h.msg = null;
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView v, int state) {
		mScrolling = state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

		// Inform all listeners
		for (AbsListView.OnScrollListener listener : mListeners) {
			if (listener != null) {
				listener.onScrollStateChanged(v, state);
			}
		}
	}
	
	@Override
	public void onScroll(AbsListView p1, int p2, int p3, int p4) {
		// Inform all listeners
		for (AbsListView.OnScrollListener listener : mListeners) {
			if (listener != null) {
				listener.onScroll(p1, p2, p3, p4);
			}
		}
	}

	public void addOnScrollListener(AbsListView.OnScrollListener listener) {
		mListeners.add(listener);
	}

	private void replyToComment(CommentModel comment) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(mContext, ReplyToActivity.class);
		i.putExtra("comment", comment);
		mContext.startActivity(i);
	}

	private void showMsg(MessageModel msg) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(mContext, SingleActivity.class);
		i.putExtra("msg", msg);
		mContext.startActivity(i);
	}

	private void repostMsg(MessageModel msg) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(mContext, RepostActivity.class);
		i.putExtra("msg", msg);
		mContext.startActivity(i);
	}

	private void commentOnMsg(MessageModel msg) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(mContext, CommentOnActivity.class);
		i.putExtra("msg", msg);
		mContext.startActivity(i);
	}

	private void delete(final MessageModel msg) {
		new AlertDialog.Builder(mContext)
			.setMessage(R.string.confirm_delete)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					new DeleteTask().execute(msg);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			})
			.show();
	}
	
	@Override
	public void onClick(View v) {
		MessageModel msg = v.getTag() instanceof ViewHolder ? ((ViewHolder) v.getTag()).msg
							: (MessageModel) v.getTag();

		switch (v.getId()) {
			case R.id.bottom_show:
				showMsg(msg);
				break;
			case R.id.bottom_orig:
				if (msg instanceof CommentModel) {
					showMsg(((CommentModel) msg).status);
				} else {
					showMsg(msg.retweeted_status);
				}
				break;
			case R.id.bottom_repost:
				repostMsg(msg);
				break;
			case R.id.bottom_reply:
				if (msg instanceof CommentModel) {
					replyToComment((CommentModel) msg);
				} else {
					commentOnMsg(msg);
				}
				break;
			case R.id.bottom_delete:
				delete(msg);
				break;
			case R.id.bottom_copy:
				Utility.copyToClipboard(mContext, msg.text);
				break;
			default:
				if (msg instanceof CommentModel) {
					replyToComment((CommentModel) msg);
				} else {
					showMsg(msg);
				}
				break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		MessageModel msg = v.getTag() instanceof ViewHolder ? ((ViewHolder) v.getTag()).msg
							: (MessageModel) v.getTag();

		if (v.getTag() instanceof ViewHolder) {
			ViewHolder h = (ViewHolder) v.getTag();
			
			SwipeLayout.Status status = h.getSwipe().getOpenStatus();

			if (status == SwipeLayout.Status.Close) {
				h.getSwipe().open(true);
			} else {
				h.getSwipe().close(true);
			}

			return true;
		}

		return false;
	}
	
	private View bindView(final MessageModel msg, View convertView) {
		/*if (DEBUG) {
			Debug.startMethodTracing("TraceLog");
		}*/

		View v = null;
		ViewHolder h = null;
		boolean useExisted = true;
		
		// If not inflated before, then we have much work to do
		v = convertView;
		
		if (v == null) {
			useExisted = false;
			v = mInflater.inflate(R.layout.weibo, null);
		}

		if (!useExisted) {
			h = new ViewHolder(v, msg);
			bindSwipe(h, msg);
		} else {
			h = (ViewHolder) v.getTag();
			
			if (h.msg != null) {
				onMovedToScrapHeap(v);
			}
			
			h.msg = msg;
		}

		new SwipeBinder().execute(h);
		
		TextView name = h.getName();
		TextView from = h.getFrom();
		TextView content = h.getContent();
		TextView date = h.getDate();
		TextView attitudes = h.getAttitudes();
		TextView retweet = h.getRetweets();
		TextView comments = h.getComments();
		
		name.setText(msg.user != null ? msg.user.getName() : "");
		from.setText(msg.source != null ? Utility.truncateSourceString(msg.source) : "");
		content.setText(SpannableStringUtils.getSpan(mContext, msg));
		content.setMovementMethod(HackyMovementMethod.getInstance());
		
		date.setText(mTimeUtils.buildTimeString(msg.millis));

		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.getCommentAndRetweet().setVisibility(View.GONE);
		} else {
			attitudes.setText(String.valueOf(msg.attitudes_count));
			retweet.setText(String.valueOf(msg.reposts_count));
			comments.setText(String.valueOf(msg.comments_count));
		}
		
		bindMultiPicLayout(h, msg, true);
		
		// If this retweets/repies to others, show the original
		if (mBindOrig) {
			if (!(msg instanceof CommentModel) && msg.retweeted_status != null) {
				bindOrig(h, msg.retweeted_status, true);
			} else if (msg instanceof CommentModel) {
				CommentModel comment = (CommentModel) msg;
				if (comment.reply_comment != null) {
					bindOrig(h, comment.reply_comment, false);
				} else if (comment.status != null) {
					bindOrig(h, comment.status, false);
				}
			}
				
		}
		
		if (msg.user != null) {
			Bitmap bmp = mUserApi.getCachedSmallAvatar(msg.user);
			
			if (bmp != null) {
				h.getAvatar().setImageBitmap(bmp);
				h.getAvatar().setTag(false);
			}
			
			h.getAvatar().setTag(TAG_MSG, msg);
			h.getAvatar().setOnClickListener(sAvatarListener);
		}
		
		h.getCard().setOnClickListener(this);
		h.getCard().setOnLongClickListener(this);
		
		new ImageDownloader().execute(new Object[]{v});
		
		/*if (DEBUG) {
			Debug.stopMethodTracing();
		}*/

		return v;
	}

	private void bindSwipe(ViewHolder h, MessageModel msg) {
		SwipeLayout swipe = h.getSwipe();
		swipe.setShowMode(SwipeLayout.ShowMode.LayDown);
		swipe.setDragEdge(SwipeLayout.DragEdge.Right);
		swipe.addSwipeDenier(h.getScroll());
		
		// Initialize all click events
		h.getReply().setOnClickListener(this);
		h.getReply().setOnLongClickListener(this);
		h.getShow().setOnClickListener(this);
		h.getShow().setOnLongClickListener(this);
		h.getDelete().setOnClickListener(this);
		h.getDelete().setOnLongClickListener(this);
		h.getRepost().setOnClickListener(this);
		h.getRepost().setOnLongClickListener(this);
		h.getOrig().setOnClickListener(this);
		h.getOrig().setOnLongClickListener(this);
		h.getCopy().setOnClickListener(this);
		h.getCopy().setOnLongClickListener(this);
	}

	private void bindSwipeActions(ViewHolder h) {
		MessageModel msg = h.msg;

		// Hide all
		h.getReply().setVisibility(View.GONE);
		h.getShow().setVisibility(View.GONE);
		h.getDelete().setVisibility(View.GONE);
		h.getRepost().setVisibility(View.GONE);
		h.getOrig().setVisibility(View.GONE);
		h.getCopy().setVisibility(View.GONE);

		// Show only needed
		if (msg instanceof CommentModel) {
			CommentModel comment = (CommentModel) msg;
			h.getCopy().setVisibility(View.VISIBLE);
			h.getReply().setVisibility(View.VISIBLE);
			h.getOrig().setVisibility(View.VISIBLE);
			
			if (comment.user.id.equals(mUid)
				|| (comment.status != null && comment.status.user.id != null
				&& comment.status.user.id.equals(mUid))) {

				h.getDelete().setVisibility(View.VISIBLE);
			}
		} else {
			h.getCopy().setVisibility(View.VISIBLE);
			h.getShow().setVisibility(View.VISIBLE);
			h.getReply().setVisibility(View.VISIBLE);
			h.getRepost().setVisibility(View.VISIBLE);
			
			if (msg.retweeted_status != null) {
				h.getOrig().setVisibility(View.VISIBLE);
			}

			if (msg.user.id.equals(mUid)) {
				h.getDelete().setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void bindOrig(ViewHolder h, MessageModel msg, boolean showPic) {
		h.getOriginParent().setVisibility(View.VISIBLE);
		h.getOrigContent().setText(SpannableStringUtils.getOrigSpan(mContext, msg));
		h.getOrigContent().setMovementMethod(HackyMovementMethod.getInstance());
		
		bindMultiPicLayout(h, msg, showPic);
		
		if (!(msg instanceof CommentModel)) {
			h.getOriginParent().setTag(msg);
			h.getOriginParent().setOnClickListener(this);
		} else {
			h.getOriginParent().setTag(null);
			h.getOriginParent().setOnClickListener(null);
		}
	}
	
	private void bindMultiPicLayout(ViewHolder h, MessageModel msg, boolean showPic) {
		HackyHorizontalScrollView scroll = h.getScroll();

		if (showPic && (msg.thumbnail_pic != null || msg.pic_urls.size() > 0) && !(mAutoNoPic && !isWIFI)) {
			scroll.setVisibility(View.VISIBLE);

			LinearLayout container = h.getContainer();

			int numChilds = msg.hasMultiplePictures() ? msg.pic_urls.size() : 1;

			for (int i = 0; i < 9; i++) {
				ImageView iv = (ImageView) container.getChildAt(i);
				
				if (i >= numChilds) {
					iv.setVisibility(View.GONE);
				} else {
					Bitmap bmp = mHomeApi.getCachedThumbnail(msg, i);
					
					if (bmp != null) {
						iv.setImageBitmap(bmp);
						iv.setTag(false);
					}
					
					iv.setTag(TAG_MSG, msg);
					iv.setTag(TAG_ID, i);
					
					iv.setOnClickListener(sImageListener);
				}
			}
		}
	}
	
	public void notifyDataSetChangedAndClear() {
		super.notifyDataSetChanged();
	}
	
	private boolean waitUntilNotScrolling(ViewHolder h, MessageModel msg) {
		while (mScrolling) {
			if (h.msg != msg) {
				return false;
			}
			
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				return false;
			}
		}
		
		return true;
	}

	// Wait until user stops scrolling and then we create the swipe layout
	private class SwipeBinder extends AsyncTask<ViewHolder, Void, Boolean> {
		ViewHolder h;

		@Override
		protected Boolean doInBackground(ViewHolder... params) {
			h = params[0];
			
			return waitUntilNotScrolling(h, h.msg);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				try {
					bindSwipeActions(h);
				} catch (NullPointerException e) {
					// Ignore all NPEs
				}
			}
		}
	}
	
	// Downloads images including avatars
	private class ImageDownloader extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object[] params) {
			View v = (View) params[0];
			ViewHolder h = (ViewHolder) v.getTag();
			MessageModel msg = h.msg;
			
			Object tag = h.getAvatar().getTag();
			
			if (tag == null) {
				tag = true;
			}
			
			// Avatars
			if (v != null && Boolean.parseBoolean(tag.toString())) {
				if (!waitUntilNotScrolling(h, msg)) return null;
				
				Bitmap avatar = mUserApi.getSmallAvatar(msg.user);
				
				publishProgress(new Object[]{v, 0, avatar, msg});
			}
			
			// Images
			MessageModel realMsg = msg;

			if (msg.retweeted_status != null) {
				realMsg = msg.retweeted_status;
			}
			
			if (v != null && !(msg instanceof CommentModel) && (realMsg.pic_urls.size() > 0 || !TextUtils.isEmpty(msg.thumbnail_pic))) {
				if (!waitUntilNotScrolling(h, msg)) return null;
				
				LinearLayout container = h.getContainer();
				
				int numChilds = realMsg.hasMultiplePictures() ? realMsg.pic_urls.size() : 1;
				
				for (int i = 0; i < numChilds; i++) {
					if (!waitUntilNotScrolling(h, msg)) return null;
					
					ImageView imgView = (ImageView) container.getChildAt(i);
					
					tag = imgView.getTag();
					
					if (tag == null) {
						tag = true;
					}
					
					if (!Boolean.parseBoolean(tag.toString())) continue;
					
					Bitmap img = mHomeApi.getThumbnailPic(realMsg, i);
					
					if (img != null) {
						publishProgress(new Object[]{v, 1, img, imgView, i, msg});
					}
				}
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			
			View v = (View) values[0];
			
			if (!(v.getTag() instanceof ViewHolder) || (((ViewHolder) v.getTag()).msg != null &&
				((ViewHolder) v.getTag()).msg.id != ((MessageModel) values[values.length -1]).id)) {
				
				return;
				
			}
			
			switch (Integer.parseInt(String.valueOf(values[1]))) {
				case 0:
					Bitmap avatar = (Bitmap) values[2];
					if (v != null) {
						ImageView iv = ((ViewHolder) v.getTag()).getAvatar();
						if (iv != null) {
							iv.setImageBitmap(avatar);
						}
					}
					break;
				case 1:
					Bitmap img = (Bitmap) values[2];
					ImageView iv = (ImageView) values[3];
					iv.setImageBitmap(img);
					break;
			}
			
		}

		
	}
	
	private class DeleteTask extends AsyncTask<MessageModel, Void, Void> {
		private ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(mContext);
			prog.setMessage(mContext.getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(MessageModel[] params) {
			if (params[0] instanceof CommentModel) {
				NewCommentApi.deleteComment(params[0].id);
			} else {
				PostApi.deletePost(params[0].id);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();
		}
	}
	
	private static class ViewHolder {
		public MessageModel msg;
		public boolean sub = false;
		
		private TextView date, retweets, comments, name, from, content, attitudes;
		private HackyHorizontalScrollView scroll;
		private LinearLayout container;
		private View originParent;
		private View comment_and_retweet;
		private ImageView weibo_avatar;
		private View v;
		private TextView orig_content;
		private SwipeLayout swipe;
		private View reply, show, delete, repost, orig, copy;
		private View card;
		private DynamicGridLayout grid;
		
		public ViewHolder(View v, MessageModel msg) {
			this.v = v;
			this.msg = msg;
			
			v.setTag(this);
		}
		
		public TextView getDate() {
			if (date == null) {
				date = (TextView) v.findViewById(R.id.weibo_date);
			}
			
			return date;
		}

		public TextView getAttitudes() {
			if (attitudes == null) {
				attitudes = (TextView) v.findViewById(R.id.weibo_attitudes);
			}

			return attitudes;
		}
		
		public TextView getComments() {
			if (comments == null) {
				comments = (TextView) v.findViewById(R.id.weibo_comments);
			}

			return comments;
		}
		
		public TextView getRetweets() {
			if (retweets == null) {
				retweets = (TextView) v.findViewById(R.id.weibo_retweet);
			}
			
			return retweets;
		}
		
		public TextView getName() {
			if (name == null) {
				name = (TextView) v.findViewById(R.id.weibo_name);
			}
			
			return name;
		}
		
		public TextView getFrom() {
			if (from == null) {
				from = (TextView) v.findViewById(R.id.weibo_from);
			}
			
			return from;
		}
		
		public TextView getContent() {
			if (content == null) {
				content = (TextView) v.findViewById(R.id.weibo_content);
			}
			
			return content;
		}
		
		public HackyHorizontalScrollView getScroll() {
			if (scroll == null) {
				scroll = (HackyHorizontalScrollView) v.findViewById(R.id.weibo_pics_scroll);
			}
			
			return scroll;
		}
		
		public LinearLayout getContainer() {
			if (container == null) {
				container = (LinearLayout) getScroll().findViewById(R.id.weibo_pics);
			}
			
			return container;
		}
		
		public View getOriginParent() {
			if (originParent == null) {
				originParent = v.findViewById(R.id.weibo_origin);
			}
			
			return originParent;
		}
		
		public View getCommentAndRetweet() {
			if (comment_and_retweet == null) {
				comment_and_retweet = v.findViewById(R.id.weibo_comment_and_retweet);
			}
			
			return comment_and_retweet;
		}
		
		public ImageView getAvatar() {
			if (weibo_avatar == null) {
				weibo_avatar = (ImageView) v.findViewById(R.id.weibo_avatar);
			}
			
			return weibo_avatar;
		}
		
		public TextView getOrigContent() {
			if (orig_content == null) {
				orig_content = (TextView) v.findViewById(R.id.weibo_orig_content);
			}
			
			return orig_content;
		}

		public View getCard() {
			if (card == null) {
				card = v.findViewById(R.id.card);
				card.setTag(this);
			}

			return card;
		}

		public SwipeLayout getSwipe() {
			if (swipe == null) {
				swipe = (SwipeLayout) v.findViewById(R.id.swipe);
				swipe.setTag(this);
			}

			return swipe;
		}

		public DynamicGridLayout getGrid() {
			if (grid == null) {
				grid = (DynamicGridLayout) v.findViewById(R.id.bottom_grid);
			}

			return grid;
		}

		public View getReply() {
			if (reply == null) {
				reply = getGrid().dynamicFindViewById(R.id.bottom_reply);
				reply.setTag(this);
			}

			return reply;
		}

		public View getShow() {
			if (show == null) {
				show = getGrid().dynamicFindViewById(R.id.bottom_show);
				show.setTag(this);
			}

			return show;
		}

		public View getDelete() {
			if (delete == null) {
				delete = getGrid().dynamicFindViewById(R.id.bottom_delete);
				delete.setTag(this);
			}

			return delete;
		}

		public View getRepost() {
			if (repost == null) {
				repost = getGrid().dynamicFindViewById(R.id.bottom_repost);
				repost.setTag(this);
			}

			return repost;
		}

		public View getOrig() {
			if (orig == null) {
				orig = getGrid().dynamicFindViewById(R.id.bottom_orig);
				orig.setTag(this);
			}
			
			return orig;
		}

		public View getCopy() {
			if (copy == null) {
				copy = getGrid().dynamicFindViewById(R.id.bottom_copy);
				copy.setTag(this);
			}

			return copy;
		}
	}

}
