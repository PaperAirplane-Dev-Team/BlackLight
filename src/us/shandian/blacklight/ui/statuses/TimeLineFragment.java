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
import android.app.Service;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTouch;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.WeiboAdapter;
import us.shandian.blacklight.ui.common.SwipeRefreshLayout;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;
import us.shandian.blacklight.ui.main.MainActivity;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class TimeLineFragment extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, GestureDetector.OnGestureListener,
		OnScrollListener {

	@InjectView(R.id.home_timeline) protected ListView mList;
	private WeiboAdapter mAdapter;
	protected HomeTimeLineApiCache mCache;

	private Settings mSettings;

	// Pull To Refresh
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;

	private boolean mRefreshing = false;

	protected boolean mBindOrig = true;
	protected boolean mShowCommentStatus = true;
	protected boolean mAllowHidingActionBar = true;

	private int mLastCount = 0;

	// Gesture
	private GestureDetector mDetector;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		initTitle();
		mSettings = Settings.getInstance(getActivity().getApplicationContext());

		View v = inflater.inflate(R.layout.home_timeline, null);
		
		// Inject
		ButterKnife.inject(this, v);

		mCache = bindApiCache();
		mCache.loadFromCache();

		mList.setDrawingCacheEnabled(true);
		mList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		mList.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE
				| ViewGroup.PERSISTENT_SCROLLING_CACHE);

		// Swipe To Refresh
		bindSwipeToRefresh((ViewGroup) v);

		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute(new Boolean[] { true });
		}

		// Content Margin
		if (getActivity() instanceof MainActivity && mAllowHidingActionBar) {
			View header = new View(getActivity());
			LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,
					Utility.getDecorPaddingTop(getActivity()));
			header.setLayoutParams(p);
			mList.addHeaderView(header);
			mSwipeRefresh.setTopMargin(p.height);
		}

		// Adapter
		mAdapter = new WeiboAdapter(getActivity(), mList, mCache.mMessages,
				mBindOrig, mShowCommentStatus);
		mList.setAdapter(mAdapter);

		// Gesture Detector
		mDetector = new GestureDetector(getActivity(), this);

		if (getActivity() instanceof MainActivity) {
			mAdapter.addOnScrollListener(this);
		}

		return v;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (!hidden) {
			initTitle();
			resume();
			showFAB();
			if (this instanceof HomeTimeLineFragment) {
				((MainActivity) getActivity()).setShowSpinner(true);
			} else {
				((MainActivity) getActivity()).setShowSpinner(false);
			}
		} else {
			hideFAB();
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
	}

	public void doRefresh() {
		mSwipeRefresh.setIsDown(false);

		if (mList.getFirstVisiblePosition() <= 30) {
			mList.smoothScrollToPosition(0);
		} else {
			mList.setSelection(0);
		}
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

			if (getActivity() instanceof MainActivity) {
				showFAB();
				getActivity().getActionBar().show();
			}

			new Refresher().execute(new Boolean[] { !mSwipeRefresh.isDown() });
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem < 1) {
			showFAB();
			getActivity().getActionBar().show();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@OnTouch(R.id.home_timeline)
	public boolean handleTouch(View v, MotionEvent ev) {
		if (getActivity() instanceof MainActivity) {
			mDetector.onTouchEvent(ev);
		}
		return false;
	}

	// Start Gesture Events

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (Math.abs(distanceY) < 20 || mRefreshing)
			return false;

		if (mList.getTop() != 0) {
			mList.setTop(0);
		}

		if (e1 == null || e2 == null || e1.getY() < e2.getY()
				|| mList.getFirstVisiblePosition() < 1) {
			showFAB();

			if (mAllowHidingActionBar) {
				getActivity().getActionBar().show();
			}
		} else {
			hideFAB();

			if (mAllowHidingActionBar) {
				getActivity().getActionBar().hide();
			}
		}

		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	// End Gesture Events

	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}

	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.timeline);
	}

	protected void bindSwipeToRefresh(ViewGroup v) {
		mSwipeRefresh = new SwipeUpAndDownRefreshLayout(getActivity());

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
			mList.setSelection(0);
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
