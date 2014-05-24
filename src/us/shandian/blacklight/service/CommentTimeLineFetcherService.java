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
import us.shandian.blacklight.api.comments.CommentTimeLineApi;
import us.shandian.blacklight.cache.comments.CommentTimeLineApiCache;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.model.CommentListModel;
import us.shandian.blacklight.ui.main.MainActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class CommentTimeLineFetcherService extends IntentService
{
	private static final String TAG = CommentTimeLineFetcherService.class.getSimpleName();
	
	private static final int ID = 100001;
	
	public CommentTimeLineFetcherService() {
		super(CommentTimeLineFetcherService.class.getSimpleName());
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
		}
		
		CommentTimeLineApiCache cache = new CommentTimeLineApiCache(this);
		cache.loadFromCache();

		if (cache.mMessages.getSize() > 0) {
			CommentModel last = (CommentModel) cache.mMessages.get(0);
			CommentListModel since = CommentTimeLineApi.fetchCommentTimeLineSince(last.id);
			if (since != null && since.getSize() > 0) {
				cache.mMessages.addAll(true, since);
				cache.cache();

				int size = since.getSize();
				String str = String.format(getResources().getString(R.string.new_comment), size);
				Notification no = new Notification(R.drawable.ic_action_chat, str, System.currentTimeMillis());
				PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
				no.setLatestEventInfo(this, str, getResources().getString(R.string.click_to_view), i);
				NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				m.notify(ID, no);
			}
		}
		
		Log.d(TAG, "service finished");
	}
}
