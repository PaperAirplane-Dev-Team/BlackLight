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

package us.shandian.blacklight.api.login;

import android.util.Log;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

/* BlackMagic Login Api */

public class LoginApi extends BaseApi {
	private static final String TAG = LoginApi.class.getSimpleName();

	// Returns token and expire date
	public static String[] login(String appId, String appSecret, String username, String passwd) {
		WeiboParameters params = new WeiboParameters();
		params.put("username", username);
		params.put("password", passwd);
		params.put("client_id", appId);
		params.put("client_secret", appSecret);
		params.put("grant_type", "password");

		try {
			JSONObject json = requestWithoutAccessToken(Constants.OAUTH2_ACCESS_TOKEN, params, HTTP_POST);
			return new String[]{json.optString("access_token"), json.optString("expires_in")};
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "login error:" + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
