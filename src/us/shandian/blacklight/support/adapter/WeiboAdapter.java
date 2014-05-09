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
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.ui.common.ImageActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;

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
	
	private HashMap<Long, View> mViews = new HashMap<Long, View>();
	
	public WeiboAdapter(Context context, MessageListModel list) {
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTimeUtils = StatusTimeUtils.instance(context);
		mUserApi = new UserApiCache(context);
		mHomeApi = new HomeTimeLineApiCache(context);
		mGray = context.getResources().getColor(R.color.light_gray);
		mContext = context;
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
			
			TextView name = (TextView) v.findViewById(R.id.weibo_name);
			TextView from = (TextView) v.findViewById(R.id.weibo_from);
			TextView content = (TextView) v.findViewById(R.id.weibo_content);
			HorizontalScrollView scroll = (HorizontalScrollView) v.findViewById(R.id.weibo_pics_scroll);
			
			name.setText(msg.user.screen_name == null ? msg.user.name : msg.user.screen_name);
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
			
			// If this retweets others, show the original
			if (!sub && msg.retweeted_status != null) {
				View origin = bindView(msg.retweeted_status, true);
				origin.setBackgroundColor(mGray);
				LinearLayout originParent = (LinearLayout) v.findViewById(R.id.weibo_origin);
				originParent.addView(origin);
				originParent.setVisibility(View.VISIBLE);
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
		retweet.setText(String.valueOf(msg.reposts_count));
		comments.setText(String.valueOf(msg.comments_count));
		
		// Update subview's info as well
		if (existed && !sub && msg.retweeted_status != null) {
			LinearLayout originParent = (LinearLayout) v.findViewById(R.id.weibo_origin);
			
			date = (TextView) originParent.findViewById(R.id.weibo_date);
			retweet = (TextView) originParent.findViewById(R.id.weibo_retweet);
			comments = (TextView) originParent.findViewById(R.id.weibo_comments);

			date.setText(mTimeUtils.buildTimeString(msg.retweeted_status.created_at));
			retweet.setText(String.valueOf(msg.retweeted_status.reposts_count));
			comments.setText(String.valueOf(msg.retweeted_status.comments_count));
		}
		
		v.setTag(msg);
		
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
				publishProgress(new Object[]{0, avatar, v});
			}
			
			// Images
			if (v != null) {
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
