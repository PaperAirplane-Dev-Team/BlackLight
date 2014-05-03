package us.shandian.blacklight.cache.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import us.shandian.blacklight.api.login.LoginApi;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class LoginApiCache
{
	private static final String TAG = LoginApiCache.class.getSimpleName();
	
	private SharedPreferences mPrefs;
	private String mAccessToken;
	private long mExpireDate;
	private String mAppId;
	private String mAppSecret;
	
	public LoginApiCache(Context context) {
		mPrefs = context.getSharedPreferences("access_token", Context.MODE_WORLD_READABLE);
		mAccessToken = mPrefs.getString("access_token", null);
		mExpireDate = mPrefs.getLong("expires_in", Long.MIN_VALUE);
		mAppId = mPrefs.getString("app_id", null);
		mAppSecret = mPrefs.getString("app_secret", null);
	}
	
	public void login(String appId, String appSecret, String username, String passwd) {
		if (mAccessToken == null || mExpireDate == Long.MIN_VALUE) {
			if (DEBUG) {
				Log.d(TAG, "access token not initialized, running login function");
			}
			String[] result = LoginApi.login(appId, appSecret, username, passwd);
			if (result != null) {
				if (DEBUG) {
					Log.d(TAG, "result got, loading to cache");
				}
				mAccessToken = result[0];
				mExpireDate = System.currentTimeMillis() + Long.valueOf(result[1]) * 1000;
				mAppId = appId;
				mAppSecret = appSecret;
			}
		}
	}
	
	public void logout() {
		mAccessToken = null;
		mExpireDate = Long.MIN_VALUE;
		mPrefs.edit().remove("access_token").remove("expires_in").commit();
	}
	
	public void cache() {
		mPrefs.edit().putString("access_token", mAccessToken)
					 .putLong("expires_in", mExpireDate)
					 .putString("app_id", mAppId)
					 .putString("app_secret", mAppSecret)
					 .commit();
	}
	
	public String getAccessToken() {
		return mAccessToken;
	}
	
	public long getExpireDate() {
		return mExpireDate;
	}
	
	public String getAppId() {
		return mAppId;
	}
	
	public String getAppSecret() {
		return mAppSecret;
	}
}
