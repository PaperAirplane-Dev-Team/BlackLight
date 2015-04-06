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

package info.papdt.blacklight.cache.file;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.support.Utility;

public class FileCacheManager
{

	public static interface ProgressCallback {
		void onProgressChanged(int read, int total);
		boolean shouldContinue();
	}

	private static FileCacheManager mInstance;
	
	private File mCacheDir;
	
	private FileCacheManager(Context context) {
		mCacheDir = context.getExternalCacheDir();
	}
	
	public static synchronized FileCacheManager instance(Context context) {
		if (mInstance == null) {
			mInstance = new FileCacheManager(context);
		}
		
		return mInstance;
	}
	
	public void createCache(String type, String name, byte[] data) throws IOException {
		String path = mCacheDir.getPath() + "/" + type + "/" + name;
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		f.getParentFile().mkdirs();
		f.createNewFile();
		
		FileOutputStream opt = new FileOutputStream(path);
		opt.write(data);
		opt.close();
	}

	public String copyCacheTo(String type, String name, String dist) throws IOException {
		String path = mCacheDir.getPath() + "/" + type + "/" + name;

		try {
			new File(dist).mkdirs();
		} catch (Exception e) {
			Runtime.getRuntime().exec("mkdir -p " + dist);
		}
		
		File origFile = new File(path);
		File distFile = new File(dist + "/" + name);
		if (distFile.createNewFile()) {
			FileInputStream ipt = new FileInputStream(origFile);
			FileOutputStream opt = new FileOutputStream(distFile);
			
			byte[] buf = new byte[1024];
			int len = 0;

			while ((len = ipt.read(buf)) != -1) {
				opt.write(buf, 0, len);
			}

			opt.close();
			ipt.close();
		}

		return distFile.getAbsolutePath();
	}

	public InputStream createCacheFromNetwork(String type, String name, String url) throws IOException {
		return createCacheFromNetwork(type, name, url, null);
	}
	
	public InputStream createCacheFromNetwork(String type, String name, String url, ProgressCallback callback) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		byte[] buf = readInputStream(conn.getInputStream(), conn.getContentLength(), callback);
		createCache(type, name, buf);
		conn.disconnect();

		// Read From file
		return getCache(type, name);
	}
	
	// To prevent OOM while loading large contents
	public InputStream createLargeCacheFromNetwork(String type, String name, String url, ProgressCallback callback) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		createCacheFromStream(type, name, conn.getInputStream(), conn.getContentLength(), callback);
		conn.disconnect();

		// Read From file
		return getCache(type, name);
	}
	
	public void createCacheFromStream(String type, String name, InputStream ipt, int total, ProgressCallback callback) throws IOException {
		String path = getCachePath(type, name);
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		f.getParentFile().mkdirs();
		f.createNewFile();
		
		FileOutputStream opt = new FileOutputStream(f);
		byte[] buf = new byte[512];
		int len = 0, read = 0;
		
		while ((len = ipt.read(buf)) != -1) {
			opt.write(buf, 0, len);
			read += len;
			callback.onProgressChanged(read, total);
			
			if (!callback.shouldContinue()) {
				opt.close();
				ipt.close();
				f.delete();
				return;
			}
		}
		
		opt.close();
		ipt.close();
	}
	
	public InputStream getCache(String type, String name) throws IOException {
		String path = mCacheDir.getPath() + "/" + type + "/" + name;
		File f = new File(path);
		if (!f.exists()) {
			return null;
		} else {
			FileInputStream ipt = new FileInputStream(path);
			return ipt;
		}
	}

	public String getCachePath(String type, String name) {
		return mCacheDir.getPath() + "/" + type + "/" + name;
	}
	
	private byte[] readInputStream(InputStream in, int total, ProgressCallback callback) throws IOException {
		ByteArrayOutputStream opt = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0, read = 0;
		while ((len = in.read(buf)) != -1) {
			opt.write(buf, 0, len);
			read += len;

			if (callback != null) {
				callback.onProgressChanged(read, total);
			}
		}
		in.close();
		byte[] ret;
		try {
			ret = opt.toByteArray();
		} catch (OutOfMemoryError e) {
			ret = null;
		}
		opt.close();
		return ret;
	}
	
	public void clearUnavailable() {
		try {
			clearUnavailable(mCacheDir);
		} catch (NullPointerException e) {
			// NPE is caused by unmounted sdcard
			// Just ignore
		}
	}
	
	private void clearUnavailable(File dir) {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				clearUnavailable(f);
			} else {
				long time = f.lastModified();
				if (!Utility.isCacheAvailable(time, Constants.FILE_CACHE_DAYS)) {
					f.delete();
				}
			}
		}
	}
}
