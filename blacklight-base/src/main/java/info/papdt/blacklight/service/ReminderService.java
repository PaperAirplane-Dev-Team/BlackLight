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

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.comments.CommentMentionsTimeLineApi;
import info.papdt.blacklight.api.directmessages.DirectMessagesApi;
import info.papdt.blacklight.api.remind.RemindApi;
import info.papdt.blacklight.api.statuses.MentionsTimeLineApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.model.CommentListModel;
import info.papdt.blacklight.model.DirectMessageListModel;
import info.papdt.blacklight.model.DirectMessageModel;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.model.UnreadModel;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.ui.entry.EntryActivity;
import info.papdt.blacklight.ui.main.MainActivity;
import info.papdt.blacklight.api.comments.CommentTimeLineApi;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
 * A service class that fetches notifications
 * From sina's API
 * Should be started by AlarmManager
 */
public class ReminderService extends IntentService {
	private static final String TAG = ReminderService.class.getSimpleName();

	private static final int ID = 100000;
	private static final int ID_CMT = ID + 1;
	private static final int ID_MENTION = ID + 2;
	private static final int ID_DM = ID + 3;

	private static final int LIMIT_TEXT = 30;

	private void doFetchRemind() {
		LoginApiCache cache = new LoginApiCache(this);
		String uid = cache.getUid();

		if (!TextUtils.isEmpty(uid)) {
			UnreadModel unread = RemindApi.getUnread(uid);

			if (DEBUG) {
				Log.d(TAG, "unread got: " + (unread != null));
			}

			doUpdateNotifications(unread);
		}
	}

