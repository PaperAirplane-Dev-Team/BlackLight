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

package info.papdt.blacklight.api.statuses;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONObject;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.support.http.WeiboParameters;

/* Fetches messages published by an exact user */
public class QueryIdApi extends BaseApi
{
	public static final int TYPE_STATUS = 1;
	public static final int TYPE_COMMENT = 2;
	public static final int TYPE_DIRECT_MESSAGE = 3;

	public static String queryId(String mid) {
		return queryId(mid, TYPE_STATUS);
	}

	public static String queryId(String mid, int type) {
		WeiboParameters params = new WeiboParameters();
		params.put("mid", mid);
		params.put("type", type);
		params.put("isBase62", TextUtils.isDigitsOnly(mid) ? 0 : 1);

		try {
			JSONObject json = request(Constants.QUERY_ID, params, HTTP_GET);
			return json.optString("id", null);
		} catch (Exception e) {
		}
		return null;
	}

	public static String queryMid(String id) {
		return queryMid(id, TYPE_STATUS);
	}

	public static String queryMid(String id, int type) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", id);
		params.put("type", type);

		try {
			JSONObject json = request(Constants.QUERY_MID, params, HTTP_GET);
			return json.optString("mid", null);
		} catch (Exception e) {
		}
		return null;
	}

	public static MessageModel fetchStatus(String mid) {
		String id = mid;
		if (!TextUtils.isDigitsOnly(mid)) {
			id = queryId(mid);
		}
		if (TextUtils.isEmpty(id)) {
			return null;
		}

		WeiboParameters params = new WeiboParameters();
		params.put("id", id);

		try {
			JSONObject json = request(Constants.SHOW, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageModel.class);
		} catch (Exception e) {
		}
		return null;
	}
}
