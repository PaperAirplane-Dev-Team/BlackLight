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

package info.papdt.blacklight.api.shorturl;

import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONObject;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.support.http.WeiboParameters;

public class ShortUrlApi extends BaseApi {
	public static Uri shorten(Uri uri) {
		String url = shorten(uri.toString());
		if (TextUtils.isEmpty(url))
			return null;
		return Uri.parse(url);
	}

	public static String shorten(String url) {
		WeiboParameters params = new WeiboParameters();
		params.put("url_long", url);

		try {
			JSONObject json = request(Constants.SHORT_URL_SHORTEN, params, HTTP_GET);
			JSONObject url_info = json.getJSONArray("urls").getJSONObject(0);
			if (url_info.getBoolean("result")) {
				return url_info.getString("url_short");
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static Uri expand(Uri uri) {
		String url = expand(uri.toString());
		if (TextUtils.isEmpty(url))
			return null;
		return Uri.parse(url);
	}

	public static String expand(String url) {
		WeiboParameters params = new WeiboParameters();
		params.put("url_short", url);

		try {
			JSONObject json = request(Constants.SHORT_URL_EXPAND, params, HTTP_GET);
			JSONObject url_info = json.getJSONArray("urls").getJSONObject(0);
			if (url_info.getBoolean("result")) {
				return url_info.getString("url_long");
			}
		} catch (Exception e) {
		}
		return url;
	}
}
