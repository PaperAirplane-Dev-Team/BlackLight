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

package us.shandian.blacklight.ui.directmessage;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.widget.SwipeRefreshLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.directmessages.DirectMessagesUserApiCache;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.DirectMessageUserAdapter;

public class DirectMessageUserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener
{
	private DirectMessagesUserApiCache mApiCache;
	private ListView mList;
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;
	private DirectMessageUserAdapter mAdapter;
	private boolean mRefreshing = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Share the view
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.home_timeline, null);
		
		mList = (ListView) v.findViewById(R.id.home_timeline);
		
		mSwipeRefresh = new SwipeUpAndDownRefreshLayout(getActivity());

		// Move child to SwipeRefreshLayout, and add SwipeRefreshLayout to root view
		v.removeViewInLayout(mList);
		v.addView(mSwipeRefresh, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mSwipeRefresh.addView(mList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(R.color.ptr_green, R.color.ptr_orange, R.color.ptr_red, R.color.ptr_blue);
		
		// Content Margin
		if (getActivity() instanceof MainActivity) {
			View header = new View(getActivity());
			LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,
					Utility.getActionBarHeight(getActivity()));

			if (Build.VERSION.SDK_INT >= 19) {
				p.height += Utility.getStatusBarHeight(getActivity());
			}

			header.setLayoutParams(p);
			mList.addHeaderView(header);
			mSwipeRefresh.setTopMargin(p.height);
		}

		mApiCache = new DirectMessagesUserApiCache(getActivity());
		mAdapter = new DirectMessageUserAdapter(getActivity(), mApiCache.mUsers);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		
		mApiCache.loadFromCache();
		
		if (mApiCache.mUsers.getSize() == 0) {
			onRefresh();
		}
		
		return v;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (!hidden) {
			getActivity().getActionBar().setTitle(R.string.direct_message);
			resume();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		resume();
	}
	
	public void resume() {
		
		Settings settings = Settings.getInstance(getActivity());

		boolean fs = settings.getBoolean(Settings.FAST_SCROLL, false);
		mList.setFastScrollEnabled(fs);

		if (fs) {
			// Scroller
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(!Utility.changeFastScrollColor(mList, getResources().getColor(R.color.gray)));
				}
			}).start();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mApiCache.cache();
	}
	
	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(!mSwipeRefresh.isDown());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(getActivity(), DirectMessageConversationActivity.class);
		i.putExtra("user", mApiCache.mUsers.get(position).user);
		startActivity(i);
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mRefreshing = true;
			mSwipeRefresh.setRefreshing(true);
		}
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			if (params[0]) {
				mApiCache.mUsers.getList().clear();
			}
			
			mApiCache.load(params[0]);
			return params[0];
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			mRefreshing = false;
			mSwipeRefresh.setRefreshing(false);
			
			if (result) {
				mAdapter.notifyDataSetChangedAndClear();
			} else {
				mAdapter.notifyDataSetChanged();
			}
		}
	}
}
