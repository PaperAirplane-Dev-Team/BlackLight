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

package us.shandian.blacklight.ui.friendships;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import us.shandian.blacklight.R;
import us.shandian.blacklight.api.friendships.FriendsApi;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.UserAdapter;
import us.shandian.blacklight.ui.common.SwipeRefreshLayout;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
	private String mUid;
	protected UserListModel mUsers;
	private int mNextCursor = 0;
	private boolean mRefreshing = false;
	
	@InjectView(R.id.home_timeline) ListView mList;
	private UserAdapter mAdapter;
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;
	
	public FriendsFragment() {
		this(null);
	}
	
	public FriendsFragment(String uid) {
		mUid = uid;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Share the layout of Home Time Line
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.home_timeline, null);
		
		// Inject
		ButterKnife.inject(this, v);

		// Init
		mUsers = new UserListModel();
		mSwipeRefresh = new SwipeUpAndDownRefreshLayout(getActivity());

		// Content Margin
		if (getActivity() instanceof MainActivity) {
			View header = new View(getActivity());
			LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,
					Utility.getDecorPaddingTop(getActivity()));
			header.setLayoutParams(p);
			mList.addHeaderView(header);
			mSwipeRefresh.setTopMargin(p.height);
		}

		mAdapter = new UserAdapter(getActivity(), mUsers);
		mList.setAdapter(mAdapter);
		
		// Move child to SwipeRefreshLayout, and add SwipeRefreshLayout to root view
		v.removeViewInLayout(mList);
		v.addView(mSwipeRefresh, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mSwipeRefresh.addView(mList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(R.color.ptr_green, R.color.ptr_orange, R.color.ptr_red, R.color.ptr_blue);
		
		if (mUid != null) {
			onRefresh(); 
		}
		
		return v;
	}

	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(!mSwipeRefresh.isDown());
		}
	}

	@OnItemClick(R.id.home_timeline)
	public void showFriend(AdapterView<?> parent, View view, int position, long id) {
		if (getActivity() instanceof MainActivity) {
			position--; // Count the header view in
		}

		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(getActivity(), UserTimeLineActivity.class);
		i.putExtra("user", mUsers.get(position));
		startActivity(i);
	}
	
	protected void doRefresh(boolean param) {
		if (param) {
			mNextCursor = 0;
			mUsers.getList().clear();
		}

		UserListModel usr = FriendsApi.getFriendsOf(mUid, 50, mNextCursor);

		if (usr != null) {
			int nextCursor = Integer.parseInt(usr.next_cursor);
			if (param || mNextCursor != 0) {
				mNextCursor = nextCursor;
				mUsers.addAll(param, usr);
			}
		}
		
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			mRefreshing = true;
			mSwipeRefresh.setRefreshing(true);
		}
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			doRefresh(params[0]);
			
			return params[0];
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mAdapter.notifyDataSetChanged();
			
			mRefreshing = false;
			mSwipeRefresh.setRefreshing(false);
		}
	}
}
