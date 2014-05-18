package us.shandian.blacklight.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;

import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class WeiboSpan extends ClickableSpan
{
	private static String TAG = WeiboSpan.class.getSimpleName();
	
	private String mUrl;
	private Uri mUri;
	
	public WeiboSpan(String url) {
		mUrl = url;
		mUri = Uri.parse(mUrl);
	}
	
	public String getURL() {
		return mUrl;
	}
	
	@Override
	public void onClick(View v) {
		Context context = v.getContext();
		
		if (mUri.getScheme().startsWith("http")) {
			// TODO View some weibo pages inside app
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(mUri);
			context.startActivity(i);
		} else {
			if (mUri.getScheme().startsWith("us.shandian.blacklight.user")) {
				String name = mUrl.substring(mUrl.lastIndexOf("@") + 1, mUrl.length());
				
				if (DEBUG) {
					Log.d(TAG, "Mention user link detected: " + name);
				}
				
				new UserInfoTask().execute(context, name);
			}
		}
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(ds.linkColor);
		ds.setUnderlineText(false);
	}
	
	private class UserInfoTask extends AsyncTask<Object, Void, Object[]> {

		@Override
		protected Object[] doInBackground(Object[] params) {
			// Detect user info in background
			return new Object[]{params[0],
				new UserApiCache((Context) params[0]).getUserByName((String) params[1])};
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			
			Context context = (Context) result[0];
			UserModel usr = (UserModel) result[1];
			
			if (usr != null && usr.id != null & !usr.id.trim().equals("")) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, UserTimeLineActivity.class);
				i.putExtra("user", usr);
				context.startActivity(i);
			}
		}
	}

}
