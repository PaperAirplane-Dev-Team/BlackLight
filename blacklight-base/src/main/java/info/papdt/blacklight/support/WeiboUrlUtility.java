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

import info.papdt.blacklight.api.shorturl.ShortUrlApi;

import java.util.Arrays;
import java.util.List;

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
			intent = new Intent(Intent.ACTION_VIEW, mUri);
		}
		return intent;
	}

	private Intent getWeiboIntent(Context context) {
		return null;
	}
}
