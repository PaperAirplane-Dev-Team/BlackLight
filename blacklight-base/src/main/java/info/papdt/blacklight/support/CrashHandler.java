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
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.PrintWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler
{
	public static String CRASH_DIR = Environment.getExternalStorageDirectory().getPath() + "/BlackLight/";
	public static String CRASH_LOG = CRASH_DIR + "last_crash.log";
	public static String CRASH_TAG = CRASH_DIR + ".crashed";

	private static String ANDROID = Build.VERSION.RELEASE;
	private static String MODEL = Build.MODEL;
	private static String MANUFACTURER = Build.MANUFACTURER;
	
	public static String VERSION = "Unknown";

	private Thread.UncaughtExceptionHandler mPrevious;

	public static void init(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			VERSION = info.versionName + info.versionCode;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void register() {
		new CrashHandler();
	}
	
	private CrashHandler() {
		mPrevious = Thread.currentThread().getUncaughtExceptionHandler();
		Thread.currentThread().setUncaughtExceptionHandler(this);
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if("Your Fault".equals(throwable.getMessage())){
			mPrevious.uncaughtException(thread, throwable);
			return;
		}
		File f = new File(CRASH_LOG);
		if (f.exists()) {
			f.delete();
		} else {
			try {
				new File(CRASH_DIR).mkdirs();
				f.createNewFile();
			} catch (Exception e) {
				return;
			}
		}
		
		PrintWriter p;
		try {
			p = new PrintWriter(f);
		} catch (Exception e) {
			return;
		}
		
		p.write("Android Version: " + ANDROID + "\n");
		p.write("Device Model: " + MODEL + "\n");
		p.write("Device Manufacturer: " + MANUFACTURER + "\n");
		p.write("App Version: " + VERSION + "\n");
		p.write("*********************\n");
		throwable.printStackTrace(p);

		p.close();

		try {
			new File(CRASH_TAG).createNewFile();
		} catch (Exception e) {
			return;
		}
		
		if (mPrevious != null) {
			mPrevious.uncaughtException(thread, throwable);
		}
	}
}
