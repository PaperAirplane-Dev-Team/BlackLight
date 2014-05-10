package us.shandian.blacklight.cache.statuses;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.statuses.UserTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.UserTimeLineTable;
import us.shandian.blacklight.model.MessageListModel;

/* Cache api for exact user timeline */
public class UserTimeLineApiCache extends HomeTimeLineApiCache
{
	private String mUid;
	
	public UserTimeLineApiCache(Context context, String uid) {
		super(context);
		mUid = uid;
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
		return UserTimeLineApi.fetchUserTimeLine(mUid, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}
}
