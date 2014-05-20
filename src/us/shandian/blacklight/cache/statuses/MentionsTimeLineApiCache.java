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

import com.google.gson.Gson;

import us.shandian.blacklight.api.statuses.MentionsTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.MentionsTimeLineTable;
import us.shandian.blacklight.model.MessageListModel;

/* Mainly similiar with Home Time Line, 
   but it only shows messages metioning me */
public class MentionsTimeLineApiCache extends HomeTimeLineApiCache
{
	
	public MentionsTimeLineApiCache(Context context) {
		super(context);
	}
	
	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + MentionsTimeLineTable.NAME);
		db.execSQL(MentionsTimeLineTable.CREATE);

		ContentValues values = new ContentValues();
		values.put(MentionsTimeLineTable.ID, 1);
		values.put(MentionsTimeLineTable.JSON, new Gson().toJson(mMessages));

		db.insert(MentionsTimeLineTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(MentionsTimeLineTable.NAME, null, null, null, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return MentionsTimeLineApi.fetchMentionsTimeLine(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}
	
}
