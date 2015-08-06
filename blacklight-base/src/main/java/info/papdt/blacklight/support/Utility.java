/*
 * Copyright (C) 2015 Peter Cai
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import info.papdt.blacklight.R;
import info.papdt.blacklight.service.ReminderService;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/* Helper functions */
public class Utility
{
	private static final String TAG = Utility.class.getSimpleName();

	private static final int REQUEST_CODE = 100001;

	public static String lastPicPath;

	public static int action_bar_title = -1;

	public static int action_bar_spinner = -1;

	static {
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$id");
			Field f = clazz.getDeclaredField("action_bar_title");
			f.setAccessible(true);
			action_bar_title = f.getInt(null);
			f = clazz.getDeclaredField("action_bar_spinner");
			f.setAccessible(true);
			action_bar_spinner = f.getInt(null);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Reflection cannot access internal ids");
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}

	public static int expireTimeInDays(long time) {
		return (int) TimeUnit.MILLISECONDS.toDays(time - System.currentTimeMillis());
	}

	public static boolean isTokenExpired(long time) {
		return time <= System.currentTimeMillis();
	}

	public static boolean isCacheAvailable(long createTime, int availableDays) {
		return System.currentTimeMillis() <= createTime + TimeUnit.DAYS.toMillis(availableDays);
	}

	public static int lengthOfString(String str) throws UnsupportedEncodingException {
		// Considers 1 Chinese character as 2 English characters
		return (str.getBytes("GB2312").length + 1) / 2;
	}

	public static int getSupportedMaxPictureSize() {
		int[] array = new int[1];
		GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, array, 0);

