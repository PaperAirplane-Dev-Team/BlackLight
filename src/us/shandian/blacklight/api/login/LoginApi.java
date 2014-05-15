package us.shandian.blacklight.api.login;

import android.util.Log;

import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* BlackMagic Login Api */

public class LoginApi extends BaseApi
{
	private static final String TAG = LoginApi.class.getSimpleName();
	
	// Returns token and expire date
	public static String[] login(String appId, String appSecret, String username, String passwd) {
		WeiboParameters params = new WeiboParameters();
		params.put("username", username);
		params.put("password", passwd);
		params.put("client_id", appId);
		params.put("client_secret", appSecret);
		params.put("grant_type", "password");
		
		try {
			JSONObject json = requestWithoutAccessToken(Constants.OAUTH2_ACCESS_TOKEN, params, HTTP_POST);
			return new String[]{json.optString("access_token"), json.optString("expires_in")};
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "login error:" + e.getClass().getSimpleName());
			}
			return null;
		}
	}
	
	// Non-blackmagic login (logged in by web page, and get the token here)
	public static String[] webLogin(String code) {
		WeiboParameters params = new WeiboParameters();
		params.put("client_id", Constants.APP_KEY);
		params.put("client_secret", Constants.APP_SECRET);
		params.put("code", code);
		params.put("redirect_uri", Constants.REDIRECT_URL);
		params.put("grant_type", "authorization_code");

		try {
			JSONObject json = requestWithoutAccessToken(Constants.OAUTH2_ACCESS_TOKEN, params, HTTP_POST);
			return new String[]{json.optString("access_token"), json.optString("expires_in")};
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "login error:" + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
