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

package us.shandian.blacklight.cache.statuses;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import us.shandian.blacklight.api.statuses.HomeTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.DataBaseHelper;
import us.shandian.blacklight.cache.database.tables.HomeTimeLineTable;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.Utility;

/* Time Line of me and my friends */
public class HomeTimeLineApiCache
{
	private static HashMap<Long, SoftReference<Bitmap>> mThumnnailCache = new HashMap<Long, SoftReference<Bitmap>>();
	
	protected DataBaseHelper mHelper;
	protected FileCacheManager mManager;
	
	public MessageListModel mMessages;

	private Context mContext;
	
	protected int mCurrentPage = 0;

	public HomeTimeLineApiCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
		mManager = FileCacheManager.instance(context);
		mContext = context;
	}
	
	public void loadFromCache() {
		Cursor cursor = query();
		
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			mMessages = new Gson().fromJson(cursor.getString(1), getListClass());
			mCurrentPage = mMessages.getSize() / Constants.HOME_TIMELINE_PAGE_SIZE;
			mMessages.spanAll(mContext);
		} else {
			try {
				mMessages = getListClass().newInstance();
			} catch (Exception e) {
				mMessages = new MessageListModel();
			}
		}
	}
	
	public void load(boolean newWeibo) {
		if (newWeibo) {
			mCurrentPage = 0;
		}
		
		MessageListModel list = load();
		
		if (newWeibo) {
			mMessages.getList().clear();
		}
		
		mMessages.addAll(false, list);
	}
	
	public Bitmap getThumbnailPic(MessageModel msg, int id) {
		String url = null;
		if (msg.hasMultiplePictures()) {
			url = msg.pic_urls.get(id).getThumbnail();
		} else if (id == 0) {
			url = msg.thumbnail_pic;
		} else {
			return null;
		}
		
		if (url == null) {
			return null;
		}
		
		String cacheName = url.substring(url.lastIndexOf("/") + 1, url.length());
		InputStream cache;
		
		try {
			cache = mManager.getCache(Constants.FILE_CACHE_PICS_SMALL, cacheName);
		} catch (Exception e) {
			cache = null;
		}
		
		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_PICS_SMALL, cacheName, url);
			} catch (Exception e) {
				cache = null;
			}
		}
		
		if (cache == null) {
			return null;
		}
		
		Bitmap bmp = BitmapFactory.decodeStream(cache);
		mThumnnailCache.put(msg.id * 10 + id, new SoftReference<Bitmap>(bmp));

		try {
			cache.close();
		} catch (IOException e) {
			// Do nothing
			// But this exception might cause memory leak
			// I have no idea about it
		}

		return bmp;
	}
	
	public Bitmap getCachedThumbnail(MessageModel msg, int id) {
		long key = msg.id * 10 + id;
		
		if (mThumnnailCache.containsKey(key)) {
			return mThumnnailCache.get(key).get();
		} else {
			return null;
		}
	}
	
	public Object getLargePic(MessageModel msg, int id) {
		String url = null;
		if (msg.hasMultiplePictures()) {
			url = msg.pic_urls.get(id).getLarge();
		} else if (id == 0) {
			url = msg.original_pic;
		} else {
			return null;
		}

		if (url == null) {
			return null;
		}

		String cacheName = url.substring(url.lastIndexOf("/") + 1, url.length());
		InputStream cache;

		try {
			cache = mManager.getCache(Constants.FILE_CACHE_PICS_LARGE, cacheName);
		} catch (Exception e) {
			cache = null;
		}

		if (cache == null) {
			try {
				cache = mManager.createCacheFromNetwork(Constants.FILE_CACHE_PICS_LARGE, cacheName, url);
			} catch (Exception e) {
				cache = null;
			}
		}

		if (cache == null) {
			return null;
		}

		if (cacheName.endsWith(".gif")) {
			Movie movie = Movie.decodeStream(cache);
			
			// A real movie must have a dutation bigger than 0
			// Or it is just a static picture
			if (movie.duration() > 0) {
				return movie;
			}
		} 
		
		Bitmap ret = null;
		try {
			ret = BitmapFactory.decodeStream(cache);
		} catch (OutOfMemoryError e) {
			// If OOM, compress and decode.
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(cache, null, opt);
			opt.inSampleSize = Utility.computeSampleSize(opt, 512, 1024 * 1024);
			opt.inJustDecodeBounds = false;
			ret = BitmapFactory.decodeStream(cache, null, opt);
		}
		
		try {
			cache.close();
		} catch (IOException e) {
			
		}
		
		return ret;
	}

	public String saveLargePic(MessageModel msg, int id) {
		String url = null;
		if (msg.hasMultiplePictures()) {
			url = msg.pic_urls.get(id).getLarge();
		} else if (id == 0) {
			url = msg.original_pic;
		} else {
			return null;
		}

		if (url == null) {
			return null;
		}

		String cacheName = url.substring(url.lastIndexOf("/") + 1, url.length());
		try {
			return mManager.copyCacheTo(Constants.FILE_CACHE_PICS_LARGE, cacheName, "/sdcard/BlackLight");
		} catch (Exception e) {
			return null;
		}
	}
	
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + HomeTimeLineTable.NAME);
		db.execSQL(HomeTimeLineTable.CREATE);
		
		ContentValues values = new ContentValues();
		values.put(HomeTimeLineTable.ID, 1);
		values.put(HomeTimeLineTable.JSON, new Gson().toJson(mMessages));
		
		db.insert(HomeTimeLineTable.NAME, null, values);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(HomeTimeLineTable.NAME, null, null, null, null, null, null);
	}
	
	protected MessageListModel load() {
		return HomeTimeLineApi.fetchHomeTimeLine(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}
	
	protected Class<? extends MessageListModel> getListClass() {
		return MessageListModel.class;
	}
}
