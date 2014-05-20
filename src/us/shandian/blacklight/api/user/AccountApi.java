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

import org.json.JSONObject;

import com.sina.weibo.sdk.net.WeiboParameters;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;

/* Current Account Api of Sina Weibo */
public class AccountApi extends BaseApi
{
	public static String getUid() {
		try {
			JSONObject json = request(Constants.GET_UID, new WeiboParameters(), HTTP_GET);
			return json.optString("uid");
		} catch (Exception e) {
			return null;
		}
	}
}
