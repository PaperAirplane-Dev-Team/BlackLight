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

package info.papdt.blacklight.cache.statuses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import info.papdt.blacklight.api.statuses.UserTimeLineApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.database.tables.UserTimeLineTable;
import info.papdt.blacklight.model.MessageListModel;

/* Cache api for exact user timeline */
public class UserTimeLineApiCache extends HomeTimeLineApiCache
{
	private String mUid;
	private boolean mOrig;
	
	public UserTimeLineApiCache(Context context, String uid, boolean orig) {
		super(context);
		mUid = uid;
		mOrig = orig;
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		
		db.beginTransaction();
		db.delete(UserTimeLineTable.NAME, UserTimeLineTable.UID + "=?", new String[]{mUid});
		
		ContentValues values = new ContentValues();
		values.put(UserTimeLineTable.UID, mUid);
		values.put(UserTimeLineTable.JSON, new Gson().toJson(mMessages));
		
		db.insert(UserTimeLineTable.NAME, null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(UserTimeLineTable.NAME, new String[]{
			UserTimeLineTable.UID,
			UserTimeLineTable.JSON
		}, UserTimeLineTable.UID + "=?", new String[]{mUid}, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return UserTimeLineApi.fetchUserTimeLine(mUid, Constants.HOME_TIMELINE_PAGE_SIZE,
				++mCurrentPage, mOrig);
	}
}
