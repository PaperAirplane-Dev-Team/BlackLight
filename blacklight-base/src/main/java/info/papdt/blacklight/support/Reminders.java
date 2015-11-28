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

package info.papdt.blacklight.support;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import java.util.ArrayList;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.comments.CommentMentionsTimeLineApi;
import info.papdt.blacklight.api.comments.CommentTimeLineApi;
import info.papdt.blacklight.api.directmessages.DirectMessagesApi;
import info.papdt.blacklight.api.statuses.MentionsTimeLineApi;
import info.papdt.blacklight.model.CommentListModel;
import info.papdt.blacklight.model.DirectMessageListModel;
import info.papdt.blacklight.model.DirectMessageModel;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.ui.comments.ReplyToActivity;
import info.papdt.blacklight.ui.entry.EntryActivity;
import info.papdt.blacklight.ui.main.MainActivity;

public class Reminders {
	private static final int ID = 100000;
	private static final int ID_CMT = ID + 1;
	private static final int ID_MENTION = ID + 2;
	private static final int ID_DM = ID + 3;

	private static final int FETCH_MAX = 5;

	Context mContext;
	NotificationManager mManager;
	int mDefaults;
	boolean mExpand;

	public Reminders(Context context, NotificationManager manager, int defaults, boolean expand) {
		mContext = context;
		mManager = manager;
		mDefaults = defaults;
		mExpand = expand;
	}

	public void execCmt(int count) {
		new CmtTask().execute(count);
	}

	@SuppressLint("NewApi")
	public class CmtTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			int count = (int) params[0];
			CommentListModel newComments = null;
			String title = format(mContext, R.string.new_comment, count);
			RemindBuilder rmd = new RemindBuilder(
					title,
					R.drawable.ic_action_chat,
					count,
					getPending(MainActivity.COMMENT)
			);

			if (mExpand) {
				newComments = CommentTimeLineApi.fetchCommentTimeLineToMe(Math.min(count, FETCH_MAX), 1);
			}

