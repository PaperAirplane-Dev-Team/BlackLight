package us.shandian.blacklight.api.favorites;

import android.util.Log;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.FavListModel;
import us.shandian.blacklight.model.MessageListModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class FavListApi extends BaseApi
{
	private static final String TAG = FavListApi.class.getSimpleName();

	public static MessageListModel fetchFavList(int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.FAVORITES_LIST, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), FavListModel.class).toMsgList();
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch fav list, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
