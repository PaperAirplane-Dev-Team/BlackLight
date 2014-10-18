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

package us.shandian.blacklight.api.statuses;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.support.Emoticons.SinaEmotion;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

public class PostApi extends BaseApi
{
	public static final String TAG = PostApi.class.getSimpleName();

	public static final int EXTRA_NONE = 0;
	public static final int EXTRA_COMMENT = 1;
	public static final int EXTRA_COMMENT_ORIG = 2;
	public static final int EXTRA_ALL = 3;
	
	public static boolean newPost(String status) {
		WeiboParameters params = new WeiboParameters();
		params.put("status", status);
		
		try {
			JSONObject json = request(Constants.UPDATE, params, HTTP_POST);
			MessageModel msg = new Gson().fromJson(json.toString(), MessageModel.class);
			
			if (msg == null || msg.idstr == null || msg.idstr.trim().equals("")) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	// Picture size must be smaller than 5M
	public static boolean newPostWithPic(String status, Bitmap pic) {
		WeiboParameters params = new WeiboParameters();
		params.put("status", status);
		params.put("pic", pic);

		try {
			JSONObject json = request(Constants.UPLOAD, params, HTTP_POST);
			MessageModel msg = new Gson().fromJson(json.toString(), MessageModel.class);

			if (msg == null || msg.idstr == null || msg.idstr.trim().equals("")) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	public static boolean newRepost(long id, String status, int extra) {
		WeiboParameters params = new WeiboParameters();
		params.put("status", status);
		params.put("id", id);
		params.put("is_comment", extra);

		try {
			JSONObject json = request(Constants.REPOST, params, HTTP_POST);
			MessageModel msg = new Gson().fromJson(json.toString(), MessageModel.class);

			if (msg == null || msg.idstr == null || msg.idstr.trim().equals("")) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	// Status destroyer
	public static void deletePost(long id) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", id);
		
		try {
			request(Constants.DESTROY, params, HTTP_POST);
		} catch (Exception e) {
			// Nothing can be done
		}
	}
	
	// Add to favorite
	public static void fav(long id) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", id);
		
		try {
			request(Constants.FAVORITES_CREATE, params, HTTP_POST);
		} catch (Exception e) {
			
		}
	}
	
	// Remove from favorite
	public static void unfav(long id) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", id);
		
		try {
			request(Constants.FAVORITES_DESTROY, params, HTTP_POST);
		} catch (Exception e) {
			
		}
	}

	// Upload pictures
	public static String uploadPicture(Bitmap picture) {
		WeiboParameters params = new WeiboParameters();
		params.put("pic", picture);

		try {
			JSONObject json = request(Constants.UPLOAD_PIC, params, HTTP_POST);
			return json.optString("pic_id");
		} catch (Exception e) {
			return null;
		}
	}

	// Post with multi pictures
	// @param pics: ids returned by uploadPicture, split with ","
	public static boolean newPostWithMultiPics(String status, String pics) {
		WeiboParameters params = new WeiboParameters();
		params.put("status", status);
		params.put("pic_id", pics);

		try {
			JSONObject json = request(Constants.UPLOAD_URL_TEXT, params, HTTP_POST);
			MessageModel msg = new Gson().fromJson(json.toString(), MessageModel.class);
			if (msg == null || msg.idstr == null || msg.idstr.trim().equals("")) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public static ArrayList<SinaEmotion> getEmoticons(String type) {
		WeiboParameters params = new WeiboParameters();
		params.put("type", type);

		try {
			String json = requestString(Constants.EMOTIONS, params, HTTP_GET);
			ArrayList<SinaEmotion> emo = new Gson().fromJson(json, new TypeToken<ArrayList<SinaEmotion>>(){}.getType());

			if (DEBUG) {
				Log.d(TAG, "json = " + json);
			}

			return emo;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
			return null;
		}
	}
}
