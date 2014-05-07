package us.shandian.blacklight.cache.statuses;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.statuses.HomeTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.DataBaseHelper;
import us.shandian.blacklight.cache.database.tables.HomeTimeLineTable;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;

/* Time Line of me and my friends */
public class HomeTimeLineApiCache
{
	private DataBaseHelper mHelper;
	private FileCacheManager mManager;
	
	public MessageListModel mMessages;
	
	private int mCurrentPage = 0;

	public HomeTimeLineApiCache(Context context) {
		mHelper = DataBaseHelper.instance(context);
		mManager = FileCacheManager.instance(context);
	}
	
	public void loadFromCache() {
		Cursor cursor = mHelper.getReadableDatabase().query(HomeTimeLineTable.NAME, null, null, null, null, null, null);
		
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			mMessages = new Gson().fromJson(cursor.getString(1), MessageListModel.class);
			mCurrentPage = mMessages.getSize() / Constants.HOME_TIMELINE_PAGE_SIZE;
		} else {
			mMessages = new MessageListModel();
		}
	}
	
	public void load(boolean newWeibo) {
		if (newWeibo) {
			MessageListModel list = HomeTimeLineApi.fetchHomeTimeLine(Constants.HOME_TIMELINE_PAGE_SIZE, 1);
			int oldSize = mMessages.getSize();
			mMessages.addAll(true, list);
			if (mMessages.getSize() - oldSize > 20) {
				mMessages.getList().clear();
				mMessages.addAll(true, list);
			}
		} else {
			MessageListModel list = HomeTimeLineApi.fetchHomeTimeLine(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
			mMessages.addAll(false, list);
		}
	}
	
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + HomeTimeLineTable.NAME);
		db.execSQL(HomeTimeLineTable.CREATE);
		
		ContentValues values = new ContentValues();
		values.put(HomeTimeLineTable.ID, 1);
		values.put(HomeTimeLineTable.JSON, new Gson().toJson(mMessages));
		
		db.insert(HomeTimeLineTable.NAME, null, values);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