			if (mExpand && newComments != null && newComments.getSize() > 0) {
				if (newComments.getSize() == 1) {
					Intent it = new Intent(mContext, ReplyToActivity.class);
					it.putExtra("comment", newComments.get(0));
					PendingIntent pin = PendingIntent.getActivity(mContext, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);

					rmd.addAction(R.drawable.ic_action_chat, getString(R.string.reply), pin);
				}

				rmd.buildExpand(newComments, count, title);
			}
			mManager.notify(ID_CMT, rmd.build());
			return null;
		}
	}

	public void execMention(int countMention, int countCmt) {
		new MentionTask().execute(countMention, countCmt);
	}

	@SuppressLint("NewApi")
	public class MentionTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			String detail = "";
			MessageListModel list = new MessageListModel();
			PendingIntent pi = null;
			int mention = (int) params[0];
			int cmt = (int) params[1];
			int count = mention + cmt;

			if (mention > 0) {
				detail += format(mContext, R.string.new_at_detail_weibo, mention);
				pi = getPending(MainActivity.MENTION);

				if (mExpand) {
					MessageListModel newMentions = MentionsTimeLineApi.fetchMentionsTimeLine(Math.min(mention, 5), 1);
					if (newMentions != null) {
						list.addAll(true, newMentions);
					}
				}
			}

			if (cmt > 0) {
				if (mention != 0) {
					detail += mContext.getString(R.string.new_at_detail_and);
				}

				detail += format(mContext, R.string.new_at_detail_comment, cmt);

				if (mention == 0) {
					pi = getPending(MainActivity.MENTION_CMT);
				}

				if(mExpand){
					MessageListModel newMentionsCmt = CommentMentionsTimeLineApi.fetchCommentMentionsTimeLine(Math.min(cmt, 5), 1);
					if (newMentionsCmt != null) {
						list.addAll(true, newMentionsCmt);
					}
				}
			}

			String title = format(mContext, R.string.new_at, count);
			RemindBuilder rmd = new RemindBuilder(
					title,
					R.drawable.ic_action_reply_all,
					count,
					pi
			);

			if (mExpand && list.getSize() > 0) {
				ArrayList<CharSequence> lines = rmd.buildExpand(list, count, detail);

				if (list.getSize() == 1) {
					rmd.setContentText(lines.get(0));
				} else {
					rmd.setContentText(detail);
				}
			}

			mManager.notify(ID_MENTION, rmd.build());
			return null;
		}
	}

	public void execDm(int count) {
		new DmTask().execute(count);
	}

	@SuppressLint("NewApi")
	public class DmTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			int count = (int) params[0];
			DirectMessageListModel newDm = null;
			String title = format(mContext, R.string.new_dm, count);
			RemindBuilder rmd = new RemindBuilder(
					title,
					R.drawable.ic_action_email,
					count,
					getPending(MainActivity.DM)
			);

			if (mExpand) {
				newDm = DirectMessagesApi.getDirectMessages(Math.min(count, 5), 1);
			}

			if (mExpand && newDm != null && newDm.getSize() > 0) {
				rmd.buildExpand(newDm, count, title);
			}
			mManager.notify(ID_DM, rmd.build());
			return null;
		}
	}


	/*
	Helpers
	 */
	private PendingIntent getPending(int tab) {
		Intent i = new Intent(mContext, EntryActivity.class);
		i.setPackage(mContext.getPackageName());
		i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra(Intent.EXTRA_INTENT, tab);

		return PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static String format(Context context, int resId, int data) {
		return String.format(context.getString(resId), data);
	}

	private String getString(@StringRes int resId) {
		return mContext.getString(resId);
	}

	@SuppressLint("NewApi")
	private class RemindBuilder extends Notification.Builder {
		public RemindBuilder(String title, int icon, int count, PendingIntent intent) {
			super(mContext);
			setContentTitle(title);
			setSmallIcon(icon);
			setNumber(count);
			setContentIntent(intent);
			setAutoCancel(true);
			setDefaults(mDefaults);
			setContentText(R.string.click_to_view);
		}

		public RemindBuilder setContentText(@StringRes int resId) {
			setContentText(getString(resId));
			return this;
		}


		private String stripReply(String text) {
			String pattern = "(" + TextUtils.join("|", mContext.getResources().getStringArray(R.array.reply_all_lang)) +").+?:";
			return text.replaceFirst(pattern, "");
		}

		private Spannable formatSpannable(String name, String text) {
			String prefix = "@" + name + " ";
			SpannableStringBuilder sp = new SpannableStringBuilder();
			sp.append(prefix).append(text)
					.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			return sp;
		}


		public RemindBuilder buildExpand(ArrayList<CharSequence> lines, int count, CharSequence defaultSummary) {
			// If there is only one line added, it'll be "eaten", so just use BigText
			if (lines.size() == 1) {
				setContentText(lines.get(0));
				setStyle(new Notification.BigTextStyle()
						.bigText(lines.get(0))
				);
			} else {
				Notification.InboxStyle inboxStyle = new Notification.InboxStyle();

				for (CharSequence line : lines) {
					inboxStyle.addLine(line);
				}

				if (count <= FETCH_MAX) {
					inboxStyle.setSummaryText(defaultSummary);
				} else {
					inboxStyle.setSummaryText(format(mContext, R.string.more_not_displayed, count - FETCH_MAX));
				}

				setStyle(inboxStyle);
				setContentText(R.string.expand_to_view);
			}

			return this;
		}

		public ArrayList<CharSequence> buildExpand(MessageListModel model, int count, CharSequence defaultSummary) {
			ArrayList<CharSequence> lines = new ArrayList<>();
			for (MessageModel msg : model.getList()) {
				lines.add(formatSpannable(msg.user.name, stripReply(msg.text)));
			}
			buildExpand(lines, count, defaultSummary);
			return lines;
		}

		public ArrayList<CharSequence> buildExpand(DirectMessageListModel model, int count, CharSequence defaultSummary) {
			ArrayList<CharSequence> lines = new ArrayList<>();
			for (DirectMessageModel msg : model.getList()) {
				lines.add(formatSpannable(msg.sender.name, msg.text));
			}
			buildExpand(lines, count, defaultSummary);
			return lines;
		}
	}


}
