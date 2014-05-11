package us.shandian.blacklight.api.comments;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.CommentListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class StatusCommentApi extends BaseApi
{
	private static String TAG = StatusCommentApi.class.getSimpleName();

	public static CommentListModel fetchCommentOfStatus(long msgId, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", msgId);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.COMMENTS_SHOW, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), CommentListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch comments timeline, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
