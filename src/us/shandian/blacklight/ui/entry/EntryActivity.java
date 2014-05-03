package us.shandian.blacklight.ui.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.ui.login.LoginActivity;
import us.shandian.blacklight.support.Utility;

public class EntryActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LoginApiCache login = new LoginApiCache(this);
		if (needsLogin(login)) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, LoginActivity.class);
			startActivity(i);
			finish();
		} else {
			BaseApi.setAccessToken(login.getAccessToken());
			// TODO Enter the main time line
		}
		
	}
	
	private boolean needsLogin(LoginApiCache login) {
		// TODO consider expire date
		return login.getAccessToken() == null || Utility.isTokenExpired(login.getExpireDate());
	}
}
