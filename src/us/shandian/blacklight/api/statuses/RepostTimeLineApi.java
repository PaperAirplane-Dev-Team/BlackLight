package us.shandian.blacklight.api.statuses;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.RepostListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class RepostTimeLineApi extends BaseApi
{
	private static final String TAG = RepostTimeLineApi.class.getSimpleName();

	public static RepostListModel fetchRepostTimeLine(long msgId, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("id", msgId);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.REPOST_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), RepostListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch repost timeline, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
