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

package us.shandian.blacklight.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.statuses.MentionsTimeLineApi;
import us.shandian.blacklight.cache.statuses.MentionsTimeLineApiCache;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.ui.entry.EntryActivity;

public class MentionsTimeLineFetcherService extends IntentService
{
	private static final String TAG = MentionsTimeLineFetcherService.class.getSimpleName();

	private static final int ID = 100002;

	public MentionsTimeLineFetcherService() {
		super(MentionsTimeLineFetcherService.class.getSimpleName());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		Log.d(TAG, "service start");

		if (BaseApi.getAccessToken() == null) {
			BaseApi.setAccessToken(new LoginApiCache(this).getAccessToken());

			if (BaseApi.getAccessToken() == null) {
				return;
			}
		}

		MentionsTimeLineApiCache cache = new MentionsTimeLineApiCache(this);
		cache.loadFromCache();

		if (cache.mMessages.getSize() > 0) {
			MessageModel last = cache.mMessages.get(0);
			MessageListModel since = MentionsTimeLineApi.fetchMentionsTimeLineSince(last.id);
			if (since != null && since.getSize() > 0) {
				cache.mMessages.addAll(true, since);
				cache.cache();

				int size = since.getSize();
				String str = String.format(getResources().getString(R.string.new_at), size);

				Settings settings = Settings.getInstance(getApplicationContext());
				int defaults = (settings.getBoolean(Settings.NOTIFICATION_SOUND, true) ? Notification.DEFAULT_SOUND : 0)|
						(settings.getBoolean(Settings.NOTIFICATION_VIBRATE, true) ? Notification.DEFAULT_VIBRATE : 0)|
						Notification.DEFAULT_LIGHTS;
				
				android.util.Log.i("Service", "get mentions!");
				
				PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, EntryActivity.class), 0);
				
				Notification n = new Notification.Builder(getApplicationContext())
				.setContentTitle(str)
				.setContentText(getString(R.string.click_to_view))
				.setSmallIcon(R.drawable.ic_action_reply_all)
				.setDefaults(defaults)
				.setAutoCancel(true)
				.setContentIntent(i)
				.build();
				
				NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				m.notify(ID, n);
			}
		}

		Log.d(TAG, "service finished");
	}
}
