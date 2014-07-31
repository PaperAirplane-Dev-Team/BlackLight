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
import android.graphics.BitmapFactory;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
		
		if (array[0] == 0) {
			GLES11.glGetIntegerv(GLES11.GL_MAX_TEXTURE_SIZE, array, 0);
			
			if (array[0] == 0) {
				GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, array, 0);
				
				if (array[0] == 0) {
					GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, array, 0);
				}
			}
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

						if (!isSplit.getBoolean(v)) {
							v.setTranslationY(y);
						} else {
							if (DEBUG) {
								Log.d(TAG, "Found Split Action Bar");
							}
						}
					}
				} catch (Exception e) {
					if (DEBUG) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
				}
			}
		}

		if (DEBUG) {
			Log.d(TAG, "==========================");
		}
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
