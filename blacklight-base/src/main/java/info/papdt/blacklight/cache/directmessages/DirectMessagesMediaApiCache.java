/*
 * Copyright (C) 2015 Xavier Yao
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

package info.papdt.blacklight.cache.directmessages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.file.FileCacheManager;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.support.LogF;
import info.papdt.blacklight.support.Utility;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class DirectMessagesMediaApiCache {
	private static final String TAG = DirectMessagesMediaApiCache.class.getSimpleName();
	private Context mContext;
	private FileCacheManager mManager;

	public DirectMessagesMediaApiCache(Context ctx) {
		mContext = ctx;
		mManager = FileCacheManager.instance(ctx);
	}

	public String getLargePic(long fid, FileCacheManager.ProgressCallback callback) {
		String url = String.format(info.papdt.blacklight.api.Constants.DIRECT_MESSAGES_ORIG_PIC, fid, BaseApi.getAccessToken());

		if (DEBUG) {
			Log.d(TAG, "url is " + url);
		}

		String cacheName = new Long(fid).toString();
		InputStream cache;

		try {
			cache = mManager.getCache(Constants.FILE_CACHE_PICS_LARGE, cacheName);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createLargeCacheFromNetwork(Constants.FILE_CACHE_PICS_LARGE, cacheName, url, callback);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache == null) {
			return null;
		}

		try {
			cache.close();
		} catch (IOException e) {

		}

		return mManager.getCachePath(Constants.FILE_CACHE_PICS_LARGE, cacheName);
	}

	public String saveLargePic(long fid) {
		String cacheName = new Long(fid).toString();
		String ret = null;
		try {
			ret =  mManager.copyCacheTo(Constants.FILE_CACHE_PICS_LARGE, cacheName,
						    Environment.getExternalStorageDirectory().getPath() + "/BlackLight");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			// Just ignore
		} finally {
			Utility.notifyScanPhotos(mContext, ret);
			return ret;
		}
	}
}
