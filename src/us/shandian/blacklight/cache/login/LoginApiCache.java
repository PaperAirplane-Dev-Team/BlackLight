package us.shandian.blacklight.cache.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.login.LoginApi;
import us.shandian.blacklight.api.user.AccountApi;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class LoginApiCache
{
	private static final String TAG = LoginApiCache.class.getSimpleName();
	
	private SharedPreferences mPrefs;
	private String mAccessToken;
	private String mUid;
	private long mExpireDate;
	private String mBMAccessToken;
	private long mBMExpireDate;
	private String mAppId;
	private String mAppSecret;
	
	public LoginApiCache(Context context) {
		mPrefs = context.getSharedPreferences("access_token", Context.MODE_WORLD_READABLE);
		mAccessToken = mPrefs.getString("access_token", null);
		mUid = mPrefs.getString("uid", null);
		mExpireDate = mPrefs.getLong("expires_in", Long.MIN_VALUE);
		mAppId = mPrefs.getString("app_id", null);
		mAppSecret = mPrefs.getString("app_secret", null);
		mBMAccessToken = mPrefs.getString("bm_access_token", null);
		mBMExpireDate = mPrefs.getLong("bm_expires_in", Long.MIN_VALUE);
		
		if (mAccessToken != null) {
			BaseApi.setAccessToken(mAccessToken);
			
			if (mBMAccessToken != null) {
				BaseApi.setBMAccessToken(mBMAccessToken);
			}
		}
	}
	
	public void login(String appId, String appSecret, String username, String passwd) {
		if (mBMAccessToken == null || mBMExpireDate == Long.MIN_VALUE) {
			if (DEBUG) {
				Log.d(TAG, "access token not initialized, running login function");
			}
			String[] result = LoginApi.login(appId, appSecret, username, passwd);
			if (result != null) {
				if (DEBUG) {
					Log.d(TAG, "result got, loading to cache");
				}
				mBMAccessToken = result[0];
				BaseApi.setBMAccessToken(mBMAccessToken);
				mBMExpireDate = System.currentTimeMillis() + Long.valueOf(result[1]) * 1000;
				mAppId = appId;
				mAppSecret = appSecret;
			}
		}
	}
	
	public void webLogin(String code) {
		if (mAccessToken == null || mExpireDate == Long.MIN_VALUE) {
			if (DEBUG) {
				Log.d(TAG, "access token not initialized, running login function");
			}
			String[] result = LoginApi.webLogin(code);
			if (result != null) {
				if (DEBUG) {
					Log.d(TAG, "result got, loading to cache");
				}
				mAccessToken = result[0];
				BaseApi.setAccessToken(mAccessToken);
				mUid = AccountApi.getUid();
				mExpireDate = System.currentTimeMillis() + Long.valueOf(result[1]) * 1000;
			}
		}
	}
	
	public void logout() {
		mAccessToken = null;
		mExpireDate = Long.MIN_VALUE;
		mPrefs.edit().remove("access_token").remove("expires_in").remove("uid").remove("bm_access_token").remove("bm_expires_in").commit();
	}
	
	public void cache() {
		mPrefs.edit().putString("access_token", mAccessToken)
					 .putLong("expires_in", mExpireDate)
					 .putString("uid", mUid)
					 .putString("app_id", mAppId)
					 .putString("app_secret", mAppSecret)
					 .putString("bm_access_token", mBMAccessToken)
					 .putLong("bm_expires_in", mBMExpireDate)
					 .commit();
	}
	
	public String getAccessToken() {
		return mAccessToken;
	}
	
	public String getUid() {
		return mUid;
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
	
	public boolean hasBlackMagic() {
		return mBMAccessToken != null;
	}
	
	public String getBMAccessToken() {
		return mBMAccessToken;
	}
	
	public long getBMExpireDate() {
		return mBMExpireDate;
	}
}
