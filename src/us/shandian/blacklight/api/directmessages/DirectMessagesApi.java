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

package us.shandian.blacklight.api.directmessages;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.DirectMessageListModel;
import us.shandian.blacklight.model.DirectMessageUserListModel;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

public class DirectMessagesApi extends BaseApi
{
	private static final String TAG = DirectMessagesApi.class.getSimpleName();
	
	public static DirectMessageUserListModel getUserList(int count, int cursor) {
		WeiboParameters params = new WeiboParameters();
		params.put("count", count);
		params.put("cursor", cursor);
		
		try {
			JSONObject json = request(Constants.DIRECT_MESSAGES_USER_LIST, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), DirectMessageUserListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		return null;
	}
	
	public static DirectMessageListModel getConversation(String uid, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("count", count);
		params.put("page", page);
		
		try {
			JSONObject json = request(Constants.DIRECT_MESSAGES_CONVERSATION, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), DirectMessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, Log.getStackTraceString(e));
			}
		}
		
		return null;
	}
	
	public static boolean send(String uid, String text) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("text", text);
		
		try {
			request(Constants.DIRECT_MESSAGES_SEND, params, HTTP_POST);
			return true;
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, Log.getStackTraceString(e));
			}
		}
		
		return false;
	}
}
