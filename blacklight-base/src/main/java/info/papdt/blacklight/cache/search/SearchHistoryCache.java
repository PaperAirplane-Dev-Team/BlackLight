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

package info.papdt.blacklight.cache.search;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import info.papdt.blacklight.cache.database.DataBaseHelper;
import info.papdt.blacklight.cache.database.tables.SearchHistoryTable;

public class SearchHistoryCache
{
	private static final int MAX_HISTORY = 5;
	private DataBaseHelper mHelper;
	
	public SearchHistoryCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
	}
	
	public List<String> getHistory() {
		Cursor cursor = mHelper.getReadableDatabase().query(SearchHistoryTable.NAME, null, null, null, null, null, SearchHistoryTable.ID + " DESC");
		
		cursor.moveToFirst();
		List<String> list = new ArrayList<String>();
		
		if (cursor.getCount() > 0) {
			do {
				list.add(cursor.getString(cursor.getColumnIndex(SearchHistoryTable.KEYWORD)));
			} while (cursor.moveToNext());
		}
		
		cursor.close();
		
		return list;
	}
	
	public void addHistory(String keyword) {
		// Get count first
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor = db.query(SearchHistoryTable.NAME, null, null, null, null, null, null);
		cursor.moveToFirst();
		int count = cursor.getCount();
		int firstId = -1;
		if (count > 0)
			firstId = cursor.getInt(cursor.getColumnIndex(SearchHistoryTable.ID));
		cursor.close();
		
		// Add
		db.close();
		db = mHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(SearchHistoryTable.KEYWORD, keyword);
		
		db.beginTransaction();
		db.insert(SearchHistoryTable.NAME, null, values);
		
		if (count == MAX_HISTORY) {
			db.delete(SearchHistoryTable.NAME, SearchHistoryTable.ID + "=" + firstId, null);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	
}
