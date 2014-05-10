package us.shandian.blacklight.cache.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.UsersTable;
import us.shandian.blacklight.cache.database.tables.UserTimeLineTable;
import us.shandian.blacklight.cache.database.tables.HomeTimeLineTable;

public class DataBaseHelper extends SQLiteOpenHelper
{
	private static String DB_NAME = "weibo_data";
	private static int DB_VER = 5;
	
	private static DataBaseHelper instance;
	
	private DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UsersTable.CREATE);
		db.execSQL(HomeTimeLineTable.CREATE);
		db.execSQL(UserTimeLineTable.CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int from, int to) {
		if (from == 1) {
			if (to >= 2) {
				db.execSQL(HomeTimeLineTable.CREATE);
			}
			if (to >= 3) {
				db.execSQL(UserTimeLineTable.CREATE);
			}
			if (to >= 4) {
				db.execSQL(Constants.SQL_DROP_TABLE + UsersTable.NAME);
			}
			if (to >= 5) {
				db.execSQL(UsersTable.CREATE);
			}
		} else if (from == 2) {
			if (to >= 3) {
				db.execSQL(UserTimeLineTable.CREATE);
			}
			if (to >= 4) {
				db.execSQL(Constants.SQL_DROP_TABLE + UsersTable.NAME);
			}
			if (to >= 5) {
				db.execSQL(UsersTable.CREATE);
			}
		} else if (from == 3) {
			if (to >= 4) {
				db.execSQL(Constants.SQL_DROP_TABLE + UsersTable.NAME);
			}
			if (to >= 5) {
				db.execSQL(UsersTable.CREATE);
			}
		} else if (from == 4) {
			if (to >= 5) {
				db.execSQL(UsersTable.CREATE);
			}
		}
	}
	
	public static synchronized DataBaseHelper instance(Context context) {
		if (instance == null) {
			instance = new DataBaseHelper(context);
		}
		
		return instance;
	}

}
