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

package us.shandian.blacklight.cache.file;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;

import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.support.Utility;

public class FileCacheManager
{
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
	
	public byte[] createCacheFromNetwork(String type, String name, String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		byte[] buf = readInputStream(conn.getInputStream());
		createCache(type, name, buf);
		return buf;
	}
	
	public byte[] getCache(String type, String name) throws IOException {
		String path = mCacheDir.getPath() + "/" + type + "/" + name;
		File f = new File(path);
		if (!f.exists()) {
			return null;
		} else {
			FileInputStream ipt = new FileInputStream(path);
			return readInputStream(ipt);
		}
	}
	
	private byte[] readInputStream(InputStream in) throws IOException {
		ByteArrayOutputStream opt = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			opt.write(buf, 0, len);
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
		clearUnavailable(mCacheDir);
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
