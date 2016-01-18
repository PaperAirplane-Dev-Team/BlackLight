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

package info.papdt.blacklight.service;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import info.papdt.blacklight.api.remind.RemindApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.model.UnreadModel;
import info.papdt.blacklight.support.Reminders;
import info.papdt.blacklight.support.Settings;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
 * A service class that fetches notifications
 * From sina's API
 * Should be started by AlarmManager
 */
public class ReminderService extends IntentService {
	private static final String TAG = ReminderService.class.getSimpleName();

	private void doFetchRemind() {
		LoginApiCache cache = new LoginApiCache(this);
		String uid = cache.getUid();

		if (!TextUtils.isEmpty(uid)) {
			UnreadModel unread = RemindApi.getUnread(uid);

			if (DEBUG) {
				Log.d(TAG, "unread got: " + (unread != null));
			}

			if (unread != null) {
				doUpdateNotifications(unread);
			}
		}
	}

	private void doUpdateNotifications(UnreadModel unread) {
		if (DEBUG) {
			Log.d(TAG, "update notifications");
		}

		Context ctx = getApplicationContext();
		Settings settings = Settings.getInstance(ctx);

		String previous = settings.getString(Settings.NOTIFICATION_ONGOING, "");
		String now = unread.toString();
		if (now.equals(previous)) {
			Log.d(TAG, "No actual unread notifications.");
			return;
		} else {
			settings.putString(Settings.NOTIFICATION_ONGOING, now);
		}

		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Boolean expand = settings.getBoolean(Settings.SHOW_BIGTEXT, false);
		Reminders rmd = new Reminders(ctx, nm, parseDefaults(ctx), expand);

		if (unread.cmt > 0 && settings.getBoolean(Settings.NOTIFY_CMT, true)) {
			if (DEBUG) {
				Log.d(TAG, "New comment: " + unread.cmt);
			}
			rmd.execCmt(unread.cmt);
		}

		if ((unread.mention_status > 0 || unread.mention_cmt > 0) && settings.getBoolean(Settings.NOTIFY_AT, true)) {
			if (DEBUG) {
				Log.d(TAG, "New mentions: " + unread.mention_status + unread.mention_cmt);
			}
			rmd.execMention(unread.mention_status, unread.mention_cmt);
		}

		if (unread.dm > 0 && settings.getBoolean(Settings.NOTIFY_DM, true)) {
			if (DEBUG) {
				Log.d(TAG, "New dm: " + unread.dm);
			}
			rmd.execDm(unread.dm);
		}
	}

	public ReminderService() {
		super(TAG);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		if (DEBUG) {
			Log.d(TAG, "start");
		}

		doFetchRemind();

	}

	private static void updateTimeLine(Object obj) {
		try {
			Method loadFromCache = findMethod(obj, "loadFromCache");
			Method load = findMethod(obj, "load", boolean.class);
			Method cache = findMethod(obj, "cache");
			loadFromCache.setAccessible(true);
			load.setAccessible(true);
			cache.setAccessible(true);
			loadFromCache.invoke(obj);
			load.invoke(obj, true);
			cache.invoke(obj);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "WTF?! Cannot update time line??");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}

	private static Method findMethod(Object obj, String name, Class<?>... params) {
		Class<?> clazz = obj.getClass();

		while (clazz != null) {
			try {
				return clazz.getDeclaredMethod(name, params);
			} catch (Exception e) {
				clazz = clazz.getSuperclass();
			}
		}

		return null;
	}

	private static int parseDefaults(Context context) {
		Settings settings = Settings.getInstance(context);

		return (settings.getBoolean(Settings.NOTIFICATION_SOUND, true) ? Notification.DEFAULT_SOUND : 0) |
			(settings.getBoolean(Settings.NOTIFICATION_VIBRATE, true) ? Notification.DEFAULT_VIBRATE : 0) |
			Notification.DEFAULT_LIGHTS;
	}
}
