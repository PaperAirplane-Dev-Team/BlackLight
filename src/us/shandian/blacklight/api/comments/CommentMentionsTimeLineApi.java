package us.shandian.blacklight.api.comments;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.CommentListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class CommentMentionsTimeLineApi extends BaseApi
{
	private static String TAG = CommentMentionsTimeLineApi.class.getSimpleName();

	public static CommentListModel fetchCommentMentionsTimeLine(int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.COMMENTS_MENTIONS, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), CommentListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch mentions timeline, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
