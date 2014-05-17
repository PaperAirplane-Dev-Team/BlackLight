package us.shandian.blacklight.api.comments;

import com.sina.weibo.sdk.net.WeiboParameters;

import com.google.gson.Gson;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.CommentModel;

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
