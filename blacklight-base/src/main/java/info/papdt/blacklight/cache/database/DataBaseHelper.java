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

package info.papdt.blacklight.cache.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import info.papdt.blacklight.cache.database.tables.CommentMentionsTimeLineTable;
import info.papdt.blacklight.cache.database.tables.CommentTimeLineTable;
import info.papdt.blacklight.cache.database.tables.DirectMessageUserTable;
import info.papdt.blacklight.cache.database.tables.FavListTable;
import info.papdt.blacklight.cache.database.tables.HomeTimeLineTable;
import info.papdt.blacklight.cache.database.tables.MentionsTimeLineTable;
import info.papdt.blacklight.cache.database.tables.RepostTimeLineTable;
import info.papdt.blacklight.cache.database.tables.SearchHistoryTable;
import info.papdt.blacklight.cache.database.tables.StatusCommentTable;
import info.papdt.blacklight.cache.database.tables.UserTimeLineTable;
import info.papdt.blacklight.cache.database.tables.UsersTable;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static String DB_NAME = "weibo_data";
	private static int DB_VER = 15;
	
	private static DataBaseHelper instance;
	
	private DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UsersTable.CREATE);
		db.execSQL(HomeTimeLineTable.CREATE);
		db.execSQL(UserTimeLineTable.CREATE);
		db.execSQL(MentionsTimeLineTable.CREATE);
		db.execSQL(CommentTimeLineTable.CREATE);
		db.execSQL(CommentMentionsTimeLineTable.CREATE);
		db.execSQL(StatusCommentTable.CREATE);
		db.execSQL(RepostTimeLineTable.CREATE);
		db.execSQL(FavListTable.CREATE);
		db.execSQL(DirectMessageUserTable.CREATE);
		db.execSQL(SearchHistoryTable.CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int from, int to) {
		if (from == 13) {
			db.execSQL(DirectMessageUserTable.CREATE);
		} else if (from == 14) {
			db.execSQL(SearchHistoryTable.CREATE);
		}
	}
	
	public static synchronized DataBaseHelper instance(Context context) {
		if (instance == null) {
			instance = new DataBaseHelper(context);
		}
		
		return instance;
	}

	public void shrink() {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.delete(UsersTable.NAME, null, null);
		db.delete(HomeTimeLineTable.NAME, null, null);
		db.delete(UserTimeLineTable.NAME, null, null);
		db.delete(MentionsTimeLineTable.NAME, null, null);
		db.delete(CommentTimeLineTable.NAME, null, null);
		db.delete(CommentMentionsTimeLineTable.NAME, null, null);
		db.delete(StatusCommentTable.NAME, null, null);
		db.delete(RepostTimeLineTable.NAME, null, null);
		db.delete(FavListTable.NAME, null, null);
		db.delete(DirectMessageUserTable.NAME, null, null);
		db.delete(SearchHistoryTable.NAME, null, null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
