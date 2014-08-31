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

package us.shandian.blacklight.api.attitudes;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.support.http.WeiboParameters;

public class AttitudesApi extends BaseApi{

	public static boolean like(long id){
		WeiboParameters params = new WeiboParameters();
		params.put("attitude","smile");
		params.put("id",id);

		try{
			JSONObject json = request(Constants.ATTITUDE_CREATE,params,HTTP_POST);
			if (json.getString("attitude").equals("smile")){
				return true;
			}
		}catch (Exception e){
			return false;
		}
		return false;
	}

	public static boolean cancelLike(long id){
		WeiboParameters params = new WeiboParameters();
		params.put("id",id);

		try{
			JSONObject json = request(Constants.ATTITUDE_DESTROY,params,HTTP_POST);
			if(json.getBoolean("result")){
				return true;
			}
		}catch (Exception e){
			return false;
		}
		return false;
	}
}
