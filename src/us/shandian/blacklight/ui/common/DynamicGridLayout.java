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

package us.shandian.blacklight.ui.common;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class DynamicGridLayout extends GridLayout {
	private static final String TAG = DynamicGridLayout.class.getSimpleName();

	private static int sHeight = -1;

	private ArrayList<View> mViews = new ArrayList<View>();

	public DynamicGridLayout(Context context) {
		this(context, null);
	}

	public DynamicGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		if (mViews.size() == 0) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				
				if (!mViews.contains(child)) {
					mViews.add(child);
				}
			}
		}

		for (View child : mViews) {
			if (child.getVisibility() == View.GONE && child.getParent() != null) {
				removeView(child);
			} else {
				GridLayout.LayoutParams params = (GridLayout.LayoutParams) child.getLayoutParams();
				
				if (params == null) {
					params = new GridLayout.LayoutParams(spec(GridLayout.UNDEFINED), spec(GridLayout.UNDEFINED));
				} else {
					params.rowSpec = spec(GridLayout.UNDEFINED);
					params.columnSpec = spec(GridLayout.UNDEFINED);
				}

				if (child.getParent() == null) {
					addView(child, params);
				} else {
					child.setLayoutParams(params);
				}
			}
		}

		setRowCount(1);

		super.onMeasure(widthSpec, heightSpec);

		int height = getMeasuredHeight();

		if (sHeight == -1) {
			sHeight = mViews.get(0).getMeasuredHeight();
		}

		int count = height / sHeight;

		if (DEBUG) {
			Log.d(TAG, "height = " + height + "; count = " + count);
		}

		if (sHeight * count > height && count > 1) {
			count = count - 1;
		}

		setRowCount(count);

		super.onMeasure(widthSpec, heightSpec);
	}
	
	public View dynamicFindViewById(int id) {
		for (View child : mViews) {
			if (child.getId() == id) {
				return child;
			} else {
				View v = child.findViewById(id);
				if (v != null)
					return v;
			}
		}

		return super.findViewById(id);
	}
}
