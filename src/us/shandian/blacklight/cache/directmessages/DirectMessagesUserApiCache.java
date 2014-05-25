package us.shandian.blacklight.cache.directmessages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.directmessages.DirectMessagesApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.DataBaseHelper;
import us.shandian.blacklight.cache.database.tables.DirectMessageUserTable;
import us.shandian.blacklight.model.DirectMessageUserListModel;

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
			mUsers.addAll(toTop, m);
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
