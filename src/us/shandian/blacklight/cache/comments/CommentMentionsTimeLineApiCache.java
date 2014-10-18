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

package us.shandian.blacklight.cache.comments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.comments.CommentMentionsTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.CommentMentionsTimeLineTable;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.CommentListModel;
import us.shandian.blacklight.model.MessageListModel;

public class CommentMentionsTimeLineApiCache extends HomeTimeLineApiCache {

	public CommentMentionsTimeLineApiCache(Context context) {
		super(context);
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + CommentMentionsTimeLineTable.NAME);
		db.execSQL(CommentMentionsTimeLineTable.CREATE);

		ContentValues values = new ContentValues();
		values.put(CommentMentionsTimeLineTable.ID, 1);
		values.put(CommentMentionsTimeLineTable.JSON, new Gson().toJson((CommentListModel) mMessages));

		db.insert(CommentMentionsTimeLineTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(CommentMentionsTimeLineTable.NAME, null, null, null, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return CommentMentionsTimeLineApi.fetchCommentMentionsTimeLine(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}

	@Override
	protected Class<? extends MessageListModel> getListClass() {
		return CommentListModel.class;
	}

}
