package us.shandian.blacklight.api.user;

import android.util.Log;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.model.UserModel;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* Apis to read / write user info */
public class UserApi extends BaseApi
{
	private static String TAG = UserApi.class.getSimpleName();
	
	public static UserModel getUser(String uid) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		
		try {
			JSONObject json = request(Constants.USER_SHOW, params, HTTP_GET);
			UserModel user = new Gson().fromJson(json.toString(), UserModel.class);
			return user;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Failed to fetch user info from net: " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
