/*
 * Copyright (C) 2015 Peter Cai
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
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import info.papdt.blacklight.api.shorturl.ShortUrlApi;
import info.papdt.blacklight.api.statuses.QueryIdApi;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.ui.statuses.SingleActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

public class WeiboUrlUtility
{
	public static final List<String> WEIBO_DOMAIN = Arrays.asList(
		"t.cn",
		"weibo.com",
		"www.weibo.com",
		"weibo.cn",
		"www.weibo.cn"
	);

	private Uri mUri;

	public WeiboUrlUtility(Uri uri) {
		mUri = uri;
	}

	public boolean isShortUrl() {
		return "http".equals(mUri.getScheme()) && "t.cn".equals(mUri.getHost());
	}

	public boolean isWeiboUrl() {
		return "http".equals(mUri.getScheme()) && WEIBO_DOMAIN.indexOf(mUri.getHost()) >= 0;
	}

	public Intent getIntent(Context context) {
		Intent intent = null;
		if (isShortUrl()) {
			mUri = ShortUrlApi.expand(mUri);
		}
		// parsed weibo url
		if (!isShortUrl() && isWeiboUrl()) {
			intent = getWeiboIntent(context);
		}

		if (null == intent) {
			// forcing an app chooser
			Intent viewIntent = new Intent(Intent.ACTION_VIEW, mUri);
			intent = Intent.createChooser(viewIntent, mUri.toString());
		}
		return intent;
	}

	private Intent getWeiboIntent(Context context) {
		Intent intent = null;
		List<String> paths = mUri.getPathSegments();
		int size = paths.size();

		// http://weibo.com/n/username
		if (2 == size && "n".equals(paths.get(0))) {
			intent = getUserIntent(context, null, paths.get(1));
			if (null != intent)
				return intent;
		}
		// http://weibo.com/u/uid
		if (2 == size && "u".equals(paths.get(0))) {
			intent = getUserIntent(context, paths.get(1), null);
			if (null != intent)
				return intent;
		}
		// http://weibo.com/uid/Base62MessageID
		if (2 == size && TextUtils.isDigitsOnly(paths.get(0))) {
			intent = getStatusIntent(context, paths.get(1));
			if (null != intent)
				return intent;
		}
		// TODO: parse more url, i.e. http://photo.weibo.com/h5/repost/reppic_id/PIC_ID
		// TODO: How long on earth have you ignored that here is an 'TODO'?
		return null;
	}

	public static Intent getUserIntent(Context context, String uid, String name) {
		UserApiCache api = new UserApiCache(context);
		UserModel user = null;

		if (!TextUtils.isEmpty(uid)) {
			user = api.getUser(uid);
		}
		if (null == user && !TextUtils.isEmpty(name)) {
			user = api.getUserByName(name);
		}

		if (null != user && !TextUtils.isEmpty(user.id)) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.setClass(context, UserTimeLineActivity.class);
			intent.putExtra("user", user);
			return intent;
		}
		return null;
	}

	public static Intent getStatusIntent(Context context, String mid) {
		MessageModel msg = QueryIdApi.fetchStatus(mid);
		if (null != msg && !TextUtils.isEmpty(msg.text)) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(context, SingleActivity.class);
			intent.putExtra("msg", msg);
			return intent;
		}
		return null;
	}

	public void view(Context context) {
		new UrlParseTask(context).execute(mUri);
	}

	private class UrlParseTask extends AsyncTask<Uri, Void, Intent> {
		Context mContext;
		UrlParseTask(Context context) {
			mContext = context;
		}

		@Override
		protected Intent doInBackground(Uri... uris) {
			WeiboUrlUtility parser = new WeiboUrlUtility(uris[0]);
			return parser.getIntent(mContext);
		}

		@Override
		protected void onPostExecute(Intent intent) {
			if (null != intent) {
				mContext.startActivity(intent);
			}
		}
	}
}
