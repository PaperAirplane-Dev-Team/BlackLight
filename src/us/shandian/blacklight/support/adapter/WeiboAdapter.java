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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.attitudes.AttitudesApi;
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
public class WeiboAdapter extends BaseAdapter implements AbsListView.RecyclerListener, AbsListView.OnScrollListener, SwipeLayout.SwipeDenier {
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
	private int mFontSize;
	
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
		mFontSize = Settings.getInstance(context).getInt(Settings.FONT_SIZE, 0);
		
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
			
			h.avatar.setImageBitmap(null);
			h.avatar.setTag(true);
			h.comment_and_retweet.setVisibility(View.VISIBLE);

			if (h.attitudes_icon.getColorFilter() != null) {
				h.attitudes_icon.setColorFilter(null);
			}

			h.swipe.close(false);
			
			LinearLayout container = h.pics;
			
			for (int i = 0; i < 9; i++) {
				ImageView iv = (ImageView) container.getChildAt(i);
				iv.setImageBitmap(null);
				iv.setVisibility(View.VISIBLE);
				iv.setTag(true);
			}
			
			h.scroll.setVisibility(View.GONE);
			h.origin_parent.setVisibility(View.GONE);
			
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

	@Override
	public boolean shouldDenySwipe(MotionEvent ev) {
		return true;
	}

	public void addOnScrollListener(AbsListView.OnScrollListener listener) {
		mListeners.add(listener);
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
			bindSwipe(h);
		} else {
			h = (ViewHolder) v.getTag();
			
			if (h.msg != null) {
				onMovedToScrapHeap(v);
			}
			
			h.msg = msg;
		}

		new SwipeBinder().execute(h);
		
		TextView name = h.name;
		TextView from = h.from;
		TextView content = h.content;
		TextView date = h.date;
		TextView attitudes = h.attitudes;
		TextView retweet = h.retweets;
		TextView comments = h.comments;
		ImageView like = h.like;
		
		name.setText(msg.user != null ? msg.user.getName() : "");
		from.setText(msg.source != null ? Utility.truncateSourceString(msg.source) : "");
		content.setText(SpannableStringUtils.getSpan(mContext, msg));
		content.setMovementMethod(HackyMovementMethod.getInstance());

		// TODO Set font size
		if (mFontSize != 0) {
			content.setTextSize(mFontSize);
			retweet.setTextSize(mFontSize);
		}
		
