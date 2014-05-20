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

package us.shandian.blacklight.api.user;

import android.util.Log;

import org.json.JSONObject;

import com.google.gson.Gson;

import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.http.WeiboParameters;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* Apis to read / write user info */
public class UserApi extends BaseApi
{
	private static String TAG = UserApi.class.getSimpleName();
	
	public static UserModel getUser(String uid) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		
		try {
			JSONObject json = request(Constants.USER_SHOW, params, HTTP_GET);
			UserModel user = new Gson().fromJson(json.toString(), UserModel.class);
			return user;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Failed to fetch user info from net: " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
	
	public static UserModel getUserByName(String name) {
		WeiboParameters params = new WeiboParameters();
		params.put("screen_name", name);

		try {
			JSONObject json = request(Constants.USER_SHOW, params, HTTP_GET);
			UserModel user = new Gson().fromJson(json.toString(), UserModel.class);
			return user;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Failed to fetch user info from net: " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
