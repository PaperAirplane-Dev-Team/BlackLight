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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.	 If not, see <http://www.gnu.org/licenses/>.
 */

package info.papdt.blacklight.ui.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/*
 * Hello, I am GenerousSlidngUpPanelLayout
 * Of course, I'm not greedy.
 * I'll pass touch events to child ListView if I do not need.
 * Wow, such view! So generous!
 */
public class GenerousSlidingUpPanelLayout extends SlidingUpPanelLayout {
	private RecyclerView mChild; // I love my child
	private float mStartX, mStartY;

	public GenerousSlidingUpPanelLayout(Context context) {
		this(context, null);
	}

	public GenerousSlidingUpPanelLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GenerousSlidingUpPanelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setChildListView(RecyclerView list) {
		mChild = list;
		setDragView(list);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mStartX = x;
				mStartY = y;
				super.onInterceptTouchEvent(ev);
				return false;
			case MotionEvent.ACTION_MOVE:
				if (mSlideOffset == 0 && !(y > mStartY && !mChild.canScrollVertically(-1)) || (y > mStartY && mSlideOffset == 1)) {
					return false;
				} else {
					return super.onInterceptTouchEvent(ev);
				}
			case MotionEvent.ACTION_UP:
				mStartX = 0;
				mStartY = 0;
				super.onInterceptTouchEvent(ev);
				return false;
			default:
				return super.onInterceptTouchEvent(ev);
		}
	}
}
