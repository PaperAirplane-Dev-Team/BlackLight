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

package info.papdt.blacklight.cache.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.user.UserApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.database.DataBaseHelper;
import info.papdt.blacklight.cache.database.tables.UsersTable;
import info.papdt.blacklight.cache.file.FileCacheManager;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.Utility;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class UserApiCache
{
	private static String TAG = UserApiCache.class.getSimpleName();

	private static BitmapDrawable[] mVipDrawable;

	private static boolean sAmIMale = true; // For gender display.

	private static HashMap<String, WeakReference<Bitmap>> mSmallAvatarCache = new HashMap<String, WeakReference<Bitmap>>();

	private DataBaseHelper mHelper;
	private FileCacheManager mManager;

	public static boolean amIMale() {
		return sAmIMale;
	}

	public static void setAmIMale(boolean am) {
		sAmIMale = am;
	}

	public UserApiCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
		mManager = FileCacheManager.instance(context);

		if (mVipDrawable == null) {
			mVipDrawable = new BitmapDrawable[]{
				(BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_personal_vip),
				(BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_enterprise_vip)
			};
		}
	}

	public UserModel getUser(String uid) {
		UserModel model;

		model = UserApi.getUser(uid);

		if (model == null) {
			Cursor cursor = mHelper.getReadableDatabase().query(UsersTable.NAME, new String[] {
				UsersTable.UID,
				UsersTable.TIMESTAMP,
				UsersTable.USERNAME,
				UsersTable.JSON
			}, UsersTable.UID + "=?", new String[]{uid}, null, null, null);

			if (cursor.getCount() >= 1) {
				cursor.moveToFirst();

				long time = cursor.getLong(cursor.getColumnIndex(UsersTable.TIMESTAMP));

				if (DEBUG) {
					Log.d(TAG, "time = " + time);
					Log.d(TAG, "available = " + Utility.isCacheAvailable(time, Constants.DB_CACHE_DAYS));
				}

				if (Utility.isCacheAvailable(time, Constants.DB_CACHE_DAYS)) {
					model = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(UsersTable.JSON)), UserModel.class);
					model.timestamp = cursor.getInt(cursor.getColumnIndex(UsersTable.TIMESTAMP));
				}
			}
		} else {

			// Insert into database
			ContentValues values = new ContentValues();
			values.put(UsersTable.UID, uid);
			values.put(UsersTable.TIMESTAMP, model.timestamp);
			values.put(UsersTable.USERNAME, model.getName());
			values.put(UsersTable.JSON, new Gson().toJson(model));

			SQLiteDatabase db = mHelper.getWritableDatabase();
			db.beginTransaction();
			db.delete(UsersTable.NAME, UsersTable.UID + "=?", new String[]{uid});
			db.insert(UsersTable.NAME, null, values);
			db.setTransactionSuccessful();
			db.endTransaction();

		}

		return model;
	}

	public UserModel getUserByName(String name) {
		UserModel model;

		model = UserApi.getUserByName(name);

		if (model == null) {
			Cursor cursor = mHelper.getReadableDatabase().query(UsersTable.NAME, new String[] {
				UsersTable.UID,
				UsersTable.TIMESTAMP,
				UsersTable.USERNAME,
				UsersTable.JSON
			}, UsersTable.USERNAME + "=?", new String[]{name}, null, null, null);

			if (cursor.getCount() >= 1) {
				cursor.moveToFirst();

				long time = cursor.getLong(cursor.getColumnIndex(UsersTable.TIMESTAMP));

				if (DEBUG) {
					Log.d(TAG, "time = " + time);
					Log.d(TAG, "available = " + Utility.isCacheAvailable(time, Constants.DB_CACHE_DAYS));
				}

				if (Utility.isCacheAvailable(time, Constants.DB_CACHE_DAYS)) {
					model = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(UsersTable.JSON)), UserModel.class);
					model.timestamp = cursor.getInt(cursor.getColumnIndex(UsersTable.TIMESTAMP));
				}
			}
		} else {

			// Insert into database
			ContentValues values = new ContentValues();
			values.put(UsersTable.UID, model.id);
			values.put(UsersTable.TIMESTAMP, model.timestamp);
			values.put(UsersTable.USERNAME, name);
			values.put(UsersTable.JSON, new Gson().toJson(model));

			SQLiteDatabase db = mHelper.getWritableDatabase();
			db.beginTransaction();
			db.delete(UsersTable.NAME, UsersTable.USERNAME + "=?", new String[]{name});
			db.insert(UsersTable.NAME, null, values);
			db.setTransactionSuccessful();
			db.endTransaction();

		}

		return model;
	}

	public Bitmap getSmallAvatar(UserModel model) {
		String cacheName = model.id + model.profile_image_url.replaceAll("/", ".").replaceAll(":", "");
		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_AVATAR_SMALL, cacheName);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_AVATAR_SMALL, cacheName, model.profile_image_url);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache == null) {
			return null;
		} else {
			Bitmap bmp = BitmapFactory.decodeStream(cache);
			mSmallAvatarCache.put(model.id, new WeakReference<Bitmap>(bmp));

			try {
				cache.close();
			} catch (IOException e) {

			}

			return bmp;
		}
	}

	public Bitmap getCachedSmallAvatar(UserModel model) {
		WeakReference<Bitmap> ref = mSmallAvatarCache.get(model.id);
		return ref == null ? null : ref.get();
	}

	public Bitmap getLargeAvatar(UserModel model) {
		String cacheName = model.id + model.avatar_large.replaceAll("/", ".").replaceAll(":", "");
		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_AVATAR_LARGE, cacheName);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_AVATAR_LARGE, cacheName, model.avatar_large);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache != null) {
			Bitmap ret = BitmapFactory.decodeStream(cache);

			try {
				cache.close();
			} catch (IOException e) {

			}

			return ret;
		} else {
			return null;
		}
	}

	public Bitmap getCover(UserModel model) {
		String url = model.getCover();
		if (url.trim().equals("")) {
			return null;
		}

		if (DEBUG) {
			Log.d(TAG, "url = " + url);
		}

		String cacheName = model.id + url.substring(url.lastIndexOf("/") + 1, url.length());

		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_COVER, cacheName);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_COVER, cacheName, url);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache != null) {
			Bitmap ret = BitmapFactory.decodeStream(cache);

			try {
				cache.close();
			} catch (IOException e) {

			}

			return ret;
		} else {
			return null;
		}
	}

}
