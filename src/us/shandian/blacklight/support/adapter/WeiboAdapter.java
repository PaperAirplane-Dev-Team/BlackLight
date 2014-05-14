package us.shandian.blacklight.support.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;

import java.util.HashMap;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.ui.common.ImageActivity;
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
		mGray = context.getResources().getColor(R.color.light_gray);
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
		
		// If not inflated before, then we have much work to do
		if (!existed) {
			v = mInflater.inflate(sub ? R.layout.weibo_content : R.layout.weibo, null);
			v.setTag(msg);
			TextView name = (TextView) v.findViewById(R.id.weibo_name);
			TextView from = (TextView) v.findViewById(R.id.weibo_from);
			TextView content = (TextView) v.findViewById(R.id.weibo_content);
			HorizontalScrollView scroll = (HorizontalScrollView) v.findViewById(R.id.weibo_pics_scroll);
			
			name.setText(msg.user != null ? msg.user.getName() : "");
			from.setText(Html.fromHtml(msg.source).toString());
			content.setText(SpannableStringUtils.span(msg.text));
			content.setMovementMethod(LinkMovementMethod.getInstance());
			
			if (msg.thumbnail_pic != null || msg.pic_urls.size() > 0) {
				scroll.setVisibility(View.VISIBLE);
				
				LinearLayout container = (LinearLayout) scroll.findViewById(R.id.weibo_pics);
				
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
					LinearLayout originParent = (LinearLayout) v.findViewById(R.id.weibo_origin);
					originParent.addView(origin);
					originParent.setVisibility(View.VISIBLE);
					
					if (msg instanceof CommentModel) {
						origin.findViewById(R.id.weibo_comment_and_retweet).setVisibility(View.GONE);
					}
					
				}
			}
			
			if (mBindOrig) {
				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MessageModel msg = (MessageModel) v.getTag();
						if (msg != null) {
							if (!(msg instanceof CommentModel)) {
								Intent i = new Intent();
								i.setAction(Intent.ACTION_MAIN);
								i.setClass(mContext, SingleActivity.class);
								i.putExtra("msg", msg);
								mContext.startActivity(i);
							}
						}
					}
				});
			}

			new ImageDownloader().execute(new Object[]{v, msg});
			
			if (!sub) {
				mViews.put(msg.id, v);
			}
			
		}
		
		// Even if inflated before, we still have to update these info
		TextView date = (TextView) v.findViewById(R.id.weibo_date);
		TextView retweet = (TextView) v.findViewById(R.id.weibo_retweet);
		TextView comments = (TextView) v.findViewById(R.id.weibo_comments);
		
		date.setText(mTimeUtils.buildTimeString(msg.created_at));
		
		if (!mShowCommentStatus || msg instanceof CommentModel) {
			if (!existed) {
				v.findViewById(R.id.weibo_comment_and_retweet).setVisibility(View.GONE);
			}
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
			LinearLayout originParent = (LinearLayout) v.findViewById(R.id.weibo_origin);
			
			date = (TextView) originParent.findViewById(R.id.weibo_date);
			retweet = (TextView) originParent.findViewById(R.id.weibo_retweet);
			comments = (TextView) originParent.findViewById(R.id.weibo_comments);

			date.setText(mTimeUtils.buildTimeString(origMsg.created_at));
			retweet.setText(String.valueOf(origMsg.reposts_count));
			comments.setText(String.valueOf(origMsg.comments_count));
		}
		
		return v;
	}
	
	// Downloads images including avatars
	private class ImageDownloader extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object[] params) {
			View v = (View) params[0];
			MessageModel msg = (MessageModel) params[1];
			
			// Avatars
			if (v != null) {
				Bitmap avatar = mUserApi.getSmallAvatar(msg.user);
				publishProgress(new Object[]{0, avatar, v, msg});
			}
			
			// Images
			if (v != null && !(msg instanceof CommentModel)) {
				HorizontalScrollView scroll = (HorizontalScrollView) v.findViewById(R.id.weibo_pics_scroll);
				
				if (scroll.getVisibility() == View.VISIBLE) {
					LinearLayout container = (LinearLayout) scroll.findViewById(R.id.weibo_pics);
					
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
			
			switch ((int) values[0]) {
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

}
