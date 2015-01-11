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

package info.papdt.blacklight.cache.favorites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import info.papdt.blacklight.api.favorites.FavListApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.database.tables.FavListTable;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageListModel;

public class FavListApiCache extends HomeTimeLineApiCache
{
	public FavListApiCache(Context context) {
		super(context);
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + FavListTable.NAME);
		db.execSQL(FavListTable.CREATE);

		ContentValues values = new ContentValues();
		values.put(FavListTable.ID, 1);
		values.put(FavListTable.JSON, new Gson().toJson(mMessages));

		db.insert(FavListTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(FavListTable.NAME, null, null, null, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return FavListApi.fetchFavList(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}
}
