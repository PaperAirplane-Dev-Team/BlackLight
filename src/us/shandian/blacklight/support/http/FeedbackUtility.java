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

package us.shandian.blacklight.support.http;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import us.shandian.blacklight.support.CrashHandler;
import us.shandian.blacklight.support.Settings;

public class FeedbackUtility {

	private static final String TAG = FeedbackUtility.class.getSimpleName(); 
	private static final String LOG_API = "http://bbug.typeblog.net/bl-crashlog";

	public static void sendLog(String user,String contact,String log) {
		WeiboParameters params = new WeiboParameters();
		params.put("user", user);
		params.put("contact", contact);
		params.put("log", log);
		try {
			HttpUtility.doRequest(LOG_API, params, HttpUtility.POST);
		} catch (Exception e) {
			Log.e(TAG, "WTF?! Send log failed?");
		}
	}

	public static void sendFeedback(String user,String contact,String feedback){

	}

	public static boolean shouldSendLog(Context context) {
		return Settings.getInstance(context)
			.getBoolean(Settings.AUTO_SUBMIT_LOG, false) &&
			new File(CrashHandler.CRASH_TAG).exists();
	}
}
