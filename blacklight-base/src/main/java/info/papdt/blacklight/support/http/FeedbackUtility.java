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

package info.papdt.blacklight.support.http;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import info.papdt.blacklight.support.CrashHandler;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.api.Constants;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class FeedbackUtility {

	private static final String TAG = FeedbackUtility.class.getSimpleName();

	public static void sendLog(String user,String contact) {
		WeiboParameters params = new WeiboParameters();
		params.put("user", user);
		params.put("contact", contact);
		params.put("log", readLog());
		try {
			String r = HttpUtility.doRequest(Constants.LOG_API, params, HttpUtility.POST);
			if (DEBUG) {
				Log.d(TAG, "Server response: " + r);
			}
		} catch (Exception e) {
			Log.e(TAG, "WTF?! Send log failed?", e);
		}
	}

	public static void sendFeedback(String user, String contact, String title, String feedback) {
		WeiboParameters params = new WeiboParameters();
		params.put("user", user);
		params.put("contact", contact);
		params.put("title", title);
		params.put("feedback", feedback);
		params.put("version", CrashHandler.VERSION);

		try {
			String r = HttpUtility.doRequest(Constants.FEEDBACK_API, params, HttpUtility.POST);
			if (DEBUG) {
				Log.d(TAG, "Server response: " + r);
			}
		} catch (Exception e) {
			Log.e(TAG, "WTF?! Send feedback failed?!", e);
		}

	}

	public static boolean shouldSendLog(Context context) {
		return Settings.getInstance(context)
			.getBoolean(Settings.AUTO_SUBMIT_LOG, false) &&
			new File(CrashHandler.CRASH_TAG).exists();
	}

	private static String readLog(){
		StringBuilder res = new StringBuilder();
		try{
			FileInputStream fin = new FileInputStream(CrashHandler.CRASH_LOG);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
			String line;
			while( (line = buf.readLine()) != null) {
				res.append(line);
				res.append("\r");
			}
			buf.close();
		}catch(Exception e){
			Log.e(TAG, "Error reading log", e);
		}
		return res.toString();
	}
}
