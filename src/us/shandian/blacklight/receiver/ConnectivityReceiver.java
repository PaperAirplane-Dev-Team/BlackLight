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

package us.shandian.blacklight.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class ConnectivityReceiver extends BroadcastReceiver
{
	private static final String TAG = ConnectivityReceiver.class.getSimpleName();
	
	public static boolean isWIFI = true;
	
	public static boolean isNet = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			if (DEBUG) {
				Log.d(TAG, "Network connected");
			}
			
			Utility.startServices(context);

			isNet = true;

			isWIFI = (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
		} else {
			if (DEBUG) {
				Log.d(TAG, "Network disconnected");
			}

			Utility.stopServices(context);

			isNet = false;
		}
	}
}
