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

package us.shandian.blacklight.support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;
import android.util.TypedValue;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import us.shandian.blacklight.R;
import us.shandian.blacklight.service.CommentTimeLineFetcherService;
import us.shandian.blacklight.service.MentionsTimeLineFetcherService;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* Helper functions */
public class Utility
{
	private static final String TAG = Utility.class.getSimpleName();
	
	private static final int REQUEST_CODE = 100001;
	
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
		} catch (Exception e) {
			// Ignore the exception
		}
		
		return array[0] != 0 ? array[0] : 2048;
	}
	
	public static boolean changeFastScrollColor(AbsListView v, int color) {
		try {
			Field f = AbsListView.class.getDeclaredField("mFastScroller");
			f.setAccessible(true);
			Object o = f.get(v);
			f = f.getType().getDeclaredField("mThumbImage");
			f.setAccessible(true);
			o = f.get(o);
			((ImageView) o).setColorFilter(color);
			return true;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
			return false;
		}
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
			startServiceAlarm(context, CommentTimeLineFetcherService.class, interval);
			startServiceAlarm(context, MentionsTimeLineFetcherService.class, interval);
		}
	}
	
	public static void stopServices(Context context) {
		stopServiceAlarm(context, CommentTimeLineFetcherService.class);
		stopServiceAlarm(context, MentionsTimeLineFetcherService.class);
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
	
	public static int getActionBarHeight(Context context) {
		TypedValue v = new TypedValue();
		
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, v, true)) {
			return TypedValue.complexToDimensionPixelSize(v.data, context.getResources().getDisplayMetrics());
		} else {
			return 0;
		}
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
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

	public static int getFontHeight(Context context, float fontSize) {
		// Convert Dp To Px
		float px = context.getResources().getDisplayMetrics().density * fontSize + 0.5f;

		// Use Paint to get font height
		Paint p = new Paint();
		p.setTextSize(px);
		FontMetrics fm = p.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.ascent);
	}

	public static Bitmap parseLongPost(Context context, String text) {
		// Get width and height
		int fontHeight = getFontHeight(context, 15.0f);
		int width = fontHeight * 17;
		int height = -1; // We will calculate this later

		// Create the paint first to measue text
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(15.0f);
		
		// Split the text into lines
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<HashMap<String, Integer>> format = new ArrayList<HashMap<String, Integer>>();
		String tmp = text;

		while (tmp.length() > 0) {
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

		// Create the bitmap and draw
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		
		paint.setColor(context.getResources().getColor(android.R.color.background_light));
		canvas.drawRect(0, 0, width, height, paint);

		paint.setColor(context.getResources().getColor(R.color.darker_gray));

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

					switch (map.get("type")) {
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
					}
				}
			}

			canvas.drawText(line.substring(lastPos, line.length()), x + xOffset, y, paint);

			y += fontHeight;
			i++;
		}

		// Finished, return
		return bmp;
	}

	public static Bitmap decodeStreamByRegion(InputStream in) {
		// Initialize the decoder
		BitmapRegionDecoder de = null;
		
		try {
			de = BitmapRegionDecoder.newInstance(in, true);
		} catch (Exception e) {
			return null;
		}
		
		// Create a empty Bitmap and a Canvas
		Bitmap bmp = Bitmap.createBitmap(de.getWidth(), de.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bmp);

		// Decode by Region
		int height = de.getHeight(), decodedHeight = 0;

		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inPreferredConfig = Bitmap.Config.RGB_565;

		while (decodedHeight < height) {

			if (DEBUG) {
				Log.d(TAG, "decodedHeight = " + decodedHeight);
				Log.d(TAG, "height = " + height);
			}

			Rect rect = null;

			if (height - decodedHeight < 100) {
				rect = new Rect(0, decodedHeight, de.getWidth(), height);
			} else {
				rect = new Rect(0, decodedHeight, de.getWidth(), decodedHeight + 100);
			}

			Bitmap tmp = de.decodeRegion(rect, op);

			if (tmp == null) {
				break;
			} else {
				canvas.drawBitmap(tmp, new Rect(0, 0, tmp.getWidth(), tmp.getHeight()), rect, null);
			}

			tmp.recycle();

			decodedHeight += 100;
		}

		// Finished
		return bmp;
	}
	
	@TargetApi(19)
	public static void enableTint(Activity activity) {
		if (Build.VERSION.SDK_INT < 19) return;
		
		Window w = activity.getWindow();
		WindowManager.LayoutParams p = w.getAttributes();
		p.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		w.setAttributes(p);
		
		SystemBarTintManager m = new SystemBarTintManager(activity);
		m.setStatusBarTintEnabled(true);
		m.setStatusBarTintResource(R.color.action_gray);
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

}
