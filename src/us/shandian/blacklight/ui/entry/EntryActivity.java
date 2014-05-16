package us.shandian.blacklight.ui.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.ui.login.WebLoginActivity;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.Emoticons;
import us.shandian.blacklight.support.Utility;

public class EntryActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Clear
		FileCacheManager.instance(this).clearUnavailable();
		
		// Init
		Emoticons.init(this);
		
		LoginApiCache login = new LoginApiCache(this);
		if (needsLogin(login)) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, WebLoginActivity.class);
			startActivity(i);
			finish();
		} else {
			// Logout BM if not available
			if (login.hasBlackMagic()) {
				if (Utility.isTokenExpired(login.getExpireDate())) {
					login.BMLogout();
					login.cache();
				}
			}
			
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, MainActivity.class);
			startActivity(i);
			finish();
		}
		
	}
	
	private boolean needsLogin(LoginApiCache login) {
		return login.getAccessToken() == null || Utility.isTokenExpired(login.getExpireDate());
	}
}
