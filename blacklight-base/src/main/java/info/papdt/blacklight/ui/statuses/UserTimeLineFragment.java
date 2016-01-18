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

package info.papdt.blacklight.ui.statuses;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.cache.statuses.UserTimeLineApiCache;

/* Little modification from HomeTimeLineFragment to UserTimeLineFragment */
public class UserTimeLineFragment extends TimeLineFragment
{
	private String mUid;
	private boolean mOrig;
	
	public UserTimeLineFragment() {
		init();
	}
	
	public UserTimeLineFragment(String uid, boolean orig) {
		// Should pass arguments by Bundle
		// Otherwise we will get an Exception
		Bundle args = new Bundle();
		args.putCharSequence("uid", uid);
		args.putBoolean("orig", orig);
		setArguments(args);
		init();
	}
	
	private void init() {
		mUid = getArguments().getCharSequence("uid").toString();
		mOrig = getArguments().getBoolean("orig");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		if (mShadow != null)
			mShadow.setVisibility(View.GONE);
		v.post(new Runnable() {
			@Override
			public void run() {
				doRefresh();
			}
		});
		return v;
	}
	
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new UserTimeLineApiCache(getActivity(), mUid, mOrig);
	}

	public RecyclerView getList() {
		return mList;
	}
}
