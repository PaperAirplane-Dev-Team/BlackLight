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
import android.view.LayoutInflater;
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

import java.util.HashMap;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.ui.common.ImageActivity;
import us.shandian.blacklight.ui.comments.ReplyToActivity;
import us.shandian.blacklight.ui.statuses.SingleActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/*
  This is a common adapter for all kinds of weibo data (Statuses / Comments)
  Adapting them to ListViews.
  They share one common layout
*/
public class WeiboAdapter extends BaseAdapter implements AbsListView.RecyclerListener, AbsListView.OnScrollListener, 
										View.OnClickListener, View.OnLongClickListener
{
	private static final String TAG = WeiboAdapter.class.getSimpleName();
	
	private MessageListModel mList;
	private LayoutInflater mInflater;
	private StatusTimeUtils mTimeUtils;
	private UserApiCache mUserApi;
	private HomeTimeLineApiCache mHomeApi;
	private LoginApiCache mLogin;
	
	private String mUid;
	
	private int mGray;
	
	private Context mContext;
	
	private boolean mBindOrig;
	private boolean mShowCommentStatus;
	private boolean mScrolling = false;
	
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
			
			return bindView(msg, convertView, false);
		}
	}

	@Override
	public void onMovedToScrapHeap(View v) {
		if (v.getTag() instanceof ViewHolder) {
			ViewHolder h = (ViewHolder) v.getTag();
			h.getAvatar().setImageBitmap(null);
			h.getCommentAndRetweet().setVisibility(View.VISIBLE);
			
			LinearLayout container = h.getContainer();
			
			for (int i = 0; i < 9; i++) {
				ImageView iv = (ImageView) container.getChildAt(i);
				iv.setImageBitmap(null);
				iv.setVisibility(View.VISIBLE);
				iv.setOnClickListener(null);
			}
			
			h.getScroll().setVisibility(View.GONE);
			h.getOriginParent().setVisibility(View.GONE);
			
			if (h.getOriginParent().getChildCount() > 0) {
				onMovedToScrapHeap(h.getOriginParent().getChildAt(0));
			}
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView v, int state) {
		mScrolling = state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
	}
	
	@Override
	public void onScroll(AbsListView p1, int p2, int p3, int p4) {
		// Do nothing
	}
	
	@Override
	public void onClick(View v) {
		MessageModel msg = ((ViewHolder) v.getTag()).msg;
		if (msg instanceof CommentModel) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(mContext, ReplyToActivity.class);
			i.putExtra("comment", (CommentModel) msg);
			mContext.startActivity(i);
		} else {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(mContext, SingleActivity.class);
			i.putExtra("msg", msg);
			mContext.startActivity(i);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		MessageModel msg = ((ViewHolder) v.getTag()).msg;
		if (msg instanceof CommentModel) {
			final CommentModel comment = (CommentModel) msg;
			if (comment.user.id.equals(mUid) || (comment.status != null && comment.status.user.id != null && comment.status.user.id.equals(mUid))) {
				new AlertDialog.Builder(mContext)
					.setMessage(R.string.confirm_delete)
					.setCancelable(true)
					.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							new DeleteTask().execute(comment);
						}
					})
					.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					})
					.show();
				return true;
			}
		}
		return false;
	}
	
	private View bindView(final MessageModel msg, View convertView, boolean sub) {
		View v = null;
		ViewHolder h = null;
		
		// If not inflated before, then we have much work to do
		v = convertView != null ? convertView : mInflater.inflate(sub ? R.layout.weibo_content : R.layout.weibo, null);
		
		if (convertView == null) {
			h = new ViewHolder(v, msg);
		} else {
			h = (ViewHolder) v.getTag();
			h.msg = msg;
			h.span = null;
		}
		
		TextView name = h.getName();
		TextView from = h.getFrom();
		TextView content = h.getContent();
		TextView date = h.getDate();
		TextView retweet = h.getRetweets();
		TextView comments = h.getComments();
		HorizontalScrollView scroll = h.getScroll();
		
		if (msg.thumbnail_pic != null || msg.pic_urls.size() > 0) {
			scroll.setVisibility(View.VISIBLE);
		}
		
		name.setText(msg.user != null ? msg.user.getName() : "");
		from.setText(msg.source != null ? Html.fromHtml(msg.source).toString() : "");
		content.setText(h.getSpan());
		content.setMovementMethod(HackyMovementMethod.getInstance());
		
		date.setText(mTimeUtils.buildTimeString(msg.created_at));

		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.getCommentAndRetweet().setVisibility(View.GONE);
		} else {
			retweet.setText(String.valueOf(msg.reposts_count));
			comments.setText(String.valueOf(msg.comments_count));
		}
		
		// If this retweets/repies to others, show the original
		if (!sub && mBindOrig) {
			View origin = null;
			boolean originViewBinded = false;
			boolean hasOrigin = false;
			LinearLayout originParent = h.getOriginParent();
			
			if (originParent.getChildCount() > 0) {
				origin = originParent.getChildAt(0);
				originViewBinded = true;
			}
			
			if (!(msg instanceof CommentModel) && msg.retweeted_status != null) {
				origin = bindView(msg.retweeted_status, origin, true);
				hasOrigin = true;
			} else if (msg instanceof CommentModel) {
				CommentModel comment = (CommentModel) msg;
				if (comment.reply_comment != null) {
					origin = bindView(comment.reply_comment, origin, true);
					hasOrigin = true;
				} else if (comment.status != null) {
					origin = bindView(comment.status, origin, true);
					hasOrigin = true;
				}
			}
				
			if (hasOrigin) {
				origin.setBackgroundColor(mGray);
				
				if (!originViewBinded) {
					originParent.addView(origin);
				}
				originParent.setVisibility(View.VISIBLE);
				
				if (msg instanceof CommentModel) {
					h.getCommentAndRetweet().setVisibility(View.GONE);
				}
				
			}
		}
		
		v.setOnClickListener(this);
		v.setOnLongClickListener(this);
		
		new ImageDownloader().execute(new Object[]{v});
		
		return v;
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
	
	// Downloads images including avatars
	private class ImageDownloader extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object[] params) {
			View v = (View) params[0];
			ViewHolder h = (ViewHolder) v.getTag();
			MessageModel msg = h.msg;
			
			// Avatars
			if (v != null) {
				if (!waitUntilNotScrolling(h, msg)) return null;
				
				Bitmap avatar = mUserApi.getSmallAvatar(msg.user);
				
				publishProgress(new Object[]{v, 0, avatar, msg});
			}
			
			// Images
			if (v != null && !(msg instanceof CommentModel) && (msg.pic_urls.size() > 0 || !TextUtils.isEmpty(msg.thumbnail_pic))) {
				if (!waitUntilNotScrolling(h, msg)) return null;
				
				publishProgress(v, 2, msg);
				
				LinearLayout container = h.getContainer();
				
				int numChilds = msg.hasMultiplePictures() ? msg.pic_urls.size() : 1;
				
				for (int i = 0; i < numChilds; i++) {
					if (!waitUntilNotScrolling(h, msg)) return null;
					
					ImageView imgView = (ImageView) container.getChildAt(i);
					Bitmap img = mHomeApi.getThumbnailPic(msg, i);
					
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
			
			if (!(v.getTag() instanceof ViewHolder) ||
				((ViewHolder) v.getTag()).msg.id != ((MessageModel) values[values.length -1]).id) {
				
				return;
				
			}
			
			switch (Integer.parseInt(String.valueOf(values[1]))) {
				case 0:
					Bitmap avatar = (Bitmap) values[2];
					if (v != null) {
						ImageView iv = ((ViewHolder) v.getTag()).getAvatar();
						if (iv != null) {
							iv.setImageBitmap(avatar);
							
							final MessageModel msg = (MessageModel) values[3];
							
							iv.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent i = new Intent();
									i.setAction(Intent.ACTION_MAIN);
									i.setClass(mContext, UserTimeLineActivity.class);
									i.putExtra("user", msg.user);
									mContext.startActivity(i);
								}
							});
						}
					}
					break;
				case 1:
					Bitmap img = (Bitmap) values[2];
					ImageView iv = (ImageView) values[3];
					iv.setImageBitmap(img);
					
					final int finalId = values[4];
					final MessageModel finalMsg = (MessageModel) values[5];
					
					iv.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent i = new Intent();
							i.setAction(Intent.ACTION_MAIN);
							i.setClass(mContext, ImageActivity.class);
							i.putExtra("model", finalMsg);
							i.putExtra("defaultId", finalId);

							if (DEBUG) {
								Log.d(TAG, "defaultId = " + finalId);

							}

							mContext.startActivity(i);
						}
						});
					
					break;
				case 2:
					MessageModel msg = (MessageModel) values[2];
					
					LinearLayout container = ((ViewHolder) v.getTag()).getContainer();

					int numChilds = msg.hasMultiplePictures() ? msg.pic_urls.size() : 1;

					for (int i = numChilds; i < 9; i++) {
						container.getChildAt(i).setVisibility(View.GONE);
					}
					
					break;
			}
			
		}

		
	}
	
	private class DeleteTask extends AsyncTask<CommentModel, Void, Void> {
		private ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(mContext);
			prog.setMessage(mContext.getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(CommentModel[] params) {
			NewCommentApi.deleteComment(params[0].id);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();
		}
	}
	
	private static class ViewHolder {
		public MessageModel msg;
		public CharSequence span;
		
		private TextView date, retweets, comments, name, from, content;
		private HorizontalScrollView scroll;
		private LinearLayout container, originParent;
		private View comment_and_retweet;
		private ImageView weibo_avatar;
		private View v;
		
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
		
		public HorizontalScrollView getScroll() {
			if (scroll == null) {
				scroll = (HorizontalScrollView) v.findViewById(R.id.weibo_pics_scroll);
			}
			
			return scroll;
		}
		
		public LinearLayout getContainer() {
			if (container == null) {
				container = (LinearLayout) getScroll().findViewById(R.id.weibo_pics);
			}
			
			return container;
		}
		
		public LinearLayout getOriginParent() {
			if (originParent == null) {
				originParent = (LinearLayout) v.findViewById(R.id.weibo_origin);
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
		
		public CharSequence getSpan() {
			if (span == null) {
				span = SpannableStringUtils.span(msg.text);
			}
			
			return span;
		}
	}

}
