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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.daimajia.swipe.SwipeLayout;

public class HackyHorizontalScrollView extends HorizontalScrollView implements SwipeLayout.SwipeDenier {

	public HackyHorizontalScrollView(Context context) {
		super(context);
	}

	public HackyHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean shouldDenySwipe(MotionEvent ev) {
		return inMyRange(ev);
	}

	// Oops, you are in my range
	// Sorry
	private boolean inMyRange(MotionEvent ev) {
		int[] location = new int[2];
		getLocationOnScreen(location);
		int x = location[0], y = location[1];
		int width = getWidth(), height = getHeight();
		float evX = ev.getRawX(), evY = ev.getRawY();

		return evX > x && evX < x + width && evY > y && evY < y + height;
	}
}
