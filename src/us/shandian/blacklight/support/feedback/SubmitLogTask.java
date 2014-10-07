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

package us.shandian.blacklight.support.feedback;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;

import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.CrashHandler;
import us.shandian.blacklight.support.http.FeedbackUtility;

public class SubmitLogTask extends AsyncTask<Void,Void,Void> {

	private Context mContext;

	public SubmitLogTask(Context context) {
		mContext = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		LoginApiCache logincache = new LoginApiCache(mContext);
		UserApiCache usercache = new UserApiCache(mContext);
		String uid = logincache.getUid();

		if(uid == null){
			FeedbackUtility.sendLog(null, null);
		}else{
			String username,contact;
			UserModel user = usercache.getUser(uid);
			username = user.screen_name;
			contact = user.url;
			FeedbackUtility.sendLog(username, contact);
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		new File(CrashHandler.CRASH_TAG).delete();
	}
}
