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

package info.papdt.blacklight.ui.common;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.papdt.blacklight.R;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
 * A pager indicator implemented by extending LinearLayout
 * Creates TextViews to display pager titles
 * Draws a line to indicate the current tab
 */
public class LinearViewPagerIndicator extends LinearLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
	private static final String TAG = LinearViewPagerIndicator.class.getSimpleName();

	private int mForeground = Color.WHITE;
	private boolean mMeasured = false;
	private Context mContext;
	private int mWidth, mHeight, mTabWidth;
	private ViewPager mPager;
	private float mOffset = 0.0f;
	private int mCurrentPage = 0;
	private ViewPager.OnPageChangeListener mListener;

	public LinearViewPagerIndicator(Context context) {
		this(context, null);
	}

	public LinearViewPagerIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);	
	}

	public LinearViewPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;

		try {
			TypedArray array = context.obtainStyledAttributes(R.styleable.BlackLight);
			mForeground = array.getColor(R.styleable.BlackLight_IndicatorForeground, Color.WHITE);
			array.recycle();
		} catch (NotFoundException e) {
			mForeground = Color.WHITE;
		}

		// Make it draw
		setWillNotDraw(false);
	}

	/*
	 * Adds a tab to the indicator
	 * You can only do this before the view is measured
	 */
	public void addTab(String title) {
		if (mMeasured) {
			throw new IllegalStateException("Cannot add tabs when measured");
		}
		
		TextView tv = new TextView(mContext);
		tv.setText(title);
		tv.setTextColor(mForeground);
		tv.setGravity(Gravity.CENTER);
		tv.setTag(getChildCount());
		tv.setOnClickListener(this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.weight = 1.0f;
		addView(tv, params);
	}

	/*
	 * Bind this view with a ViewPager
	 */
	public void setViewPager(ViewPager pager) {
		mPager = pager;
		pager.setOnPageChangeListener(this);
	}

	/*
	 * Set color
	 */
	public void setForeground(int color) {
		mForeground = color;
		for (int i = 0; i < getChildCount(); i++) {
			TextView tv = (TextView) getChildAt(i);
			tv.setTextColor(mForeground);
		}
		invalidate();
	}
	
	public String getTextOfTab(int id) {
		return ((TextView) getChildAt(id)).getText().toString();
	}
	
	public void setTextOfTab(int id, String text) {
		((TextView) getChildAt(id)).setText(text);
	}

	/*
	 * Set listener
	 */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		mTabWidth = getChildAt(0).getMeasuredWidth();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the indicator line
		int position = mCurrentPage * mTabWidth + (int) (mOffset * mTabWidth);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(mForeground);
		
		canvas.drawRect(position, (mHeight - mHeight / 18), position + mTabWidth, mHeight, paint);

		if (DEBUG) {
			Log.d(TAG, "mWidth = " + mWidth + "; mHeight = " + mHeight + "; mTabWidth = " + mTabWidth);
			Log.d(TAG, "mCurrentPage = " + mCurrentPage);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mCurrentPage = position;
		mOffset = positionOffset;
		invalidate();

		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onClick(View v) {
		TextView tv = (TextView) v;
		mPager.setCurrentItem(Integer.valueOf(tv.getTag().toString()));
	}
}
