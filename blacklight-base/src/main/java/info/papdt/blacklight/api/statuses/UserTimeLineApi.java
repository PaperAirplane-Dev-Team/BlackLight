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

import com.google.gson.Gson;

import org.json.JSONObject;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.support.http.WeiboParameters;

/* Fetches messages published by an exact user */
public class UserTimeLineApi extends BaseApi
{
	public static MessageListModel fetchUserTimeLine(String uid, int count, int page, boolean orig) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("count", count);
		params.put("page", page);
		params.put("feature", orig?1:0);

		try {
			JSONObject json = request(Constants.USER_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			return null;
		}
	}
}
