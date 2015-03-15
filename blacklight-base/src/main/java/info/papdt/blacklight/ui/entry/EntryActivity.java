/* 
 * Copyright (C) 2015 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.papdt.blacklight.ui.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import info.papdt.blacklight.cache.file.FileCacheManager;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.receiver.ConnectivityReceiver;
import info.papdt.blacklight.support.CrashHandler;
import info.papdt.blacklight.support.Emoticons;
import info.papdt.blacklight.support.FilterUtility;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.feedback.SubmitLogTask;
import info.papdt.blacklight.support.http.FeedbackUtility;
import info.papdt.blacklight.ui.login.LoginActivity;
import info.papdt.blacklight.ui.main.MainActivity;

public class EntryActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CrashHandler.init(this);
		CrashHandler.register();
		
		super.onCreate(savedInstanceState);

		// Clear
		FileCacheManager.instance(this).clearUnavailable();
		
		// Init
		ConnectivityReceiver.readNetworkState(this);
		Emoticons.init(this);
		FilterUtility.init(this);

		// Crash Log
		if (FeedbackUtility.shouldSendLog(this)) {
			new SubmitLogTask(this).execute();
		}
		
		LoginApiCache login = new LoginApiCache(this);
		if (needsLogin(login)) {
			login.logout();
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, LoginActivity.class);
			startActivity(i);
			finish();
		} else {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, MainActivity.class);
			i.putExtra(Intent.EXTRA_INTENT,getIntent().getIntExtra(Intent.EXTRA_INTENT,0));
			startActivity(i);
			finish();
		}
		
	}
	
	private boolean needsLogin(LoginApiCache login) {
		return login.getAccessToken() == null || Utility.isTokenExpired(login.getExpireDate());
	}
}
