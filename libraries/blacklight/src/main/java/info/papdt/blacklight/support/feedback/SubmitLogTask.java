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

package info.papdt.blacklight.support.feedback;

import android.content.Context;

import java.io.File;

import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.CrashHandler;
import info.papdt.blacklight.support.http.FeedbackUtility;

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
			contact = "http://weibo.com/u/" + uid;
			FeedbackUtility.sendLog(username, contact);
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		new File(CrashHandler.CRASH_TAG).delete();
	}
}
