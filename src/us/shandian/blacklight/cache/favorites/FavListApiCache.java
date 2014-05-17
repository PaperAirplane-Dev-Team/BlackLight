package us.shandian.blacklight.cache.favorites;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.favorites.FavListApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.FavListTable;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageListModel;

public class FavListApiCache extends HomeTimeLineApiCache
{
	public FavListApiCache(Context context) {
		super(context);
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL(Constants.SQL_DROP_TABLE + FavListTable.NAME);
		db.execSQL(FavListTable.CREATE);

		ContentValues values = new ContentValues();
		values.put(FavListTable.ID, 1);
		values.put(FavListTable.JSON, new Gson().toJson(mMessages));

		db.insert(FavListTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(FavListTable.NAME, null, null, null, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return FavListApi.fetchFavList(Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}
}
