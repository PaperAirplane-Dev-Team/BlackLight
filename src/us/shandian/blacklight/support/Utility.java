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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import us.shandian.blacklight.service.CommentTimeLineFetcherService;

import static us.shandian.blacklight.BuildConfig.DEBUG;

/* Helper functions */
public class Utility
{
	private static final String TAG = Utility.class.getSimpleName();
	
	private static final int REQUEST_CODE = 100001;
	
	private static final int INTERVAL_THREE_MINUTES = 3 * 60 * 1000;
	
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
		startServiceAlarm(context, CommentTimeLineFetcherService.class, INTERVAL_THREE_MINUTES);
	}
	
	public static void stopServices(Context context) {
		stopServiceAlarm(context, CommentTimeLineFetcherService.class);
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
}
