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
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.support.http.WeiboParameters;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class GroupsApi extends BaseApi {
	private static final String TAG = GroupsApi.class.getSimpleName();

	public static GroupListModel getGroups() {
		WeiboParameters params = new WeiboParameters();
		
		try {
			JSONObject json = request(Constants.FRIENDSHIPS_GROUPS, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), GroupListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot get groups");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		return null;
	}

	public static boolean isMember(String uid, String groupId) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("list_id", groupId);
		
		try {
			JSONObject json = request(Constants.FRIENDSHIPS_GROUPS_IS_MEMBER, params, HTTP_GET);
			return json.optBoolean("lists");
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot determine if user is a member");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		return false;
	}

	public static MessageListModel fetchGroupTimeLine(String groupId, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("list_id", groupId);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.FRIENDSHIPS_GROUPS_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot get group timeline");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		return null;
	}

	public static void addMemberToGroup(String uid, String groupId) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("list_id", groupId);

		try {
			request(Constants.FRIENDSHIPS_GROUPS_MEMBERS_ADD, params, HTTP_POST);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot add user to group");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}

	public static void removeMemberFromGroup(String uid, String groupId) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("list_id", groupId);

		try {
			request(Constants.FRIENDSHIPS_GROUPS_MEMBERS_DESTROY, params, HTTP_POST);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot remove user from group");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}

	public static void createGroup(String name) {
		WeiboParameters params = new WeiboParameters();
		params.put("name", name);

		try {
			request(Constants.FRIENDSHIPS_GROUPS_CREATE, params, HTTP_POST);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot create group");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}

	public static void destroyGroup(String groupId) {
		WeiboParameters params = new WeiboParameters();
		params.put("list_id", groupId);

		try {
			request(Constants.FRIENDSHIPS_GROUPS_DESTROY, params, HTTP_POST);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot destroy group");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}
}
