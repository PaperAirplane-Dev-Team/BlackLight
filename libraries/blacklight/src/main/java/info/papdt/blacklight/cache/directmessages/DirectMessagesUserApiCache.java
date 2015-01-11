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

package info.papdt.blacklight.cache.directmessages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import info.papdt.blacklight.api.directmessages.DirectMessagesApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.database.DataBaseHelper;
import info.papdt.blacklight.cache.database.tables.DirectMessageUserTable;
import info.papdt.blacklight.model.DirectMessageUserListModel;

public class DirectMessagesUserApiCache
{
	public DirectMessageUserListModel mUsers = new DirectMessageUserListModel();
	private DataBaseHelper mHelper;
	
	public DirectMessagesUserApiCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
	}
	
	public void loadFromCache() {
		Cursor cursor = mHelper.getReadableDatabase().query(DirectMessageUserTable.NAME, null, null, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String json = cursor.getString(cursor.getColumnIndex(DirectMessageUserTable.JSON));
			
			DirectMessageUserListModel m = new Gson().fromJson(json, DirectMessageUserListModel.class);
			if (m != null) {
				mUsers.addAll(true, m);
			}
		}
	}
	
	public void load(boolean toTop) {
		DirectMessageUserListModel m;
		if (toTop) {
			m = DirectMessagesApi.getUserList(Constants.HOME_TIMELINE_PAGE_SIZE, 0);
		} else {
			m = DirectMessagesApi.getUserList(Constants.HOME_TIMELINE_PAGE_SIZE, Integer.parseInt(mUsers.next_cursor));
		}
		
		if (m != null) {
			if (toTop || Integer.parseInt(mUsers.next_cursor) != 0) {
				mUsers.addAll(toTop, m);
			}
		}
	}
	
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + DirectMessageUserTable.NAME);
		db.execSQL(DirectMessageUserTable.CREATE);

		ContentValues values = new ContentValues();
		values.put(DirectMessageUserTable.ID, 1);
		values.put(DirectMessageUserTable.JSON, new Gson().toJson(mUsers));

		db.insert(DirectMessageUserTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
