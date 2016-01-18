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

package info.papdt.blacklight.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.ui.search.TopicsActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

import static info.papdt.blacklight.BuildConfig.DEBUG;

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
			new WeiboUrlUtility(mUri).view(context);
		} else {
			if (mUri.getScheme().startsWith("us.shandian.blacklight.user")) {
				String name = mUrl.substring(mUrl.lastIndexOf("@") + 1, mUrl.length());
				
				if (DEBUG) {
					Log.d(TAG, "Mention user link detected: " + name);
				}
				
				new UserInfoTask().execute(context, name);
			} else if (mUri.getScheme().startsWith("us.shandian.blacklight.topic")) {
				String name = mUrl.substring(mUrl.indexOf("#") + 1, mUrl.lastIndexOf("#"));
				
				// Start Activity
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, TopicsActivity.class);
				i.putExtra("topic", name);
				context.startActivity(i);
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
		protected Object[] doInBackground(Object... params) {
			// Detect user info in background
			return new Object[]{params[0],
				new UserApiCache((Context) params[0]).getUserByName((String) params[1])};
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			
			Context context = (Context) result[0];
			UserModel usr = (UserModel) result[1];
			
			if (usr != null && usr.id != null && !usr.id.trim().equals("")) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, UserTimeLineActivity.class);
				i.putExtra("user", usr);
				context.startActivity(i);
			}
		}
	}

}
