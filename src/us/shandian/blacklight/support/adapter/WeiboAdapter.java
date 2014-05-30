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
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Html;
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
public class WeiboAdapter extends BaseAdapter
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
	
	private HashMap<Long, View> mViews = new HashMap<Long, View>();
	
	public WeiboAdapter(Context context, MessageListModel list, boolean bindOrig, boolean showCommentStatus) {
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
			return bindView(msg, false);
		}
	}
	
	private View bindView(final MessageModel msg, boolean sub) {
		boolean existed = false;
		View v = null;
		if (!sub) {
			if (mViews.containsKey(msg.id)) {
				v = mViews.get(msg.id);
				existed = true;
			}
		}
		
		ViewHolder h = null;
		
		// If not inflated before, then we have much work to do
		if (!existed) {
			v = mInflater.inflate(sub ? R.layout.weibo_content : R.layout.weibo, null);
			h = new ViewHolder(v, msg);
		} else {
			h = (ViewHolder) v.getTag();
		}
		
		if (!existed) {
			TextView name = h.getName();
			TextView from = h.getFrom();
			TextView content = h.getContent();
			HorizontalScrollView scroll = h.getScroll();
			
			name.setText(msg.user != null ? msg.user.getName() : "");
			from.setText(msg.source != null ? Html.fromHtml(msg.source).toString() : "");
			content.setText(SpannableStringUtils.span(msg.text));
			content.setMovementMethod(HackyMovementMethod.getInstance());
			
			if (msg.thumbnail_pic != null || msg.pic_urls.size() > 0) {
				scroll.setVisibility(View.VISIBLE);
				
				LinearLayout container = h.getContainer();
				
				int numChilds = msg.hasMultiplePictures() ? msg.pic_urls.size() : 1;
				
				for (int i = 0; i < numChilds; i++) {
					View c = mInflater.inflate(R.layout.weibo_pic, container);
					
				}
			}
			
			// If this retweets/repies to others, show the original
			if (!sub && mBindOrig) {
				View origin = null;
				if (!(msg instanceof CommentModel) && msg.retweeted_status != null) {
					origin = bindView(msg.retweeted_status, true);
				} else if (msg instanceof CommentModel) {
					CommentModel comment = (CommentModel) msg;
					if (comment.reply_comment != null) {
						origin = bindView(comment.reply_comment, true);
					} else if (comment.status != null) {
						origin = bindView(comment.status, true);
					}
				}
				
				if (origin != null) {
					origin.setBackgroundColor(mGray);
					LinearLayout originParent = h.getOriginParent();
					originParent.addView(origin);
					originParent.setVisibility(View.VISIBLE);
					
					if (msg instanceof CommentModel) {
						h.getCommentAndRetweet().setVisibility(View.GONE);
					}
					
				}
			}
			
			if (!(msg instanceof CommentModel)) {
				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MessageModel msg = ((ViewHolder) v.getTag()).msg;
						if (msg != null) {
							Intent i = new Intent();
							i.setAction(Intent.ACTION_MAIN);
							i.setClass(mContext, SingleActivity.class);
							i.putExtra("msg", msg);
							mContext.startActivity(i);
						}
					}
				});
			} else {
				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CommentModel comment = (CommentModel) ((ViewHolder) v.getTag()).msg;
						if (comment != null) {
							Intent i = new Intent();
							i.setAction(Intent.ACTION_MAIN);
							i.setClass(mContext, ReplyToActivity.class);
							i.putExtra("comment", comment);
							mContext.startActivity(i);
						}
					}		
				});
				
				CommentModel comment = (CommentModel) msg;
				if (comment.user.id.equals(mUid) || (comment.status != null && comment.status.user.id != null && comment.status.user.id.equals(mUid))) {
					v.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(final View v) {
							new AlertDialog.Builder(mContext)
									.setMessage(R.string.confirm_delete)
									.setCancelable(true)
									.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
											new DeleteTask().execute((CommentModel) ((ViewHolder) v.getTag()).msg);
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
					});
				}
			}

			new ImageDownloader().execute(new Object[]{v});
			
			if (!sub) {
				setAnimation(v);
				mViews.put(msg.id, v);
			}
			
		}
		
		// Even if inflated before, we still have to update these info
		TextView date = h.getDate();
		TextView retweet = h.getRetweets();
		TextView comments = h.getComments();
		
		date.setText(mTimeUtils.buildTimeString(msg.created_at));
		
		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.getCommentAndRetweet().setVisibility(View.GONE);
		} else {
			retweet.setText(String.valueOf(msg.reposts_count));
			comments.setText(String.valueOf(msg.comments_count));
		}
		
		// Update subview's info as well
		MessageModel origMsg = null;
		
		if (msg instanceof CommentModel) {
			origMsg = ((CommentModel) msg).status;
		} else {
			origMsg = msg.retweeted_status;
		}
		
		if (existed && mBindOrig && !sub && origMsg != null) {
			LinearLayout originParent = h.getOriginParent();
			ViewHolder originHolder = (ViewHolder) originParent.getChildAt(0).getTag();
			
			date = originHolder.getDate();
			retweet = originHolder.getRetweets();
			comments = originHolder.getComments();

			date.setText(mTimeUtils.buildTimeString(origMsg.created_at));
			retweet.setText(String.valueOf(origMsg.reposts_count));
			comments.setText(String.valueOf(origMsg.comments_count));
		}
		
		return v;
	}
	
	private void setAnimation(View v) {
		AlphaAnimation alpha = new AlphaAnimation(0.4f, 1.0f);
		TranslateAnimation translate = new TranslateAnimation(0.0f, 0.0f, 50.0f, 0.0f);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(alpha);
		set.addAnimation(translate);
		set.setInterpolator(mContext, android.R.anim.accelerate_interpolator);
		set.setDuration(250);
		v.startAnimation(set);
	}
	
	public void notifyDataSetChangedAndClear() {
		super.notifyDataSetChanged();
		
		// Fix memory leak
		mViews.clear();
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
				Bitmap avatar = mUserApi.getSmallAvatar(msg.user);
				publishProgress(new Object[]{0, avatar, v, msg});
			}
			
			// Images
			if (v != null && !(msg instanceof CommentModel)) {
				HorizontalScrollView scroll = h.getScroll();
				
				if (scroll.getVisibility() == View.VISIBLE) {
					LinearLayout container = h.getContainer();
					
					for (int i = 0; i < container.getChildCount(); i++) {
						ImageView imgView = (ImageView) container.getChildAt(i);
						Bitmap img = mHomeApi.getThumbnailPic(msg, i);
						
						if (img != null) {
							publishProgress(new Object[]{1, img, imgView, i, msg});
						}
					}
				}
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			
			switch (Integer.parseInt(String.valueOf(values[0]))) {
				case 0:
					Bitmap avatar = (Bitmap) values[1];
					View v = (View) values[2];
					if (v != null) {
						ImageView iv = (ImageView) v.findViewById(R.id.weibo_avatar);
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
					Bitmap img = (Bitmap) values[1];
					ImageView iv = (ImageView) values[2];
					iv.setImageBitmap(img);
					
					final int finalId = values[3];
					final MessageModel finalMsg = (MessageModel) values[4];
					
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
	
	private class ViewHolder {
		public MessageModel msg;
		
		private TextView date, retweets, comments, name, from, content;
		private HorizontalScrollView scroll;
		private LinearLayout container, originParent;
		private View comment_and_retweet;
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
	}

}
