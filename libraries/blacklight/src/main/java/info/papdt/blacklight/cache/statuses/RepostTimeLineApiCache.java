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

import info.papdt.blacklight.api.statuses.RepostTimeLineApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.database.tables.RepostTimeLineTable;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.RepostListModel;

public class RepostTimeLineApiCache extends HomeTimeLineApiCache
{
	private long mId;

	public RepostTimeLineApiCache(Context context, long id) {
		super(context);
		mId = id;
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();

		db.delete(RepostTimeLineTable.NAME, RepostTimeLineTable.MSGID + "=?", new String[]{String.valueOf(mId)});

		ContentValues values = new ContentValues();
		values.put(RepostTimeLineTable.MSGID, mId);
		values.put(RepostTimeLineTable.JSON, new Gson().toJson((RepostListModel) mMessages));

		db.insert(RepostTimeLineTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(RepostTimeLineTable.NAME, new String[]{
			RepostTimeLineTable.MSGID,
			RepostTimeLineTable.JSON
		}, RepostTimeLineTable.MSGID + "=?", new String[]{String.valueOf(mId)}, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return RepostTimeLineApi.fetchRepostTimeLine(mId, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}

	@Override
	protected Class<? extends MessageListModel> getListClass() {
		return RepostListModel.class;
	}
}
