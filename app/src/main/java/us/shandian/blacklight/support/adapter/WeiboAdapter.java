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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Debug;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.attitudes.AttitudesApi;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.comments.CommentOnActivity;
import us.shandian.blacklight.ui.comments.ReplyToActivity;
import us.shandian.blacklight.ui.common.ImageActivity;
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
public class WeiboAdapter extends RecyclerView.Adapter<WeiboAdapter.ViewHolder> {
	private static final String TAG = WeiboAdapter.class.getSimpleName();

	private static final int TAG_MSG = R.id.weibo_content;
	private static final int TAG_ID = R.id.card;

	private static final View.OnClickListener sImageListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ActivityOptionsCompat o = ActivityOptionsCompat
				.makeSceneTransitionAnimation((Activity) v.getContext(), v, "model");
			MessageModel msg = (MessageModel) v.getTag(TAG_MSG);
			int id = Integer.parseInt(v.getTag(TAG_ID).toString());
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), ImageActivity.class);
			i.putExtra("model", msg);
			i.putExtra("defaultId", id);
			ActivityCompat.startActivity((Activity) v.getContext(), i, o.toBundle());
		}
	};
	
	private MessageListModel mList;
	private MessageListModel mClone;
	private LayoutInflater mInflater;
	private StatusTimeUtils mTimeUtils;
	private UserApiCache mUserApi;
	private HomeTimeLineApiCache mHomeApi;
	private LoginApiCache mLogin;
	private View mHeader = null;
	
	private String mUid;
	
	private int mGray;
	
	private Context mContext;

	private ArrayList<RecyclerView.OnScrollListener> mListeners = new ArrayList<RecyclerView.OnScrollListener>();
	
	private boolean mBindOrig;
	private boolean mShowCommentStatus;
	private boolean mScrolling = false;
	private boolean mAutoNoPic = false;
	private String mAppName;
	
	public WeiboAdapter(Context context, RecyclerView listView, MessageListModel list, boolean bindOrig, boolean showCommentStatus) {
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
		mAppName = context.getString(R.string.app_name);
		
		listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

				@Override
				public void onScrollStateChanged(RecyclerView v, int state) {
					mScrolling = state != RecyclerView.SCROLL_STATE_IDLE;

					// Inform all listeners
					for (RecyclerView.OnScrollListener listener : mListeners) {
						if (listener != null) {
							listener.onScrollStateChanged(v, state);
						}
					}
				}

				@Override
				public void onScrolled(RecyclerView p1, int p2, int p3) {
					// Inform all listeners
					for (RecyclerView.OnScrollListener listener : mListeners) {
						if (listener != null) {
							listener.onScrolled(p1, p2, p3);
						}
					}
				}
		});
		notifyDataSetChangedAndClone();
	}
	
	@Override
	public int getItemCount() {
		return mHeader != null ? mClone.getSize() + 1 : mClone.getSize();
	}

	@Override
	public int getItemViewType(int position) {
		if (mHeader != null && position == 0) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		return mClone.get(position).id;
	}

	@Override
	public void onViewRecycled(ViewHolder h) {
		if (h.isHeader) return;
		
		h.avatar.setImageResource(R.color.gray);
		h.avatar.setTag(true);
		h.comment_and_retweet.setVisibility(View.VISIBLE);
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
	
	public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
		mListeners.add(listener);
	}
	
	public void setHeaderView(View header) {
		mHeader = header;
	}

	@Override
	public WeiboAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == -1) {
			return new ViewHolder(this, mHeader, true);
		} else {
			View v = mInflater.inflate(R.layout.weibo, null);
			return new ViewHolder(this, v, false);
		}
	}
	
	@Override
	public void onBindViewHolder(ViewHolder h, int position) {
		/*if (DEBUG) {
			Debug.startMethodTracing("TraceLog");
		}*/
		
		if (mHeader != null) {
			if (position == 0) {
				return;
			} else {
				position--;
			}
		}

		View v = h.v;
		final MessageModel msg = mClone.get(position);
		h.msg = msg;
		
		TextView name = h.name;
		TextView from = h.from;
		TextView content = h.content;
		TextView date = h.date;
		TextView attitudes = h.attitudes;
		TextView retweet = h.retweets;
		TextView comments = h.comments;
		
		name.setText(msg.user != null ? msg.user.getName() : "");
		
		String ver = "";
		if (msg.annotations.size() > 0 && !(ver = msg.annotations.get(0).bl_version).trim().equals("")) {
			// Show a fake tail for BL :)
			from.setText(mAppName);
		} else {
			from.setText(TextUtils.isEmpty(msg.source) ? "" : Utility.truncateSourceString(msg.source));
		}

		content.setText(SpannableStringUtils.getSpan(mContext, msg));
		content.setMovementMethod(HackyMovementMethod.getInstance());
		
		date.setText(mTimeUtils.buildTimeString(msg.millis));

		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.comment_and_retweet.setVisibility(View.GONE);
		} else {
			attitudes.setText(Utility.addUnitToInt(mContext, msg.attitudes_count));
			retweet.setText(Utility.addUnitToInt(mContext, msg.reposts_count));
			comments.setText(Utility.addUnitToInt(mContext, msg.comments_count));
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

	void buildPopup(final ViewHolder h) {
		PopupMenu p = new PopupMenu(mContext, h.popup);
		p.inflate(R.menu.popup);
		final Menu m = p.getMenu();

		// Show needed items
		m.findItem(R.id.popup_copy).setVisible(true);
		if (h.msg instanceof CommentModel) {
			m.findItem(R.id.popup_reply).setVisible(true);

			CommentModel cmt = (CommentModel) h.msg;
			if (cmt.user.id.equals(mUid) || (cmt.status != null && cmt.status.user != null && cmt.status.user.id.equals(mUid))) {
				m.findItem(R.id.popup_delete).setVisible(true);
			}
		} else {
			m.findItem(R.id.popup_repost).setVisible(true);
			m.findItem(R.id.popup_comment).setVisible(true);

			if (h.msg.liked) {
				m.findItem(R.id.popup_unlike).setVisible(true);
			} else {
				m.findItem(R.id.popup_like).setVisible(true);
			}

			if (h.msg.user != null && h.msg.user.id.equals(mUid)) {
				m.findItem(R.id.popup_delete).setVisible(true);
			}
		}

		p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.popup_delete:
						h.delete();
						break;
					case R.id.popup_copy:
						h.copy();
						break;
					case R.id.popup_comment:
					case R.id.popup_reply:
						h.reply();
						break;
					case R.id.popup_repost:
						h.repost();
						break;
					case R.id.popup_like:
					case R.id.popup_unlike:
						new LikeTask().execute(h.msg, h, m);
						break;
				}

				return true;
			}
		});

		// Pop up!
		p.show();
	}
	
	public void notifyDataSetChangedAndClone() {
		mClone = mList.clone();
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
	
	private static class DeleteTask extends AsyncTask<MessageModel, Void, Void> {
		private ProgressDialog prog;
		private Context context;
		
		public DeleteTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(context);
			prog.setMessage(context.getResources().getString(R.string.plz_wait));
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
		private Menu m;

		@Override
		protected Boolean doInBackground(Object... params) {
			mm = (MessageModel) params[0];
			h = (ViewHolder) params[1];
			m = (Menu) params[2];
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

				h.attitudes.setText(String.valueOf(mm.attitudes_count));

				m.findItem(R.id.popup_unlike).setVisible(mm.liked);
				m.findItem(R.id.popup_like).setVisible(!mm.liked);
			}
		}
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public boolean sub = false;

		public TextView date;
		public TextView retweets;
		public TextView comments;
		public TextView name;
		public TextView from;
		public TextView content;
		public TextView attitudes;
		public TextView orig_content;
		public ImageView avatar;
		public ImageView popup;
		public HorizontalScrollView scroll;
		public LinearLayout pics;
		public View card;
		public View origin_parent;
		public View comment_and_retweet;
		
		public View v;
		public MessageModel msg = null;
		public Context context;
		public WeiboAdapter adapter;
		public boolean isHeader = false;

		public ViewHolder(WeiboAdapter adapter, View v, boolean isHeader) {
			super(v);
			this.v = v;
			this.context = v.getContext();
			this.adapter = adapter;
			this.isHeader = isHeader;

			v.setTag(this);
			
			if (!isHeader) {
				init();
			}
		}
		
		private void init() {
			// Views
			date = Utility.findViewById(v, R.id.weibo_date);
			retweets = Utility.findViewById(v, R.id.weibo_retweet);
			comments = Utility.findViewById(v, R.id.weibo_comments);
			name = Utility.findViewById(v, R.id.weibo_name);
			from = Utility.findViewById(v, R.id.weibo_from);
			content = Utility.findViewById(v, R.id.weibo_content);
			attitudes = Utility.findViewById(v, R.id.weibo_attitudes);
			orig_content = Utility.findViewById(v, R.id.weibo_orig_content);
			avatar = Utility.findViewById(v, R.id.weibo_avatar);
			popup = Utility.findViewById(v, R.id.weibo_popup);
			scroll = Utility.findViewById(v, R.id.weibo_pics_scroll);
			pics = Utility.findViewById(v, R.id.weibo_pics);
			card = Utility.findViewById(v, R.id.card);
			origin_parent = Utility.findViewById(v, R.id.weibo_origin);
			comment_and_retweet = Utility.findViewById(v, R.id.weibo_comment_and_retweet);
			
			// Events
			Utility.bindOnClick(this, popup, "popup");
			Utility.bindOnClick(this, avatar, "showUser");
			Utility.bindOnClick(this, card, "show");
			Utility.bindOnClick(this, origin_parent, "showOrig");
		}

		void popup() {
			adapter.buildPopup(this);
		}

		void showUser() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", msg.user);
			context.startActivity(i);
		}

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
			
			ActivityOptionsCompat o =
				ActivityOptionsCompat.makeSceneTransitionAnimation(
					(Activity) context, v, "msg");

			ActivityCompat.startActivity((Activity) context, i, o.toBundle());
		}

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
			
			ActivityOptionsCompat o =
				ActivityOptionsCompat.makeSceneTransitionAnimation(
				(Activity) context, origin_parent, "msg");

			ActivityCompat.startActivity((Activity) context, i, o.toBundle());
		}

		void repost() {
			if(!(msg instanceof CommentModel)) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, RepostActivity.class);
				i.putExtra("msg", msg);
				context.startActivity(i);
			}
		}

		void copy() {
			Utility.copyToClipboard(context, msg.text);
		}

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

		void delete() {
			new AlertDialog.Builder(context)
				.setMessage(R.string.confirm_delete)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						new DeleteTask(context).execute(msg);
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
