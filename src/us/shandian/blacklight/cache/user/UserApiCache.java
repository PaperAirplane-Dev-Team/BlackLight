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

package us.shandian.blacklight.cache.user;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.DataBaseHelper;
import us.shandian.blacklight.cache.database.tables.UsersTable;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class UserApiCache
{
	private static String TAG = UserApiCache.class.getSimpleName();
	
	private static BitmapDrawable[] mVipDrawable;
	
	private static HashMap<String, SoftReference<Bitmap>> mSmallAvatarCache = new HashMap<String, SoftReference<Bitmap>>();
	
	private DataBaseHelper mHelper;
	private FileCacheManager mManager;
	
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
		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_AVATAR_SMALL, model.id);
		} catch (Exception e) {
			cache = null;
		}
		
		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_AVATAR_SMALL, model.id, model.profile_image_url);
			} catch (Exception e) {
				cache = null;
			}
		}
		
		if (cache == null) {
			return null;
		} else {
			Bitmap bmp = drawVipType(model, BitmapFactory.decodeStream(cache));
			mSmallAvatarCache.put(model.id, new SoftReference<Bitmap>(bmp));

			try {
				cache.close();
			} catch (IOException e) {

			}

			return bmp;
		}
	}
	
	public Bitmap getCachedSmallAvatar(UserModel model) {
		if (mSmallAvatarCache.containsKey(model.id)) {
			return mSmallAvatarCache.get(model.id).get();
		} else {
			return null;
		}
	}
	
	public Bitmap getLargeAvatar(UserModel model) {
		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_AVATAR_LARGE, model.id);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_AVATAR_LARGE, model.id, model.avatar_large);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache != null) {
			Bitmap ret = drawVipType(model, BitmapFactory.decodeStream(cache));

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
		if (model.cover_image == null) {
			return null;
		}
		
		InputStream cache;
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_COVER, model.id);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_COVER, model.id, model.cover_image);
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
	
	private Bitmap drawVipType(UserModel model, Bitmap bitmap) {
		if (!model.verified || model.verified_type < 0) return bitmap;
		
		BitmapDrawable drawable = mVipDrawable[model.verified_type > 1 ? 1 : model.verified_type];
		Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(copy);
		int w1 = bitmap.getWidth();
		int w2 = w1 / 4;
		int h1 = bitmap.getHeight();
		int h2 = h1 / 4;
		drawable.setBounds(w1 - w2, h1 - h2, w1, h1);
		drawable.draw(canvas);
		
		bitmap.recycle();
		
		return copy;
	}
	
}
