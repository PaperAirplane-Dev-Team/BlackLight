package us.shandian.blacklight.api;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

public abstract class BaseApi
{
	private static final String TAG = BaseApi.class.getSimpleName();
	
	// Http Methods
	protected static final String HTTP_GET = "GET";
	protected static final String HTTP_POST = "POST";
	
	// Access Token
	private static String mAccessToken;
	private static String mBMAccessToken;
	
	protected static JSONObject BMRequest(String url, WeiboParameters params, String method) throws JSONException {
		return request(mBMAccessToken, url, params, method);
	}
	
	protected static JSONObject request(String url, WeiboParameters params, String method) throws JSONException {
		return request(mAccessToken, url, params, method);
	}
	
	protected static JSONObject request(String token, String url, WeiboParameters params, String method) throws JSONException {
		if (token == null) {
			return null;
		} else {
			params.put("access_token", token);
			String jsonData = AsyncWeiboRunner.request(url, params, method);
			
			if (DEBUG) {
				Log.d(TAG, "jsonData = " + jsonData);
			}
			
			if (jsonData != null && jsonData.contains("{")) {
				return new JSONObject(jsonData);
			} else {
				return null;
			}
		}
	}
	
	protected static JSONObject requestWithoutAccessToken(String url, WeiboParameters params, String method) throws JSONException {
		String jsonData = AsyncWeiboRunner.request(url, params, method);
		
		if (DEBUG) {
			Log.d(TAG, "jsonData = " + jsonData);
		}
		
		if (jsonData != null && jsonData.contains("{")) {
			return new JSONObject(jsonData);
		} else {
			return null;
		}
	}
	
	public static String getAccessToken() {
		return mAccessToken;
	}
	
	public static void setAccessToken(String token) {
		mAccessToken = token;
	}
	
	public static void setBMAccessToken(String token) {
		mBMAccessToken = token;
	}
	
	public static boolean hasBlackMagic() {
		return mBMAccessToken != null;
	}
}
