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

package info.papdt.blacklight.ui.directmessage;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import info.papdt.blacklight.cache.directmessages.DirectMessagesMediaApiCache;
import info.papdt.blacklight.ui.common.AbsImageActivity;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class DirectMessageImageActivity extends AbsImageActivity<DirectMessagesMediaApiCache>
{
	private static final String TAG = DirectMessageImageActivity.class.getSimpleName();

	private long mFid;

	@Override
	protected DirectMessagesMediaApiCache buildApiCache() {
		return new DirectMessagesMediaApiCache(this);
	}

	@Override
	protected String saveLargePic(int current) {
		return getApiCache().saveLargePic(current);
	}

	@Override
	protected Object[] doDownload(Object[] params) {
		int id = Integer.parseInt(params[1].toString());

		if (DEBUG)
			Log.d(TAG, "Did not BOOM till id");

		Object img = getApiCache().getLargePic(mFid, (MyCallback) params[2]);

		if (DEBUG)
			Log.d(TAG, "Did not BOOM till downloading");

		mLoaded[id] = true;
		return new Object[]{params[0], img};
	}

	@Override
	protected int getCount() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFid = getIntent().getLongExtra("fid", 0);

		if (DEBUG) {
			Log.d(TAG, "mFid = " + mFid);
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
