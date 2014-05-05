package us.shandian.blacklight.cache.user;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.DataBaseHelper;
import us.shandian.blacklight.cache.database.tables.UsersTable;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.model.UserModel;

public class UserApiCache
{
	private DataBaseHelper mHelper;
	private FileCacheManager mManager;
	
	public UserApiCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
		mManager = FileCacheManager.instance(context);
	}
	
	public UserModel getUser(String uid) {
		Cursor cursor = mHelper.getReadableDatabase().query(UsersTable.NAME, new String[] {
			UsersTable.UID,
			UsersTable.TIMESTAMP,
			UsersTable.JSON
		}, UsersTable.UID + "=?", new String[]{uid}, null, null, null);
		
		UserModel model;
		
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			model = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(UsersTable.JSON)), UserModel.class);
			model.timestamp = cursor.getInt(cursor.getColumnIndex(UsersTable.TIMESTAMP));
		} else {
			model = UserApi.getUser(uid);
			
			if (model == null) {
				return null;
			}
			
			// Insert into database
			ContentValues values = new ContentValues();
			values.put(UsersTable.UID, uid);
			values.put(UsersTable.TIMESTAMP, model.timestamp);
			values.put(UsersTable.JSON, new Gson().toJson(model));
			
			SQLiteDatabase db = mHelper.getWritableDatabase();
			db.insert(UsersTable.NAME, null, values);
		}
		
		return model;
	}
	
	public Bitmap getSmallAvatar(UserModel model) {
		byte[] cache;
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
		
		return cache != null ? BitmapFactory.decodeByteArray(cache, 0, cache.length) : null;
	}
	
}
