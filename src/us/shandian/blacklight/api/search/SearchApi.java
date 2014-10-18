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

package us.shandian.blacklight.api.search;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

public class SearchApi extends BaseApi {
	public static final String TAG = SearchApi.class.getSimpleName();

	public static MessageListModel searchStatus(String q, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("q", q);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.SEARCH_STATUSES, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot search, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}

	public static UserListModel searchUser(String q, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("q", q);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.SEARCH_USERS, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), UserListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot search, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}

	public static ArrayList<String> suggestAtUser(String q, int count) {
		WeiboParameters params = new WeiboParameters();
		params.put("q", q);
		params.put("count", count);
		params.put("type", 0);
		params.put("range", 0);

		try {
			JSONArray json = requestArray(Constants.SEARCH_SUGGESTIONS_AT_USERS, params, HTTP_GET);
			ArrayList<String> ret = new ArrayList<String>();

			for (int i = 0; i < json.length(); i++) {
				ret.add(json.getJSONObject(i).optString("nickname"));
			}

			return ret;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Cannot search, " + e.getClass().getSimpleName());
				Log.e(TAG, Log.getStackTraceString(e));
			}
			return null;
		}
	}
}
