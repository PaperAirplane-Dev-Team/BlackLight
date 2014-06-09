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

package us.shandian.blacklight.ui.statuses;

import android.app.Fragment;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ConcurrentModificationException;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.WeiboAdapter;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;

public class HomeTimeLineFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener
{
	private ListView mList;
	private View mNew;
	private WeiboAdapter mAdapter;
	private HomeTimeLineApiCache mCache;
	
	// Pull To Refresh
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;
	
	private boolean mRefreshing = false;
	
	protected boolean mBindOrig = true;
	protected boolean mShowCommentStatus = true;
	
	private int mLastCount = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		initTitle();
		
		View v = inflater.inflate(R.layout.home_timeline, null);
		mList = (ListView) v.findViewById(R.id.home_timeline);
		mCache = bindApiCache();
		mCache.loadFromCache();
		mAdapter = new WeiboAdapter(getActivity(), mList, mCache.mMessages, mBindOrig, mShowCommentStatus);
		mList.setAdapter(mAdapter);
		
		mList.setDrawingCacheEnabled(true);
		mList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		mList.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE | ViewGroup.PERSISTENT_SCROLLING_CACHE);
		
		// Swipe To Refresh
		bindSwipeToRefresh((ViewGroup) v);
		
		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute(new Boolean[]{true});
		}
		
		// Floating "New" button
		bindNewButton(v);
		
		return v;
	}

	@Override
	public void onStop() {
		super.onStop();
		
		try {
			mCache.cache();
		} catch (ConcurrentModificationException e) {
			
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (!hidden) {
			initTitle();
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
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(new Boolean[]{!mSwipeRefresh.isDown()});
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mNew) {
			newPost();
		}
	}
	
	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}
	
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.timeline);
	}
	
	protected void bindSwipeToRefresh(ViewGroup v) {
		mSwipeRefresh = new SwipeUpAndDownRefreshLayout(getActivity());
		
		// Move child to SwipeRefreshLayout, and add SwipeRefreshLayout to root view
		v.removeViewInLayout(mList);
		v.addView(mSwipeRefresh, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mSwipeRefresh.addView(mList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
									 android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
	}
	
	protected void bindNewButton(View v) {
		mNew = v.findViewById(R.id.home_timeline_new);
		mNew.setVisibility(View.VISIBLE);
		mNew.bringToFront();
		mNew.setOnClickListener(this);
	}
	
	protected void newPost() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(getActivity(), NewPostActivity.class);
		startActivity(i);
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Boolean>
	{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLastCount = mCache.mMessages.getSize();
			mRefreshing = true;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(true);
			}
		}
		
		@Override
		protected Boolean doInBackground(Boolean[] params) {
			mCache.load(params[0]);
			return params[0];
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter.notifyDataSetChangedAndClear();
			}
			mRefreshing = false;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(false);
			}
		}

		
	}
}
