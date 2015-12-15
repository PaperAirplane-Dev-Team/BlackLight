/* 
 * Copyright (C) 2015 Peter Cai
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

package info.papdt.blacklight.support;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
  Real AsyncTask
*/

public abstract class AsyncTask<Params, Progress, Result>
{
	private static final String TAG = AsyncTask.class.getSimpleName();
	
	private static class AsyncResult<Data> {
		public AsyncTask task;
		public Data[] data;
		
		public AsyncResult(AsyncTask task, Data... data) {
			this.task = task;
			this.data = data;
		}
	}
	
	private static final int MSG_FINISH = 1000;
	private static final int MSG_PROGRESS = 1001;
	
	private Handler mInternalHandler = new Handler() {
		@SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
		@Override
		public void handleMessage(Message msg) {
			AsyncResult result = (AsyncResult) msg.obj;
			switch (msg.what) {
				case MSG_FINISH:
					result.task.onPostExecute(result.data[0]);
					break;
				case MSG_PROGRESS:
					result.task.onProgressUpdate(result.data);
					break;
			}
		}
	};
	
	private Params[] mParams;
	
	private Thread mThread = new Thread(new Runnable() {
		@Override
		public void run() {
			CrashHandler.register();
			
			try {
				Result result = doInBackground(mParams);
				mInternalHandler.sendMessage(mInternalHandler.obtainMessage(MSG_FINISH, new AsyncResult<Result>(AsyncTask.this, result)));
			} catch (Exception e) {
				// Don't crash the whole app
				if (DEBUG) {
					Log.d(TAG, e.getClass().getSimpleName() + " caught when running background task. Printing stack trace.");
					Log.d(TAG, Log.getStackTraceString(e));
				}
			}
			
			Thread.currentThread().interrupt();
		}
	});
	
	protected void onPostExecute(Result result) {}
	
	protected abstract Result doInBackground(Params... params);
	
	protected void onPreExecute() {}
	
	protected void onProgressUpdate(Progress... progress) {}
	
	protected void publishProgress(Progress... progress) {
		mInternalHandler.sendMessage(mInternalHandler.obtainMessage(MSG_PROGRESS, new AsyncResult<Progress>(this, progress)));
	}
	
	public void execute(Params... params) {
		onPreExecute();
		mParams = params;
		mThread.start();
	}
}
