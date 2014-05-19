package us.shandian.blacklight.api.search;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/*
  Searches for statuses with an exact topic
*/
public class TopicsApi extends BaseApi
{
	private static final String TAG = TopicsApi.class.getSimpleName();

	public static MessageListModel searchTopic(String q, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("q", q);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = BMRequest(Constants.SEARCH_TOPICS, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot search, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
