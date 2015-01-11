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

package info.papdt.blacklight.api.friendships;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.model.UserListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.http.WeiboParameters;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class FriendsApi extends BaseApi
{
	private static final String TAG = FriendsApi.class.getSimpleName();
	
	private static UserListModel getUsers(String uid, int count, int cursor, String url) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("count", count);
		params.put("cursor", cursor);
		
		try {
			JSONObject json = request(url, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), UserListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot get friends");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		return null;
	}

    public static UserListModel getFriendsOf(String uid, int count, int cursor){
        return getUsers(uid, count, cursor, Constants.FRIENDSHIPS_FRIENDS);
    }

    public static UserListModel getFollowersOf(String uid, int count, int cursor){
        return getUsers(uid, count, cursor, Constants.FRIENDSHIPS_FOLLOWERS);
    }
	
	public static boolean follow(String uid) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		
		try {
			JSONObject json = request(Constants.FRIENDSHIPS_CREATE, params, HTTP_POST);
			UserModel user = new Gson().fromJson(json.toString(), UserModel.class);
			return user != null && !user.id.trim().equals("");
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean unfollow(String uid) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);

		try {
			JSONObject json = request(Constants.FRIENDSHIPS_DESTROY, params, HTTP_POST);
			UserModel user = new Gson().fromJson(json.toString(), UserModel.class);
			return user != null && !user.id.trim().equals("");
		} catch (Exception e) {
			return false;
		}
	}
}
