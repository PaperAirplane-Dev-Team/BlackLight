package us.shandian.blacklight.cache.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.CommentTimeLineTable;
import us.shandian.blacklight.cache.database.tables.CommentMentionsTimeLineTable;
import us.shandian.blacklight.cache.database.tables.UsersTable;
import us.shandian.blacklight.cache.database.tables.UserTimeLineTable;
import us.shandian.blacklight.cache.database.tables.HomeTimeLineTable;
import us.shandian.blacklight.cache.database.tables.MentionsTimeLineTable;
import us.shandian.blacklight.cache.database.tables.StatusCommentTable;

public class DataBaseHelper extends SQLiteOpenHelper
{
	private static String DB_NAME = "weibo_data";
	private static int DB_VER = 11;
	
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
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int from, int to) {
		if (from == 10) {
			db.execSQL(StatusCommentTable.CREATE);
		}
	}
	
	public static synchronized DataBaseHelper instance(Context context) {
		if (instance == null) {
			instance = new DataBaseHelper(context);
		}
		
		return instance;
	}

}
