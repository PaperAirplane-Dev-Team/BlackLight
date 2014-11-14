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

package us.shandian.blacklight.api.comments;

import com.google.gson.Gson;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.support.http.WeiboParameters;

public class NewCommentApi extends BaseApi
{
	public static boolean commentOn(long id, String comment, boolean commentOrig) {
		WeiboParameters params = new WeiboParameters();
		params.put("comment", comment);
		params.put("id", id);
		params.put("comment_ori", commentOrig ? 1 : 0);

		try {
			JSONObject json = request(Constants.COMMENTS_CREATE, params, HTTP_POST);
			CommentModel msg = new Gson().fromJson(json.toString(), CommentModel.class);

			if (msg == null || msg.id <= 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	public static boolean replyTo(long id, long cid, String comment, boolean commentOrig) {
		WeiboParameters params = new WeiboParameters();
		params.put("comment", comment);
		params.put("id", id);
		params.put("cid", cid);
		params.put("comment_ori", commentOrig ? 1 : 0);

		try {
			JSONObject json = request(Constants.COMMENTS_REPLY, params, HTTP_POST);
			CommentModel msg = new Gson().fromJson(json.toString(), CommentModel.class);

			if (msg == null || msg.id <= 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	public static void deleteComment(long cid) {
		WeiboParameters params = new WeiboParameters();
		params.put("cid", cid);
		
		try {
			request(Constants.COMMENTS_DESTROY, params, HTTP_POST);
		} catch (Exception e) {
			
		}
	}
}
