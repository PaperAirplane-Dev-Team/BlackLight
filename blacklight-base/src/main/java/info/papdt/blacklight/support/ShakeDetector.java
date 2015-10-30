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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;

import java.util.ArrayList;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
 * Detect shaking behavior, calls listeners and vibrates when shaked
 * author: Zhen Zhiren
 * modifier: Peter Cai
 */
public class ShakeDetector implements SensorEventListener {
	
	private static final String TAG = ShakeDetector.class.getSimpleName();

	public static interface ShakeListener {
		public void onShake();
	}

	private static final int INTERVAL = 100;
	private static final int THRESHOLD = 10; // detecting threshold

	private static ShakeDetector sInstance = null; // Single instance

	private long mLastTime;

	// Last position
	// And this is a 3-D world
	private float mLastX, mLastY, mLastZ;

	private float mLastV = -1;

	private Vibrator mVibrator;

	private ArrayList<ShakeListener> mListeners = new ArrayList<ShakeListener>();

	public synchronized static ShakeDetector getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ShakeDetector(context);
		}

		return sInstance;
	}

	// Private constructor, should not be called outside
	private ShakeDetector(Context context) {
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (sensor != null) {
			manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		}

	}

	public void addListener(ShakeListener listener) {
		mListeners.add(listener);
	}

	public void removeListener(ShakeListener listener) {
		mListeners.remove(listener);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// We do not need this
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long millis = System.currentTimeMillis();
		long diff = millis - mLastTime;
		if (diff >= INTERVAL) {
			mLastTime = millis;
			float x = event.values[0],
				  y = event.values[1],
				  z = event.values[2];

			float dX = x - mLastX,
				  dY = y - mLastY,
				  dZ = z - mLastZ;

			float eV = (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ) / diff * 10000;

			if (mLastV > -1) {
				float dV = eV - mLastV;
				float a = Math.abs(dV / diff); // We do not need to detect direction

				if (a >= THRESHOLD) {
					// Call all listeners
					notifyListeners();
				}

				if (DEBUG) {
					/*Log.d(TAG, "a = " + a);*/
				}
			}

			if (DEBUG) {
				/*Log.d(TAG, "x = " + x + "; y = " + y + "; z = " + z);
				Log.d(TAG, "dX = " + dX + "; dY = " + dY + "; dZ = " + dZ);
				Log.d(TAG, "mLastX = " + mLastX + "; mLastY = " + mLastY + "; mLastZ = " + mLastZ);
				Log.d(TAG, "eV = " + eV + "; mLastV = " + mLastV);*/
			}
			
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mLastV = eV;
		}
	}

	private void notifyListeners() {
		for (ShakeListener listener : mListeners) {
			if (listener != null) {
				// Vibrate
				mVibrator.vibrate(new long[]{0, 100}, -1);

				// Call the listener
				listener.onShake();
			}
		}
	}
}
