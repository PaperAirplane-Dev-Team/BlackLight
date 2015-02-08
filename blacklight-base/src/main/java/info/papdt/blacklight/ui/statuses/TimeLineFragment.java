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

package info.papdt.blacklight.ui.statuses;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.WeiboAdapter;
import info.papdt.blacklight.ui.common.DragRelativeLayout;
import info.papdt.blacklight.ui.common.ToolbarActivity;
import info.papdt.blacklight.ui.main.MainActivity;

public abstract class TimeLineFragment extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, MainActivity.Refresher {
	
	private static final String TAG = TimeLineFragment.class.getSimpleName();

	protected RecyclerView mList;
	protected View mShadow, mScroller, mOrbit;
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
	protected boolean mFastScrollEnabled = false;
	private boolean mFABShowing = true;

	private int mLastCount = 0;
	private int mLastPosition = -1;
	private int mNewPosition = -1;
	
	private Runnable mScrollToRunnable = new Runnable() {
		@Override
		public void run() {
			if (mNewPosition != -1) {
				mList.scrollToPosition(mNewPosition);
				mNewPosition = -1;
			}
		}
	};
	
	private Runnable mHideScrollerRunnable = new Runnable() {
		@Override
		public void run() {
			AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
			alpha.setDuration(600);
			alpha.setFillAfter(true);
			mScroller.clearAnimation();
			mOrbit.clearAnimation();
			mScroller.setAnimation(alpha);
			mOrbit.setAnimation(alpha);
			alpha.startNow();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				
		mActionBar = ((ToolbarActivity) getActivity()).getSupportActionBar();
		mToolbar = ((ToolbarActivity) getActivity()).getToolbar();
		initTitle();
		mSettings = Settings.getInstance(getActivity().getApplicationContext());
		
		mFastScrollEnabled = mSettings.getBoolean(Settings.FAST_SCROLL, false);

		final DragRelativeLayout v = (DragRelativeLayout) inflater.inflate(R.layout.home_timeline, null);
		
		// Initialize views
		mList = Utility.findViewById(v, R.id.home_timeline);
		mShadow = Utility.findViewById(v, R.id.action_shadow);
		mScroller = Utility.findViewById(v, R.id.scroller);
		mOrbit = Utility.findViewById(v, R.id.scroller_orbit);

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
			mSwipeRefresh.setProgressViewOffset(false, 0, (int) ((p.height + Utility.dp2px(getActivity(), 20)) * 1.2));
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

					mFABShowing = shouldShow;
					mLastY = dy;
				}
			});
		}
		
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
		
		mAdapter.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView rv, int dx, int dy) {
				if (!mRefreshing && mManager.findLastVisibleItemPosition() >= mAdapter.getItemCount() - 5) {
					new Refresher().execute(false);
				}
				
				if (mFastScrollEnabled) {
					mScroller.removeCallbacks(mHideScrollerRunnable);
					mScroller.clearAnimation();
					mOrbit.clearAnimation();
					
					mScroller.setAlpha(1.0f);
					mOrbit.setAlpha(1.0f);
					
					int first = mManager.findFirstVisibleItemPosition();
					int visible = mManager.findLastVisibleItemPosition() - first;
					int total = mAdapter.getCount();
					
					mScroller.setTranslationY(((mList.getHeight() - mScroller.getHeight() - 2 * params.topMargin) * ((float) first / (total - visible))));
					
					postHideScroller();
				}
			}
		});

		mShadow.bringToFront();
		
		if (mFastScrollEnabled) {
			mOrbit.setVisibility(View.VISIBLE);
			mScroller.setVisibility(View.VISIBLE);
			
			mOrbit.bringToFront();
			mScroller.bringToFront();
			ViewCompat.setElevation(mScroller, 5.0f);
			
			// Drag
			v.setDraggableChild(mScroller);
			v.setCallback(new DragRelativeLayout.Callback() {
				@Override
				public int onDraggedVertically(int top, int dy) {
					mScroller.removeCallbacks(mHideScrollerRunnable);
					mScroller.removeCallbacks(mScrollToRunnable);
					mScroller.clearAnimation();
					mOrbit.clearAnimation();
					postHideScroller();
					int newTop = mScroller.getTop() + (int) mScroller.getTranslationY() + dy;
					
					if (newTop < mOrbit.getTop()) {
						newTop = mOrbit.getTop();
					} else if (newTop > mOrbit.getBottom()) {
						newTop = mOrbit.getBottom() - mScroller.getHeight();
					}
					
					int first = mManager.findFirstVisibleItemPosition();
					int visible = mManager.findLastVisibleItemPosition() - first;
					int total = mAdapter.getCount();
					
					mScroller.setTranslationY(newTop);
					postScrollTo((int) ((float) newTop / (mList.getHeight() - mScroller.getHeight() - 2 * params.topMargin) * (total - visible)));
					
					return 0;
				}
			
				@Override
				public int onDraggedHorizontally(int left, int dx) {
					return mScroller.getLeft();
				}
			});
			
			postHideScroller();
		}
		
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (getActivity() instanceof MainActivity && mAllowHidingActionBar) {
					mActionBarHeight = mToolbar.getHeight();
					mShadow.setTranslationY(mActionBarHeight);
					RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) mAdapter.getHeaderView().getLayoutParams();
					lp.height = mActionBarHeight;
					mAdapter.getHeaderView().setLayoutParams(lp);
					mSwipeRefresh.setProgressViewOffset(false, 0, (int) (mActionBarHeight * 1.2));
					mSwipeRefresh.invalidate();
					v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});

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
	public void goToTop() {
		
		int pos = mManager.findFirstVisibleItemPosition();
		
		if (pos != 0 || mLastPosition != -1) {
			
			if (mLastPosition != -1) {
				pos = mLastPosition;
				mLastPosition = -1;
			} else {
				mLastPosition = pos;
				pos = 0;
			}
			
			mList.smoothScrollToPosition(pos);
		}
	}

	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(true);
		}
	}
	
	private void postHideScroller() {
		mScroller.postDelayed(mHideScrollerRunnable, 1000);
	}
	
	private void postScrollTo(int pos) {
		mNewPosition = pos;
		mScroller.postDelayed(mScrollToRunnable, 100);
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
