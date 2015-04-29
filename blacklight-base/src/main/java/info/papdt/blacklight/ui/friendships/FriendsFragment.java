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

package info.papdt.blacklight.ui.friendships;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.FriendsApi;
import info.papdt.blacklight.model.UserListModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.UserAdapter;
import info.papdt.blacklight.ui.main.MainActivity;
import info.papdt.blacklight.ui.statuses.AbsTimeLineFragment;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

public class FriendsFragment extends AbsTimeLineFragment<UserAdapter> implements SwipeRefreshLayout.OnRefreshListener {
	private String mUid;
	protected UserListModel mUsers;
	private int mNextCursor = 0;
	
	private boolean mIsFriends;
	
	public FriendsFragment() {
		init();
	}
	
	public FriendsFragment(String uid, boolean friends) {
		Bundle args = new Bundle();
		args.putCharSequence("uid", uid);
        args.putBoolean("isFriends", friends);
		setArguments(args);
		init();
	}
	
	private void init() {
		if (getArguments() != null) {
			mUid = getArguments().getCharSequence("uid").toString();
            mIsFriends = getArguments().getBoolean("isFriends");
		} else {
			mUid = null;
		}
	}

	@Override
	protected void onCreateCache() {
		mUsers = new UserListModel();
	}
	
	@Override
	protected UserAdapter buildAdapter() {
		return new UserAdapter(getActivity(), mUsers, mList);
	}
	
	@Override
	protected void loadFromCache() {
		doRefresh(true);
	}
	
	@Override
	protected void cacheLoadNew(boolean param) {
		doRefresh(param);
	}
	
	@Override
	protected int getCacheSize() {
		return mUsers.getSize();
	}
	
	@Override
	protected int getCurrentItemCount() {
		return mAdapter.getCount();
	}
	
	@Override
	protected void initTitle() {
		
	}
	
	protected void doRefresh(boolean param) {
		if (param) {
			mNextCursor = 0;
			mUsers.getList().clear();
		}

        UserListModel usr;
        if(mIsFriends) usr = FriendsApi.getFriendsOf(mUid, 50, mNextCursor);
        else usr = FriendsApi.getFollowersOf(mUid, 50, mNextCursor);

		if (usr != null) {
			int nextCursor = Integer.parseInt(usr.next_cursor);
			if (param || mNextCursor != 0) {
				mNextCursor = nextCursor;
				mUsers.addAll(param, usr);
			}
		}

	}
}
