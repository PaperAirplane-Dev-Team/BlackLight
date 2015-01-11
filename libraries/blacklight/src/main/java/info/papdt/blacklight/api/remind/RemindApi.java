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

package info.papdt.blacklight.api.remind;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.model.UnreadModel;
import info.papdt.blacklight.support.http.WeiboParameters;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class RemindApi extends BaseApi {
	private static final String TAG = RemindApi.class.getSimpleName();

	public enum Type {
		Follower("follower"),
		Cmt("cmt"),
		Dm("dm"),
		Mention_Status("mention_status"),
		Mention_Cmt("mention_cmt"),
		Group("group"),
		Notice("notice"),
		Invite("invite"),
		Badge("badge"),
		Photo("photo");

		public String str;

		Type(String str) {
			this.str = str;
		}
	}

	public static UnreadModel getUnread(String uid) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("unread_message", 0);

		try {
			JSONObject json = request(Constants.REMIND_UNREAD_COUNT, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), UnreadModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Failed to get unread count");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		return null;
	}

	public static void clearUnread(String type) {
		WeiboParameters params = new WeiboParameters();
		params.put("type", type);

		try {
			request(Constants.REMIND_UNREAD_SET_COUNT, params, HTTP_POST);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot clear unread count");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}
}
