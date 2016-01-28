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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.papdt.blacklight.model.MessageModel;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/* This class is modified from qii/weiciyuan */
public class SpannableStringUtils
{
	private static final String TAG = SpannableStringUtils.class.getSimpleName();

	private static final Pattern PATTERN_WEB = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
	private static final Pattern PATTERN_TOPIC = Pattern.compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
	private static final Pattern PATTERN_MENTION = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
	private static final Pattern PATTERN_EMOTICON = Pattern.compile("\\[(\\S+?)\\]");
	private static final Pattern PATTERN_STYLE = Pattern.compile("(?!^\\\\$)((_+)|(\\*+)|(~+))(\\w{1,})(?!^\\\\$)\\1");

	private static final String HTTP_SCHEME = "http://";
	private static final String TOPIC_SCHEME = "us.shandian.blacklight.topic://";
	private static final String MENTION_SCHEME = "us.shandian.blacklight.user://";

	private static boolean mStyleText;

	public static void init(Context context) {
		Settings settings = Settings.getInstance(context);
		mStyleText = settings.getBoolean(Settings.STYLE_TEXT, true);
	}

	public static SpannableString span(Context context, String text) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(text);
		Linkify.addLinks(ssb, PATTERN_WEB, HTTP_SCHEME);
		Linkify.addLinks(ssb, PATTERN_TOPIC, TOPIC_SCHEME);
		Linkify.addLinks(ssb, PATTERN_MENTION, MENTION_SCHEME);

		// Convert to our own span
		URLSpan[] spans = ssb.getSpans(0, ssb.length(), URLSpan.class);
		for (URLSpan span : spans) {
			WeiboSpan s = new WeiboSpan(span.getURL());
			int start = ssb.getSpanStart(span);
			int end = ssb.getSpanEnd(span);
			ssb.removeSpan(span);
			ssb.setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		// Match Emoticons
		Matcher matcher = PATTERN_EMOTICON.matcher(text);
		while (matcher.find()) {
			// Don't be too long
			if (matcher.end() - matcher.start() < 8) {
				String iconName = matcher.group(0);
				Bitmap bitmap = Emoticons.EMOTICON_BITMAPS_SCALED.get(iconName);

				if (bitmap != null) {
					ImageSpan span = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BASELINE);
					ssb.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}

		// Match style
		if (mStyleText) {
			text = text.replace("\\n", "\n").replace("<br>", "\n");
			WeiboSpan[] mySpans = ssb.getSpans(0, ssb.length(), WeiboSpan.class);
			matcher = PATTERN_STYLE.matcher(ssb);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();

				if (isInsideSpans(start, end, mySpans, ssb)) {
					continue;
				}

				String group = matcher.group(1);

				int len = group.length();

				Object span = null;

				if (group.startsWith("~")) {
					span = new StrikethroughSpan();
				} else {
					int type = Typeface.BOLD;
					if (len == 1) {
						type = Typeface.ITALIC;
					} else if (len == 2) {
						type = Typeface.BOLD;
					}
					span = new StyleSpan(type);
				}

				if (span != null) {
					ssb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				ssb.delete(start, start + len);

				end -= len;
				ssb.delete(end - len, end);

				matcher = PATTERN_STYLE.matcher(ssb);
			}
		}

		return SpannableString.valueOf(ssb);
	}

	public static SpannableString getSpan(Context context, MessageModel msg) {
		if (msg.span == null) {

			if (DEBUG) {
				Log.d(TAG, msg.id + " span is null");
			}

			msg.span = span(context, msg.text);
		}

		return msg.span;
	}

	public static SpannableString getOrigSpan(Context context, MessageModel orig) {
		if (orig.origSpan == null) {

			if (DEBUG) {
				Log.d(TAG, orig.id + " origSpan is null");
			}

			String username = "";

			if (orig.user != null) {
				username = orig.user.getName();
				username = "@" + username + ":";
			}

			orig.origSpan = span(context, username + orig.text);
		}

		return orig.origSpan;
	}

	private static boolean isInsideSpans(int start, int end, Object[] spans, SpannableStringBuilder s) {
		for (Object span : spans) {
			int spanStart = s.getSpanStart(span);
			int spanEnd = s.getSpanEnd(span);
			if ((start >= spanStart && start <= spanEnd) || (end >= spanStart && end <= spanEnd)) {
				return true;
			}
		}

		return false;
	}
}
