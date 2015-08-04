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

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.papdt.blacklight.support.Utility;

public class SlidingTabStrip extends LinearLayout {

	private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 0;
	private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
	private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 2;
	private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

	private static int sColorPrimary = 0;

	private final int mBottomBorderThickness;
	private final Paint mBottomBorderPaint;

	private final int mSelectedIndicatorThickness;
	private final Paint mSelectedIndicatorPaint;

	private final int mDefaultBottomBorderColor;

	private int mSelectedPosition;
	private float mSelectionOffset;

	private SlidingTabLayout.TabColorizer mCustomTabColorizer;
	private final SimpleTabColorizer mDefaultTabColorizer;

	SlidingTabStrip(Context context) {
		this(context, null);
	}

	SlidingTabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);

		final float density = getResources().getDisplayMetrics().density;

		TypedValue outValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorForeground, outValue, true);
		final int themeForegroundColor =  outValue.data;

		mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor,
												  DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

		mDefaultTabColorizer = new SimpleTabColorizer();
		mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);

		mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
		mBottomBorderPaint = new Paint();
		mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

		mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
		mSelectedIndicatorPaint = new Paint();

		// Obtain primary color
		sColorPrimary = Utility.getColorPrimary(getContext());
	}

	void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
		mCustomTabColorizer = customTabColorizer;
		invalidate();
	}

	void setSelectedIndicatorColors(int... colors) {
		// Make sure that the custom colorizer is removed
		mCustomTabColorizer = null;
		mDefaultTabColorizer.setIndicatorColors(colors);
		invalidate();
	}

	void onViewPagerPageChanged(int position, float positionOffset) {
		mSelectedPosition = position;
		mSelectionOffset = positionOffset;

		// Title colors changes when page scrolled
		final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
			? mCustomTabColorizer
			: mDefaultTabColorizer;

		int id = getSlidingTabLayout().getTextViewId();

		View selected = getChildAt(mSelectedPosition);
		View selectedTitle = id == 0 ? selected : selected.findViewById(id);

		int selectedColor = tabColorizer.getSelectedTitleColor(mSelectedPosition);
		int normalColor = tabColorizer.getNormalTitleColor(mSelectedPosition);

		if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
			View next = getChildAt(mSelectedPosition + 1);
			View nextTitle = id == 0 ? next : next.findViewById(id);

			// Set the gradient title colors
			int nextSelectedColor = tabColorizer.getSelectedTitleColor(mSelectedPosition + 1);
			int nextNormalColor = tabColorizer.getNormalTitleColor(mSelectedPosition + 1);

			int selectedBlend = blendColors(selectedColor, normalColor, 1.0f - mSelectionOffset);
			int nextBlend = blendColors(nextSelectedColor, nextNormalColor, mSelectionOffset);

			if (selectedTitle instanceof TextView)
				((TextView) selectedTitle).setTextColor(selectedBlend);
			else if (selectedTitle instanceof TintImageView)
				((TintImageView) selectedTitle).setColor(selectedBlend);

			if (nextTitle instanceof TextView)
				((TextView) nextTitle).setTextColor(nextBlend);
			else if (nextTitle instanceof TintImageView)
				((TintImageView) nextTitle).setColor(nextBlend);

		} else if (mSelectionOffset == 0f) {
			if (selectedTitle instanceof TextView)
				((TextView) selectedTitle).setTextColor(selectedColor);
			else if (selectedTitle instanceof TintImageView)
				((TintImageView) selectedTitle).setColor(selectedColor);
		}

		invalidate();
	}

	void updateTitleViews() {
		final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
			? mCustomTabColorizer
			: mDefaultTabColorizer;

		int id = getSlidingTabLayout().getTextViewId();

		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			View child =  id == 0 ? v : v.findViewById(id);

			int color;
			if (mSelectedPosition != i)
				color = tabColorizer.getNormalTitleColor(i);
			else
				color = tabColorizer.getSelectedTitleColor(i);

			if (child instanceof TextView) {
				((TextView) v).setTextColor(color);
			} else if (child instanceof TintImageView) {
				((TintImageView) v).setColor(color);
			}
		}

		invalidate();
	}

	SlidingTabLayout getSlidingTabLayout() {
		ViewParent parent = getParent();

		if (parent instanceof SlidingTabLayout) {
			return (SlidingTabLayout) parent;
		} else if (parent == null) {
			return null;
		} else {
			throw new RuntimeException("The parent of a SlidingTabStrip must be a SlidingTabLayout");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int height = getHeight();
		final int childCount = getChildCount();
		final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
			? mCustomTabColorizer
			: mDefaultTabColorizer;

		// Thick colored underline below the current selection
		if (childCount > 0) {
			View selectedTitle = getChildAt(mSelectedPosition);
			int left = selectedTitle.getLeft();
			int right = selectedTitle.getRight();
			int color = tabColorizer.getIndicatorColor(mSelectedPosition);

			if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
				int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
				if (color != nextColor) {
					color = blendColors(nextColor, color, mSelectionOffset);
				}

				// Draw the selection partway between the tabs
				View nextTitle = getChildAt(mSelectedPosition + 1);
				left = (int) (mSelectionOffset * nextTitle.getLeft() +
					(1.0f - mSelectionOffset) * left);
				right = (int) (mSelectionOffset * nextTitle.getRight() +
					(1.0f - mSelectionOffset) * right);
			}

			mSelectedIndicatorPaint.setColor(color);

			canvas.drawRect(left, height - mSelectedIndicatorThickness, right,
							height, mSelectedIndicatorPaint);
		}

		// Thin underline along the entire bottom edge
		canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
	}

	/**
	 * Set the alpha value of the {@code color} to be the given {@code alpha} value.
	 */
	private static int setColorAlpha(int color, byte alpha) {
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}

	/**
	 * Blend {@code color1} and {@code color2} using the given ratio.
	 *
	 * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
	 *              0.0 will return {@code color2}.
	 */
	private static int blendColors(int color1, int color2, float ratio) {
		final float inverseRation = 1f - ratio;
		float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
		float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
		float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
		return Color.rgb((int) r, (int) g, (int) b);
	}

	public static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
		private int[] mIndicatorColors;
		private int mBlendColor = sColorPrimary;

		@Override
		public int getIndicatorColor(int position) {
			return mIndicatorColors[position % mIndicatorColors.length];
		}


		@Override
		public int getSelectedTitleColor(int position) {
			return 0;
		}

		@Override
		public int getNormalTitleColor(int position) {
			return blendColors(getSelectedTitleColor(position), mBlendColor, 0.6f);
		}

		public void setBlendColor(int color) {
			mBlendColor = color;
		}

		void setIndicatorColors(int... colors) {
			mIndicatorColors = colors;
		}
	}
}