		try {
			if (array[0] == 0) {
				GLES11.glGetIntegerv(GLES11.GL_MAX_TEXTURE_SIZE, array, 0);

				if (array[0] == 0) {
					GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, array, 0);

					if (array[0] == 0) {
						GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, array, 0);
					}
				}
			}
		} catch (NoClassDefFoundError e) {
			// Ignore the exception
		}

		return array[0] != 0 ? array[0] : 2048;
	}

	public static String truncateSourceString(String from) {
		int start = from.indexOf(">") + 1;
		int end = from.lastIndexOf("<");
		return from.substring(start, end);
	}

	public static void clearOngoingUnreadCount(Context context) {
		Settings s = Settings.getInstance(context);
		s.putString(Settings.NOTIFICATION_ONGOING, "");
	}

	public static void startServiceAlarm(Context context, Class<?> service, long interval) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, service);
		PendingIntent p = PendingIntent.getService(context, REQUEST_CODE, i, PendingIntent.FLAG_CANCEL_CURRENT);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, p);
	}

	public static void stopServiceAlarm(Context context, Class<?> service) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, service);
		PendingIntent p = PendingIntent.getService(context, REQUEST_CODE, i, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(p);
	}

	public static void startServices(Context context) {
		Settings settings = Settings.getInstance(context);
		int interval = getIntervalTime(settings.getInt(Settings.NOTIFICATION_INTERVAL, 1));

		if (interval > -1) {
			startServiceAlarm(context, ReminderService.class, interval);
		}
	}

	public static void stopServices(Context context) {
		stopServiceAlarm(context, ReminderService.class);
	}

	public static void restartServices(Context context) {
		stopServices(context);

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			startServices(context);
		}
	}

	public static int getIntervalTime(int id) {
		switch (id){
		case 0:
			return 1 * 60 * 1000;
		case 1:
			return 3 * 60 * 1000;
		case 2:
			return 5 * 60 * 1000;
		case 3:
			return 10 * 60 * 1000;
		case 4:
			return 30 * 60 * 1000;
		case 5:
			return -1;
		}
		return -1;
	}

	public static int getCurrentLanguage(Context context) {
		int lang = Settings.getInstance(context).getInt(Settings.LANGUAGE, -1);
		if (lang == -1) {
			String language = Locale.getDefault().getLanguage();
			String country = Locale.getDefault().getCountry();

			if (DEBUG) {
				Log.d(TAG, "Locale.getLanguage() = " + language);
			}

			if (language.equalsIgnoreCase("zh")) {
				if (country.equalsIgnoreCase("CN")) {
					lang = 1;
				} else {
					lang = 2;
				}
			} else {
				lang = 0;
			}
		}

		return lang;
	}

	// Must be called before setContentView()
	public static void changeLanguage(Context context, int lang) {
		String language = null;
		String country = null;

		switch (lang) {
			case 1:
				language = "zh";
				country = "CN";
				break;
			case 2:
				language = "zh";
				country = "TW";
				break;
			default:
				language = "en";
				country = "US";
				break;
		}

		Locale locale = new Locale(language, country);
		Configuration conf = context.getResources().getConfiguration();
		conf.locale = locale;
		context.getApplicationContext().getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
	}

	public static View addActionViewToCustom(Activity activity, int id, ViewGroup custom) {
		View v = activity.findViewById(id);

		if (v != null) {
			return addActionViewToCustom(v, custom);
		} else {
			return null;
		}
	}

	public static View addActionViewToCustom(View v, ViewGroup custom) {
		if (v != null) {
			ViewGroup parent = (ViewGroup) v.getParent();
			parent.removeView(v);
			parent.setVisibility(View.GONE);
			ViewGroup.LayoutParams params = parent.getLayoutParams();
			params.width = 0;
			params.height = 0;
			parent.setLayoutParams(params);
			custom.addView(v);
		}

		return v;
	}

	// For SDK < 18
	public static View findActionSpinner(Activity activity) {
		ActionBar action = activity.getActionBar();

		// Get ActionBarImpl class for ActionView object
		// Then get spinner from ActionView
		try {
			Class<?> clazz = Class.forName("com.android.internal.app.ActionBarImpl");
			Field f = clazz.getDeclaredField("mActionView");
			f.setAccessible(true);
			Object actionView = f.get(action);
			clazz = Class.forName("com.android.internal.widget.ActionBarView");
			f = clazz.getDeclaredField("mSpinner");
			f.setAccessible(true);
			return (View) f.get(actionView);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, "Failed to find spinner");
				Log.e(TAG, Log.getStackTraceString(e));
			}

			return null;
		}
	}

	public static boolean isChrome() {
		return Build.BRAND.equals("chromium") || Build.BRAND.equals("chrome");
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}


	public static int getActionBarHeight(Context context) {
		TypedValue v = new TypedValue();

		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, v, true)) {
			return TypedValue.complexToDimensionPixelSize(v.data, context.getResources().getDisplayMetrics());
		} else {
			return 0;
		}
	}

	public static int getDecorPaddingTop(Context context) {
		return getActionBarHeight(context);
	}

	public static void setActionBarTranslation(Activity activity, float y) {
		ViewGroup vg = (ViewGroup) activity.findViewById(android.R.id.content).getParent();
		int count = vg.getChildCount();

		if (DEBUG) {
			Log.d(TAG, "==========================");
		}

		// Get the class of action bar
		Class<?> actionBarContainer= null;
		Field isSplit = null;

		try {
			actionBarContainer = Class.forName("com.android.internal.widget.ActionBarContainer");
			isSplit = actionBarContainer.getDeclaredField("mIsSplit");
			isSplit.setAccessible(true);
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		for (int i = 0; i < count; i++) {
			View v = vg.getChildAt(i);

			if (v.getId() != android.R.id.content) {
				if (DEBUG) {
					Log.d(TAG, "Found View: " + v.getClass().getName());
				}

				try {
					if (actionBarContainer.isInstance(v)) {
						if (DEBUG) {
							Log.d(TAG, "Found ActionBarContainer");
						}

						if (isSplit.getBoolean(v)) {
							if (DEBUG) {
								Log.d(TAG, "Found Split Action Bar");
							}

							continue;
						}
					}
				} catch (Exception e) {
					if (DEBUG) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
				}

				v.setTranslationY(y);
			}
		}

		if (DEBUG) {
			Log.d(TAG, "==========================");
		}
	}

	public static String addUnitToInt(Context context, int i) {
		String tenThousand = context.getString(R.string.ten_thousand);
		String million = context.getString(R.string.million);
		String hundredMillion = context.getString(R.string.hundred_million);
		String billion = context.getString(R.string.billion);

		if (tenThousand.equals("null")) { // English-styled number format
			if (i < 1000000) {
				return String.valueOf(i);
			} else if (i < 1000000000) { // million
				return String.valueOf(i / 1000000) + million;
			} else { // billion
				return String.valueOf(i / 1000000000) + billion;
			}
		} else { // Chinese-styled number format
			if (i < 10000) {
				return String.valueOf(i);
			} else if (i < 100000000) {
				return String.valueOf(i / 10000) + tenThousand;
			} else {
				return String.valueOf(i / 100000000) + hundredMillion;
			}
		}
	}

	public static float dp2px(Context context, float dp) {
		return context.getResources().getDisplayMetrics().density * dp + 0.5f;
	}

	public static int getFontHeight(Context context, float fontSize) {
		// Convert Dp To Px
		float px = context.getResources().getDisplayMetrics().density * fontSize + 0.5f;

		// Use Paint to get font height
		Paint p = new Paint();
		p.setTextSize(px);
		FontMetrics fm = p.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.ascent);
	}

	public static Bitmap parseLongPost(Context context, String text, Bitmap pic) {
		if (DEBUG) {
			Log.d(TAG, "parseLongPost");
			Log.d(TAG, "text = " + text);
		}

		// Get width and height
		int fontHeight = getFontHeight(context, 15.0f);
		int width = fontHeight * 17;
		int height = -1; // We will calculate this later
		int picWidth = width - 20, picHeight = 0; // We will calculate this later

		// Create the paint first to measue text
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(15.0f);

		// Split the text into lines
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<HashMap<String, Integer>> format = new ArrayList<HashMap<String, Integer>>();
		String tmp = text;

		while (tmp.length() > 0) {
			if (DEBUG) {
				Log.d(TAG, "tmp = " + tmp);
			}

			String line = "";

			boolean ignore = false;

			while (tmp.length() > 0) {
				String str = tmp.substring(0, 1);

				// The escape character is "\"
				if (str.equals("\\") && !ignore) {
					// \*This is not Italic text \*
					tmp = tmp.substring(1, tmp.length());
					ignore = true;
					continue;
				}

				// Simple text formatting
				// Thanks to Markdown
				if (str.equals("_") && tmp.length() > 1 && tmp.substring(1, 2).equals("_") && !ignore) {
					// __This is bold text__
					tmp = tmp.substring(2, tmp.length());
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					map.put("line", lines.size());
					map.put("pos", line.length());
					map.put("type", 0);
					format.add(map);
					continue;
				} else if (str.equals("*") && !ignore) {
					// *This is Italic text*
					tmp = tmp.substring(1, tmp.length());
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					map.put("line", lines.size());
					map.put("pos", line.length());
					map.put("type", 1);
					format.add(map);
					continue;
				} else if (str.equals("~") && tmp.length() > 1 && tmp.substring(1, 2).equals("~") && !ignore) {
					// ~~This is deleted text~~
					tmp = tmp.substring(2, tmp.length());
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					map.put("line", lines.size());
					map.put("pos", line.length());
					map.put("type", 2);
					format.add(map);
					continue;
				} else if (str.equals("[") && tmp.length() > 1 && !ignore) {
					// Inspired from shell's coloring
					// [rRed Text[d
					// [gGreen Text[d
					// [bBlue Text[d
					// [yYellow Text[d
					// [cCyan Text[d
					// [mMagenta Text[d
					// [dDefault Color[d
					String color = tmp.substring(1, 2);
					int type = Integer.MIN_VALUE;
					if (color.equals("r")) {
						type = Color.RED;
					} else if (color.equals("g")) {
						type = Color.GREEN;
					} else if (color.equals("b")) {
						type = Color.BLUE;
					} else if (color.equals("y")) {
						type = Color.YELLOW;
					} else if (color.equals("c")) {
						type = Color.CYAN;
					} else if (color.equals("m")) {
						type = Color.MAGENTA;
					} else if (color.equals("d")) {
						type = -1;
					} else if (color.equals("#")) {
						color = tmp.substring(1, 8);
						type = Color.parseColor(color);
					}

					if (type > Integer.MIN_VALUE) {
						HashMap<String, Integer> map = new HashMap<String, Integer>();
						map.put("line", lines.size());
						map.put("pos", line.length());
						map.put("type", type);
						format.add(map);
						tmp = tmp.substring(color.length() + 1, tmp.length());
						continue;
					}
				}

				ignore = false;

				line += str;
				tmp = tmp.substring(1, tmp.length());

				if (line.contains("\n") || paint.measureText(line) >= width - fontHeight * 2) {
					break;
				}
			}

			lines.add(line);
		}

		lines.add("");

		// Generated By BlackLight
		String from = context.getResources().getString(R.string.long_from);
		lines.add(from);

		// Calculate height
		height = fontHeight * (lines.size() + 2);

		if (pic != null) {
			picHeight = (int) (((float) picWidth / (float) pic.getWidth()) * pic.getHeight());
			height += picHeight + 20;

			if (DEBUG) {
				Log.d(TAG, "picHeight = " + picHeight + "; height = " + height
					   		+ "; pic.getHeight() = " + pic.getHeight());
				Log.d(TAG, "picWidth = " + picWidth + "; pic.getWidth() = " + pic.getWidth());
			}
		}

		// Create the bitmap and draw
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		paint.setColor(context.getResources().getColor(android.R.color.background_light));
		canvas.drawRect(0, 0, width, height, paint);

		int defColor = context.getResources().getColor(R.color.darker_gray);
		paint.setColor(defColor);

		float y = fontHeight * 1.5f;
		float x = fontHeight;

		int i = 0;

		for (String line : lines) {
			if (DEBUG) {
				Log.d(TAG, "line = " + line);
				Log.d(TAG, "y = " + y);
			}

			if (line.equals(from)) {
				paint.setColor(context.getResources().getColor(R.color.gray));
			}

			int lastPos = 0;

			float xOffset = 0;

			while (format.size() > 0) {
				HashMap<String, Integer> map = format.get(0);

				if (map.get("line") != i) {
					break;
				} else {
					format.remove(0);
					int pos = map.get("pos");
					String str = line.substring(lastPos, pos);
					canvas.drawText(str, x + xOffset, y, paint);
					xOffset += paint.measureText(str);
					lastPos = pos;

					int type = map.get("type");
					switch (type) {
						case 0:
							paint.setFakeBoldText(!paint.isFakeBoldText());
							break;
						case 1:
							if (paint.getTextSkewX() >= 0.0f)
								paint.setTextSkewX(-0.25f);
							else
								paint.setTextSkewX(0.0f);
							break;
						case 2:
							paint.setStrikeThruText(!paint.isStrikeThruText());
							break;
						case -1:
							paint.setColor(defColor);
							break;
						default:
							paint.setColor(type);
							break;
					}
				}
			}

			canvas.drawText(line.substring(lastPos, line.length()), x + xOffset, y, paint);

			y += fontHeight;
			i++;
		}

		// Draw the picture
		if (pic != null) {
			canvas.drawBitmap(pic, new Rect(0, 0, pic.getWidth(), pic.getHeight()),
					new Rect(10, (int) (y + 10), picWidth + 10, (int) (picHeight + y + 10)), paint);
		}

		// Finished, return
		return bmp;
	}

	public static String parseLongContent(Context context, String content) {
		if (DEBUG) {
			Log.d(TAG, "parseLongContent");
		}

		String[] strs = content.split("\n");
		String str = "";

		if (strs.length > 0) {
			str = strs[0];
		}

		if (str.length() < 140) {
			if (TextUtils.isEmpty(str)) {
				str = context.getResources().getString(R.string.long_post);
			}

			return str;
		} else {
			return str.substring(0, 137) + "...";
		}
	}

	public static void notifyScanPhotos(Context context, String path) {
		Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(path));
		i.setData(uri);
		context.sendBroadcast(i);
	}

	public static void copyToClipboard(Context context, String data) {
		ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData cd = ClipData.newPlainText("msg", data);
		cm.setPrimaryClip(cd);

		// Inform the user
		Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show();
	}

	public static boolean isDarkMode(Context context) {
		return Settings.getInstance(context).getBoolean(Settings.THEME_DARK, false);
	}

	public static void switchTheme(Context context) {
		Settings.getInstance(context).putBoolean(Settings.THEME_DARK, !isDarkMode(context));
	}

	// Change theme to dark if dark mode is set
	public static void initDarkMode(Activity activity) {
		if (isDarkMode(activity)) {
			int theme = 0;

			try {
				theme = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0).theme;
			} catch (NameNotFoundException e) {
				return;
			}

			// Convert to dark theme
			if (theme == R.style.BL_Theme_Light) {
				theme = R.style.BL_Theme_Dark;
			} else if (theme == R.style.BL_Theme_Light_Translucent) {
				theme = R.style.BL_Theme_Dark_Translucent;
			} else if (theme == R.style.BL_Theme_Light_TranslucentActionBar_NoTranslucent) {
				theme = R.style.BL_Theme_Dark_TranslucentActionBar_NoTranslucent;
			} else if (theme == R.style.BL_Theme_Light_TranslucentActionBar) {
				theme = R.style.BL_Theme_Dark_TranslucentActionBar;
			} else if (theme == R.style.BL_Theme_Light_GradientActionBar) {
				theme = R.style.BL_Theme_Dark_GradientActionBar;
			} else if (theme == R.style.BL_Theme_Light_WithNav) {
				theme = R.style.BL_Theme_Dark_WithNav;
			}

			activity.setTheme(theme);
		}
	}

	// Change tab host's theme
	public static void initDarkTabHost(Activity activity, TabHost tabhost) {
		if (isDarkMode(activity)) {
			int textColor = 0;

			try {
				TypedArray array = activity.getTheme().obtainStyledAttributes(R.styleable.BlackLight);
				textColor = array.getColor(R.styleable.BlackLight_CardForeground, 0);
				array.recycle();
			} catch (NotFoundException e) {
				return;
			}

			for (int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
				TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
				tv.setTextColor(textColor);
			}
		}
	}

	public static int getLayerColor(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_LayerColor, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getFABBackground(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_FABBackground, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getSelectorGrey(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_SelectorGrey, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getDragBackground(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_DragBackground, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getCardSubColor(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_CardSubColor, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getDrawerForeground(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			int ret = array.getColor(R.styleable.BlackLight_DrawerForeground, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}

	}

	public static Drawable getFABNewIcon(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			Drawable ret = array.getDrawable(R.styleable.BlackLight_FABNewIcon);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return null;
		}
	}

	public static int getColorPrimary(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.Theme);
			int ret = array.getColor(R.styleable.Theme_colorPrimary, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static int getColorPrimaryDark(Context context) {
		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.Theme);
			int ret = array.getColor(R.styleable.Theme_colorPrimaryDark, 0);
			array.recycle();
			return ret;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	public static <T> T findViewById(View v, int id) {
		return (T) v.findViewById(id);
	}

	public static <T> T findViewById(Activity activity, int id) {
		return (T) activity.findViewById(id);
	}

	public static boolean isUidBanned(Context context, String uid) {
		String[] banned = context.getResources().getStringArray(R.array.banned_users);
		for (String ban : banned) {
			if (ban.equals(uid)) {
				return true;
			}
		}

		return false;
	}

	public static void bindOnClick(final Object obj, Object... viewsAndMethod) {
		final Class<?> clazz = obj.getClass();
		String method = viewsAndMethod[viewsAndMethod.length - 1].toString();
		try {
			final Method m = findMethod(clazz, method);
			m.setAccessible(true);
			View.OnClickListener listener = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try {
						m.invoke(obj);
					} catch (InvocationTargetException e) {

					} catch (IllegalAccessException e) {

					}
				}
			};

			for (Object o : viewsAndMethod) {
				if (o instanceof View) {
					((View) o).setOnClickListener(listener);
				}
			}
		} catch (NoSuchMethodException e) {

		}
	}

	public static void bindOnLongClick(final Object obj, Object... viewsAndMethod) {
		final Class<?> clazz = obj.getClass();
		String method = viewsAndMethod[viewsAndMethod.length - 1].toString();
		try {
			final Method m = findMethod(clazz, method);
			m.setAccessible(true);
			View.OnLongClickListener listener = new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						try {
							return Boolean.parseBoolean(m.invoke(obj).toString());
						} catch (InvocationTargetException e) {

						} catch (IllegalAccessException e) {

						}

						return false;
					}
				};

				for (Object o : viewsAndMethod) {
					if (o instanceof View) {
						((View) o).setOnLongClickListener(listener);
					}
				}
		} catch (NoSuchMethodException e) {

		}
	}

	public static Method findMethod(Class<?> clazz, String name) throws NoSuchMethodException {
		Class<?> cla = clazz;
		Method method = null;

		do {
			try {
				method = cla.getDeclaredMethod(name);
			} catch (NoSuchMethodException e) {
				method = null;
				cla = cla.getSuperclass();
			}
		} while (method == null && cla != Object.class);

		if (method == null) {
			throw new NoSuchMethodException();
		} else {
			return method;
		}
	}

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
		Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

    // SmartBar Support
    public static boolean hasSmartBar() {
        try {
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
        }

        if (Build.DEVICE.equals("mx2") || Build.DEVICE.equals("mx3")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }

        return false;
    }

	/**
	 *
	 * @param from color value in the form 0xAARRGGBB.
	 * @param to color value in the form 0xAARRGGBB.
	 */
	public static int getGradientColor(int from, int to, float factor){
		int r = calculateGradient(Color.red(from),Color.red(to),factor); // It's so annoying without lambda.
		int g = calculateGradient(Color.green(from),Color.green(to),factor);
		int b = calculateGradient(Color.blue(from),Color.blue(to),factor);
		int a = calculateGradient(Color.alpha(from),Color.alpha(to),factor);

		return Color.argb(a,r,g,b);
	}

	private static int calculateGradient(int from, int to, float factor){
		return from + (int)((to - from) * factor);
	}

	/** Create a file Uri for saving an image*/
	public static Uri getOutputMediaFileUri(){
		Uri uri = Uri.fromFile(getOutputImageFile());
		lastPicPath = uri.getPath();
		return uri;
	}

	/** Create a File for saving an image*/
	private static File getOutputImageFile(){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "BlackLight");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator +
				"IMG_"+ timeStamp + ".jpg");
		return mediaFile;
	}

}