	private void doUpdateNotifications(UnreadModel unread) {
		if (DEBUG) {
			Log.d(TAG, "update notifications");
		}

		Context c = getApplicationContext();

		if (unread != null) {
			Settings settings = Settings.getInstance(c);
			String previous = settings.getString(Settings.NOTIFICATION_ONGOING, "");
			String now = unread.toString();
			if (now.equals(previous)) {
				Log.d(TAG, "No actual unread notifications.");
				//return;
			} else {
				settings.putString(Settings.NOTIFICATION_ONGOING, now);
			}

			Boolean expand = settings.getBoolean(Settings.SHOW_BIGTEXT, false);
			int defaults = parseDefaults(c);
			Intent i = new Intent(c, EntryActivity.class);
			i.setPackage(c.getPackageName());
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi;
			String clickToView = c.getString(R.string.click_to_view);
			String expandToView = c.getString(R.string.expand_to_view);
			NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

			if (unread.cmt > 0 && settings.getBoolean(Settings.NOTIFY_CMT, true)) {
				if (DEBUG) {
					Log.d(TAG, "New comment: " + unread.cmt);
				}

				i.putExtra(Intent.EXTRA_INTENT, MainActivity.COMMENT);
				pi = PendingIntent.getActivity(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

				Notification n;
				CommentListModel newComments = null;
				if (expand) {
					newComments = CommentTimeLineApi.fetchCommentTimeLineToMe(Math.min(unread.cmt, 5), 1);
				}
				if (expand && newComments != null) {
					String bigText = buildBigText(newComments);

					n = buildBigNotification(c,
							format(c, R.string.new_comment, unread.cmt),
							expandToView,
							bigText,
							R.drawable.ic_action_chat,
							defaults,
							pi);
				} else {
					n = buildNotification(c,
							format(c, R.string.new_comment, unread.cmt),
							clickToView,
							R.drawable.ic_action_chat,
							defaults,
							pi);
				}
				nm.notify(ID_CMT, n);
			}

			if ((unread.mention_status > 0 || unread.mention_cmt > 0) && settings.getBoolean(Settings.NOTIFY_AT, true)) {
				String detail = "";
				String bigText = "";
				int count = 0;
				
				if (unread.mention_status > 0) {
					detail += format(c, R.string.new_at_detail_weibo, unread.mention_status);
					count += unread.mention_status;
					i.putExtra(Intent.EXTRA_INTENT,MainActivity.MENTION);

					if(expand){
						MessageListModel newMentions = MentionsTimeLineApi.fetchMentionsTimeLine(Math.min(unread.mention_status, 5), 1);
						if (newMentions != null) {
							bigText += buildBigText(newMentions);
						}
					}
				}

				if (unread.mention_cmt > 0) {
					if (count > 0) {
						detail += c.getString(R.string.new_at_detail_and);
					}

					detail += format(c, R.string.new_at_detail_comment, unread.mention_cmt);
					count += unread.mention_cmt;

					if (unread.mention_status == 0){
						i.putExtra(Intent.EXTRA_INTENT,MainActivity.MENTION_CMT);
					}

					if(expand){
						MessageListModel newMentionsCmt = CommentMentionsTimeLineApi.fetchCommentMentionsTimeLine(Math.min(unread.mention_cmt, 5), 1);
						if (newMentionsCmt != null) {
							bigText += System.getProperty("line.separator");
							bigText += buildBigText(newMentionsCmt);
						}
					}
				}

				if (DEBUG) {
					Log.d(TAG, "New mentions: " + count);
				}

				pi = PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

				Notification n;
				if (expand && !bigText.equals("")) {
					n = buildBigNotification(c,
							format(c, R.string.new_at, count),
							expandToView,
							bigText,
							R.drawable.ic_action_reply_all,
							defaults,
							pi);
				} else {
					n = buildNotification(c,
							format(c, R.string.new_at, count),
							detail,
							R.drawable.ic_action_reply_all,
							defaults,
							pi);
				}
				nm.notify(ID_MENTION, n);
			}

			if (unread.dm > 0 && settings.getBoolean(Settings.NOTIFY_DM, true)) {
				if (DEBUG) {
					Log.d(TAG, "New dm: " + unread.dm);
				}

				i.putExtra(Intent.EXTRA_INTENT,MainActivity.DM);
				pi = PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

				Notification n;
				DirectMessageListModel newDm = null;
				if (expand) {
					newDm = DirectMessagesApi.getDirectMessages(Math.min(unread.dm, 5), 1);
				}
				if (expand && newDm != null) {
					String bigText = buildBigText(newDm);

					n = buildBigNotification(c,
							format(c, R.string.new_dm, unread.dm),
							expandToView,
							bigText,
							R.drawable.ic_action_email,
							defaults,
							pi);
				} else {
					n = buildNotification(c,
							format(c, R.string.new_dm, unread.dm),
							clickToView,
							R.drawable.ic_action_email,
							defaults,
							pi);
				}
				nm.notify(ID_DM, n);
			}
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

	@SuppressLint("NewApi")
	private static Notification buildNotification(Context context, String title, String text, int icon, int defaults, PendingIntent intent) {
		return new Notification.Builder(context)
			.setContentTitle(title)
			.setContentText(text)
			.setSmallIcon(icon)
			.setDefaults(defaults)
			.setAutoCancel(true)
			.setContentIntent(intent)
			.build();
		//FIXME 话说Lint报了个错说只有API 16+才能用啊
	}

	@SuppressLint("NewApi")
	private static Notification buildBigNotification(Context context, String title, String text, String bigText, int icon, int defaults, PendingIntent intent) {
		Notification.Builder builder =  new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(text)
				.setSmallIcon(icon)
				.setDefaults(defaults)
				.setAutoCancel(true)
				.setContentIntent(intent);
		builder.setStyle(new Notification.BigTextStyle().bigText(bigText));
		return builder.build();
	}

	private static String format(Context context, int resId, int data) {
		return String.format(context.getString(resId), data);
	}

	public static String ellipsis(final String text, int length)
	{
		if (text.length() > length)
		{
			return text.substring(0, length - 3) + "...";
		}

		return text;
	}

	private String formatLine(String name, String text) {
		return String.format("@%s: %s", name, ellipsis(text, LIMIT_TEXT));
	}

	private String buildBigText(MessageListModel model) {
		String detail;
		detail = formatLine(model.get(0).user.name, model.get(0).text);
		List<? extends MessageModel> list = model.getList();
		list.remove(0);

		for (MessageModel msg : list) {
			detail += System.getProperty("line.separator");
			detail +=  formatLine(msg.user.name,msg.text);
		}

		return detail;
	}

	private String buildBigText(DirectMessageListModel model) {
		String detail;
		detail = formatLine(model.get(0).sender.name, model.get(0).text);
		List<? extends DirectMessageModel> list = model.getList();
		list.remove(0);

		for (DirectMessageModel msg : list) {
			detail += System.getProperty("line.separator");
			detail +=  formatLine(msg.sender.name,msg.text);
		}

		return detail;
	}
}
