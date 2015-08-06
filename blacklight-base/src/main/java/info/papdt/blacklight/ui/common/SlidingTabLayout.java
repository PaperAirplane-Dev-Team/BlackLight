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

package info.papdt.blacklight.ui.common;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingTabLayout extends HorizontalScrollView {

	public interface TabColorizer {
		/**
		 * @return return the color of the indicator used when {@code position} is selected.
		 */
		int getIndicatorColor(int position);

		int getSelectedTitleColor(int position);
		int getNormalTitleColor(int position);
	}

	public interface TabIconAdapter {
		Drawable getIcon(int position);
	}

	private static final int TITLE_OFFSET_DIPS = 24;
	private static final int TAB_TEXT_VIEW_PADDING_DIPS = 16;
	private static final int TAB_ICON_VIEW_PADDING_DIPS = 16 + 2;
	private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

	private int mTitleOffset;

	private int mTabViewLayoutId;
	private int mTabViewTextViewId;
	private int mTabIconSize = 48;
	private boolean mDistributeEvenly;
	private boolean mTabStripPopulated = false;

	private ViewPager mViewPager;
	private SparseArray<String> mContentDescriptions = new SparseArray<String>();
	private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

	private final SlidingTabStrip mTabStrip;

	private TabIconAdapter mIconAdapter;

	public SlidingTabLayout(Context context) {
		this(context, null);
	}

	public SlidingTabLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Disable the Scroll Bar
		setHorizontalScrollBarEnabled(false);
		// Make sure that the Tab Strips fills this View
		setFillViewport(true);

		mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

		mTabStrip = new SlidingTabStrip(context);
		addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	public void setCustomTabColorizer(TabColorizer tabColorizer) {
		mTabStrip.setCustomTabColorizer(tabColorizer);
	}

	// Call this when contents in TabColorizer changed.
	public void notifyIndicatorColorChanged() {
		mTabStrip.updateTitleViews();
	}

	public void setDistributeEvenly(boolean distributeEvenly) {
		mDistributeEvenly = distributeEvenly;
	}

	public void setSelectedIndicatorColors(int ... colors) {
		mTabStrip.setSelectedIndicatorColors(colors);
	}

	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mViewPagerPageChangeListener = listener;
	}

	public void setCustomTabView(int layoutResId, int textViewId) {
		mTabViewLayoutId = layoutResId;
		mTabViewTextViewId = textViewId;
	}

	public void setIconAdapter(TabIconAdapter adapter) {
		mIconAdapter = adapter;
	}

	public void setViewPager(ViewPager viewPager) {
		mTabStrip.removeAllViews();

		mViewPager = viewPager;
		if (viewPager != null) {
			viewPager.setOnPageChangeListener(new InternalViewPagerListener());
			populateTabStrip();
		}
	}

	public void setViewPager(ViewPager viewPager, SlidingTabLayout layout) {
		mTabStrip.removeAllViews();

		mViewPager = viewPager;
		if (layout != null) {
			layout.setOnPageChangeListener(new InternalViewPagerListener());
			populateTabStrip();
		}
	}

	public void setTabIconSize(int size) {
		mTabIconSize = size;
		if (mViewPager != null && mIconAdapter != null) {
			for (int i = 0; i < mTabStrip.getChildCount(); i++) {
				View child = mTabStrip.getChildAt(i);

				if (child instanceof ImageView) {
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)child.getLayoutParams();
					lp.width = (int) (size  * 0.88f);
					lp.height = size;
				}
			}
		}
	}

	/**
	 * Create a default view to be used for tabs. This is called if a custom tab view is not set via
	 * {@link #setCustomTabView(int, int)}.
	 */
	protected View createDefaultTabView(Context context) {
		View v;
		if (mIconAdapter == null) {
			TextView textView = new TextView(context);
			textView.setGravity(Gravity.CENTER);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
			textView.setTypeface(Typeface.DEFAULT_BOLD);
			textView.setAllCaps(true);
			textView.setLayoutParams(new LinearLayout.LayoutParams(
							 ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
			int padding = (int) (TAB_TEXT_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
			textView.setPadding(padding, padding, padding, padding);

			v = textView;
		} else {
			ImageView imgView = new TintImageView(context);
			imgView.setScaleType(ImageView.ScaleType.FIT_XY);
			imgView.setLayoutParams(new LinearLayout.LayoutParams(mTabIconSize, mTabIconSize));
			int padding = (int) (TAB_ICON_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
			imgView.setPadding(padding, padding, padding, padding);

			v = imgView;
		}

		TypedValue outValue = new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
							 outValue, true);
		v.setBackgroundResource(outValue.resourceId);

		return v;
	}

	private void populateTabStrip() {
		final PagerAdapter adapter = mViewPager.getAdapter();
		final OnClickListener tabClickListener = new TabClickListener();

		for (int i = 0; i < adapter.getCount(); i++) {
			View tabView = null;
			TextView tabTitleView = null;
			ImageView tabIconView = null;

			if (mTabViewLayoutId != 0) {
				// If there is a custom tab view layout id set, try and inflate it
				tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
										    false);
				tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
			}

			if (tabView == null) {
				tabView = createDefaultTabView(getContext());
			}

			if (tabTitleView == null && TextView.class.isInstance(tabView)) {
				tabTitleView = (TextView) tabView;
			} else if (tabIconView == null && ImageView.class.isInstance(tabView)) {
				tabIconView = (ImageView) tabView;
			}

			if (mDistributeEvenly) {
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)tabView.getLayoutParams();
				lp.width = 0;
				lp.weight = 1;
			}

			if (tabTitleView != null)
				tabTitleView.setText(adapter.getPageTitle(i));
			else if (tabIconView != null)
				tabIconView.setImageDrawable(mIconAdapter.getIcon(i));

			tabView.setOnClickListener(tabClickListener);
			String desc = mContentDescriptions.get(i, null);
			if (desc != null) {
				tabView.setContentDescription(desc);
			}

			mTabStrip.addView(tabView);
			if (i == mViewPager.getCurrentItem()) {
				tabView.setSelected(true);
			}
		}

		mTabStripPopulated = true;
	}

	public void setContentDescription(int i, String desc) {
		mContentDescriptions.put(i, desc);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mViewPager != null) {
			scrollToTab(mViewPager.getCurrentItem(), 0);
		}
	}

	int getTextViewId() {
		return mTabViewTextViewId;
	}

	private void scrollToTab(int tabIndex, int positionOffset) {
		final int tabStripChildCount = mTabStrip.getChildCount();
		if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
			return;
		}

		View selectedChild = mTabStrip.getChildAt(tabIndex);
		if (selectedChild != null) {
			int targetScrollX = selectedChild.getLeft() + positionOffset;

			if (tabIndex > 0 || positionOffset > 0) {
				// If we're not at the first child and are mid-scroll, make sure we obey the offset
				targetScrollX -= mTitleOffset;
			}

			scrollTo(targetScrollX, 0);
		}
	}

	private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
		private int mScrollState;

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			int tabStripChildCount = mTabStrip.getChildCount();
			if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
				return;
			}

			mTabStrip.onViewPagerPageChanged(position, positionOffset);

			View selectedTitle = mTabStrip.getChildAt(position);
			int extraOffset = (selectedTitle != null)
					  ? (int) (positionOffset * selectedTitle.getWidth())
					  : 0;
			scrollToTab(position, extraOffset);

			if (mViewPagerPageChangeListener != null) {
				mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
									    positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			mScrollState = state;

			if (mViewPagerPageChangeListener != null) {
				mViewPagerPageChangeListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position) {
			if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
				mTabStrip.onViewPagerPageChanged(position, 0f);
				scrollToTab(position, 0);
			}
			for (int i = 0; i < mTabStrip.getChildCount(); i++) {
				mTabStrip.getChildAt(i).setSelected(position == i);
			}
			if (mViewPagerPageChangeListener != null) {
				mViewPagerPageChangeListener.onPageSelected(position);
			}
		}
	}

	private class TabClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			for (int i = 0; i < mTabStrip.getChildCount(); i++) {
				if (v == mTabStrip.getChildAt(i)) {
					mViewPager.setCurrentItem(i);
					return;
				}
			}
		}
	}

}