		date.setText(mTimeUtils.buildTimeString(msg.millis));

		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.comment_and_retweet.setVisibility(View.GONE);
		} else {
			attitudes.setText(String.valueOf(msg.attitudes_count));
			retweet.setText(String.valueOf(msg.reposts_count));
			comments.setText(String.valueOf(msg.comments_count));
		}

		if (!(msg instanceof CommentModel) && msg.liked) { 
			h.attitudes_icon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
			like.setImageResource(R.drawable.ic_action_bad);
		}

		if (DEBUG) {
			Log.d(TAG, "liked = " + String.valueOf(msg.liked));
		}
		
		bindMultiPicLayout(h, msg, true);
		
		// If this retweets/replies to others, show the original
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
				h.avatar.setImageBitmap(bmp);
				h.avatar.setTag(false);
			}
		}
		
		new ImageDownloader().execute(new Object[]{v});
		
		/*if (DEBUG) {
			Debug.stopMethodTracing();
		}*/

		return v;
	}

	private void bindSwipe(ViewHolder h) {
		SwipeLayout swipe = h.swipe;
		swipe.setShowMode(SwipeLayout.ShowMode.LayDown);
		swipe.setDragEdge(SwipeLayout.DragEdge.Bottom);
		swipe.addSwipeDenier(this);
	}

	private void bindSwipeActions(ViewHolder h) {
		MessageModel msg = h.msg;

		// Hide all
		h.reply.setVisibility(View.GONE);
		h.delete.setVisibility(View.GONE);
		h.repost.setVisibility(View.GONE);
		h.like.setVisibility(View.GONE);
		h.orig.setVisibility(View.GONE);
		h.copy.setVisibility(View.GONE);

		// Show only needed
		if (msg instanceof CommentModel) {
			CommentModel comment = (CommentModel) msg;
			h.copy.setVisibility(View.VISIBLE);
			h.reply.setVisibility(View.VISIBLE);
			h.orig.setVisibility(View.VISIBLE);
			
			if (comment.user.id.equals(mUid)
				|| (comment.status != null && comment.status.user.id != null
				&& comment.status.user.id.equals(mUid))) {

				h.delete.setVisibility(View.VISIBLE);
			}
		} else {
			h.copy.setVisibility(View.VISIBLE);
			h.reply.setVisibility(View.VISIBLE);
			h.repost.setVisibility(View.VISIBLE);

			if (msg.liked){
				h.like.setImageResource(R.drawable.ic_action_bad);
			}
			h.like.setVisibility(View.VISIBLE);

			if (msg.user.id.equals(mUid)) {
				h.delete.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void bindOrig(ViewHolder h, MessageModel msg, boolean showPic) {
		h.origin_parent.setVisibility(View.VISIBLE);
		h.orig_content.setText(SpannableStringUtils.getOrigSpan(mContext, msg));
		h.orig_content.setMovementMethod(HackyMovementMethod.getInstance());
		
		bindMultiPicLayout(h, msg, showPic);
		
		if (!(msg instanceof CommentModel)) {
			h.origin_parent.setTag(msg);
		} else {
			h.origin_parent.setTag(null);
		}
	}
	
	private void bindMultiPicLayout(ViewHolder h, MessageModel msg, boolean showPic) {
		HorizontalScrollView scroll = h.scroll;

		if (showPic && (msg.thumbnail_pic != null || msg.pic_urls.size() > 0) && !(mAutoNoPic && !isWIFI)) {
			scroll.setVisibility(View.VISIBLE);

			LinearLayout container = h.pics;

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
			
			Object tag = h.avatar.getTag();
			
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
				
				LinearLayout container = h.pics;
				
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
						ImageView iv = ((ViewHolder) v.getTag()).avatar;
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

	private class LikeTask extends AsyncTask<Object, Void, Boolean>{
		private MessageModel mm;
		private ViewHolder h;

		@Override
		protected Boolean doInBackground(Object... params) {
			mm = (MessageModel) params[0];
			h = (ViewHolder) params[1];
			if (mm.liked){
				return AttitudesApi.cancelLike(mm.id);
			}else{
				return AttitudesApi.like(mm.id);
			}
		}

		@Override
		protected void onPostExecute(Boolean result){
			if (result && mm == h.msg){
				if(mm.liked){
					-- mm.attitudes_count;
				}else{
					++ mm.attitudes_count;
				}
				mm.liked = !mm.liked;

				if (mm.liked) {
					h.attitudes_icon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
				} else {
					h.attitudes_icon.setColorFilter(null);
				}

				h.attitudes.setText(String.valueOf(mm.attitudes_count));
			}
		}
	}
	
	class ViewHolder {
		public boolean sub = false;

		@InjectView(R.id.weibo_date) public TextView date;
		@InjectView(R.id.weibo_retweet) public TextView retweets;
		@InjectView(R.id.weibo_comments) public TextView comments;
		@InjectView(R.id.weibo_name) public TextView name;
		@InjectView(R.id.weibo_from) public TextView from;
		@InjectView(R.id.weibo_content) public TextView content;
		@InjectView(R.id.weibo_attitudes) public TextView attitudes;
		@InjectView(R.id.weibo_orig_content) public TextView orig_content;
		@InjectView(R.id.weibo_avatar) public ImageView avatar;
		@InjectView(R.id.bottom_like) public ImageView like;
		@InjectView(R.id.bottom_reply) public ImageView reply;
		@InjectView(R.id.bottom_delete) public ImageView delete;
		@InjectView(R.id.bottom_repost) public ImageView repost;
		@InjectView(R.id.bottom_orig) public ImageView orig;
		@InjectView(R.id.bottom_copy) public ImageView copy;
		@InjectView(R.id.swipe) public SwipeLayout swipe;
		@InjectView(R.id.weibo_pics_scroll) public HorizontalScrollView scroll;
		@InjectView(R.id.weibo_pics) public LinearLayout pics;
		@InjectView(R.id.weibo_bottom) public View bottom;
		@InjectView(R.id.card) public View card;
		@InjectView(R.id.weibo_origin) public View origin_parent;
		@InjectView(R.id.weibo_comment_and_retweet) public View comment_and_retweet;
		@InjectView(R.id.weibo_attitudes_icon) public ImageView attitudes_icon;
		
		public View v;
		public MessageModel msg;
		public Context context;

		public ViewHolder(View v, MessageModel msg) {
			this.v = v;
			this.msg = msg;
			this.context = v.getContext();

			v.setTag(this);
			ButterKnife.inject(this, v);
		}

		@OnLongClick({
			R.id.card, R.id.weibo_bottom,
			R.id.bottom_like, R.id.bottom_reply,
			R.id.bottom_delete, R.id.bottom_repost,
			R.id.bottom_orig, R.id.bottom_copy,
		})
		boolean openOrClose() {
			if (swipe.getOpenStatus() == SwipeLayout.Status.Close) {
				swipe.open(true);
			} else {
				swipe.close(true);
			}

			return true;
		}


		@OnClick(R.id.weibo_avatar)
		void showUser() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", msg.user);
			context.startActivity(i);
		}

		@OnClick(R.id.card)
		void show() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);

			if (msg instanceof CommentModel) {
				i.setClass(context, ReplyToActivity.class);
				i.putExtra("comment", (CommentModel) msg);
			} else {
				i.setClass(context, SingleActivity.class);
				i.putExtra("msg", msg);
			}

			context.startActivity(i);
		}

		@OnClick({R.id.weibo_origin, R.id.bottom_orig})
		void showOrig() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(context, SingleActivity.class);

			if (!(msg instanceof CommentModel)) {
				if (msg.retweeted_status != null) {
					i.putExtra("msg", msg.retweeted_status);
				}
			} else {
				i.putExtra("msg", ((CommentModel) msg).status);
			}

			context.startActivity(i);
		}

		@OnClick({R.id.bottom_repost, R.id.weibo_retweet_icon})
		void repost() {
			if(!(msg instanceof CommentModel)) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, RepostActivity.class);
				i.putExtra("msg", msg);
				context.startActivity(i);
			}
		}

		@OnClick(R.id.bottom_copy)
		void copy() {
			Utility.copyToClipboard(context, msg.text);
		}

		@OnClick({R.id.bottom_reply, R.id.weibo_comments_icon})
		void reply() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);

			if (msg instanceof CommentModel) {
				i.setClass(context, ReplyToActivity.class);
				i.putExtra("comment", (CommentModel) msg);
			} else {
				i.setClass(context, CommentOnActivity.class);
				i.putExtra("msg", msg);
			}
			
			context.startActivity(i);
		}

		@OnClick({R.id.bottom_like, R.id.weibo_attitudes_icon})
		void like() {
			new LikeTask().execute(msg, this);
		}

		@OnClick(R.id.bottom_delete)
		void delete() {
			new AlertDialog.Builder(context)
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

	}

}
