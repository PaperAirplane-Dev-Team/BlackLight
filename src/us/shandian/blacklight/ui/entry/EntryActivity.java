/* 
 * Copyright (C) 2014 Peter Cai
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

package us.shandian.blacklight.ui.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.ui.login.LoginActivity;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.CrashHandler;
import us.shandian.blacklight.support.Emoticons;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.feedback.SubmitLogTask;
import us.shandian.blacklight.support.http.FeedbackUtility;

public class EntryActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Clear
		FileCacheManager.instance(this).clearUnavailable();
		
		// Init
		CrashHandler.init(this);
		CrashHandler.register();
		Emoticons.init(this);

		// Crash Log
		if (FeedbackUtility.shouldSendLog(this)) {
			new SubmitLogTask(this).execute();
		}
		
		LoginApiCache login = new LoginApiCache(this);
		if (needsLogin(login)) {
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
