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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.WeiboAdapter;
import us.shandian.blacklight.ui.common.ToolbarActivity;
import us.shandian.blacklight.ui.main.MainActivity;

public abstract class TimeLineFragment extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, MainActivity.Refresher {
	
	private static final String TAG = TimeLineFragment.class.getSimpleName();

	protected RecyclerView mList;
	protected View mShadow;
	private WeiboAdapter mAdapter;
	private LinearLayoutManager mManager;
	protected HomeTimeLineApiCache mCache;

	private Settings mSettings;
	
	protected ActionBar mActionBar = null;
	protected Toolbar mToolbar = null;
	private int mActionBarHeight = 0;
	private int mTranslationY = 0;
	private int mLastY = 0;

	// Pull To Refresh
	private SwipeRefreshLayout mSwipeRefresh;

	private boolean mRefreshing = false;

	protected boolean mBindOrig = true;
	protected boolean mShowCommentStatus = true;
	protected boolean mAllowHidingActionBar = true;
	private boolean mFABShowing = true;

	private int mLastCount = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mActionBar = ((ToolbarActivity) getActivity()).getSupportActionBar();
		mToolbar = ((ToolbarActivity) getActivity()).getToolbar();
		initTitle();
		mSettings = Settings.getInstance(getActivity().getApplicationContext());

		View v = inflater.inflate(R.layout.home_timeline, null);
		
		// Initialize views
		mList = Utility.findViewById(v, R.id.home_timeline);
		mShadow = Utility.findViewById(v, R.id.action_shadow);

		mCache = bindApiCache();
		mCache.loadFromCache();

		mList.setDrawingCacheEnabled(true);
		mList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		mList.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE
				| ViewGroup.PERSISTENT_SCROLLING_CACHE);
				
		mManager = new LinearLayoutManager(getActivity());
		mList.setLayoutManager(mManager);

		// Swipe To Refresh
		bindSwipeToRefresh((ViewGroup) v);

		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute(true);
		}
		
		// Adapter
		mAdapter = new WeiboAdapter(getActivity(), mList, mCache.mMessages,
				   mBindOrig, mShowCommentStatus);

		// Content Margin
		if (getActivity() instanceof MainActivity && mAllowHidingActionBar) {
			View header = new View(getActivity());
			RecyclerView.LayoutParams p = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
					Utility.getDecorPaddingTop(getActivity()));
			header.setLayoutParams(p);
			mAdapter.setHeaderView(header);
			mSwipeRefresh.setProgressViewOffset(false, 0, (int) (p.height * 1.2));
		}

		mList.setAdapter(mAdapter);

		// Listener
		if (getActivity() instanceof MainActivity) {
			mAdapter.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView view, int dx, int dy) {
					int deltaY = -dy;
					boolean shouldShow = deltaY > 0;
					if (shouldShow != mFABShowing) {
						if (shouldShow) {
							showFAB();
						} else {
							hideFAB();
						}
					}

					if (mAllowHidingActionBar) {
						if ((mTranslationY > -mActionBarHeight && deltaY < 0)
							|| (mTranslationY < 0 && deltaY > 0)) {
								
							mTranslationY += deltaY;
						}
						
						if (mTranslationY < -mActionBarHeight) {
							mTranslationY = -mActionBarHeight;
						} else if (mTranslationY > 0) {
							mTranslationY = 0;
						}
						
						updateTranslation();
					}
					
					if (!mRefreshing && mManager.findLastVisibleItemPosition() >= mAdapter.getItemCount() - 5) {
						new Refresher().execute(false);
					}

					mFABShowing = shouldShow;
					mLastY = dy;
				}
			});
		}

		mShadow.bringToFront();

		if (getActivity() instanceof MainActivity && mAllowHidingActionBar) {
			mActionBarHeight = Utility.getActionBarHeight(getActivity());
			mShadow.setTranslationY(mActionBarHeight);
		}

		return v;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (!hidden) {
			initTitle();
			showFAB();
			if (this instanceof HomeTimeLineFragment) {
				((MainActivity) getActivity()).setShowSpinner(true);
			} else {
				((MainActivity) getActivity()).setShowSpinner(false);
			}
			updateTranslation();
		} else {
			hideFAB();
		}
	}

	@Override
	public void doRefresh() {
		mList.smoothScrollToPosition(0);
		mList.post(new Runnable() {
			@Override
			public void run() {
				onRefresh();
			}
		});
	}

	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(true);
		}
	}
	
	protected void updateTranslation() {
		mToolbar.setTranslationY(mTranslationY);
		mShadow.setTranslationY(mActionBarHeight + mTranslationY);
		/*mSwipeRefresh.setProgressViewOffset(false, 0, (int) ((mActionBarHeight + mTranslationY) * 1.2));
		mSwipeRefresh.invalidate();*/
	}

	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}

	protected void initTitle() {
		mActionBar.setTitle(R.string.timeline);
	}

	protected void bindSwipeToRefresh(ViewGroup v) {
		mSwipeRefresh = new SwipeRefreshLayout(getActivity());

		// Move child to SwipeRefreshLayout, and add SwipeRefreshLayout to root
		// view
		v.removeViewInLayout(mList);
		v.addView(mSwipeRefresh, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mSwipeRefresh.addView(mList, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(R.color.ptr_green, R.color.ptr_orange,
				R.color.ptr_red, R.color.ptr_blue);
	}

	public void hideFAB() {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).hideFAB();
		}
	}

	public void showFAB() {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).showFAB();
		}
	}

	protected void newPost() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(getActivity(), NewPostActivity.class);
		startActivity(i);
	}

	protected void load(boolean param) {
		mCache.load(param);
		mCache.cache();
	}

	private class Refresher extends AsyncTask<Boolean, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Utility.clearOngoingUnreadCount(getActivity());
			mLastCount = mCache.mMessages.getSize();
			mRefreshing = true;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(true);
				mSwipeRefresh.invalidate();
			}
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			load(params[0]);
			return params[0];
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (result) {
				mList.smoothScrollToPosition(0);
			}
			
			mAdapter.notifyDataSetChangedAndClone();
			mRefreshing = false;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(false);
			}
		}

	}
}
