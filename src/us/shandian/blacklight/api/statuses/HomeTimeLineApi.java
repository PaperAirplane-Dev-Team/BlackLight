package us.shandian.blacklight.api.statuses;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* Fetches a Home Timeline */
public class HomeTimeLineApi extends BaseApi
{
	private static final String TAG = HomeTimeLineApi.class.getSimpleName();
	
	public static MessageListModel fetchHomeTimeLine(int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("count", count);
		params.put("page", page);
		
		try {
			JSONObject json = request(Constants.HOME_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch home timeline, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
