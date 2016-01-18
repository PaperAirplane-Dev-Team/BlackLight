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
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class DragRelativeLayout extends RelativeLayout
{
	private static final String TAG = DragRelativeLayout.class.getSimpleName();
	
	public interface Callback {
		int onDraggedVertically(int top, int dy);
		int onDraggedHorizontally(int left, int dx);
	}
	
	private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			
			if (DEBUG) {
				Log.d(TAG, "tryCaptureView: " + (child == mDraggable));
			}
			
			return child == mDraggable;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			if (mMyCallback != null) {
				return mMyCallback.onDraggedVertically(top, dy);
			} else {
				return super.clampViewPositionVertical(child, top, dy);
			}
		}

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (mMyCallback != null) {
				return mMyCallback.onDraggedHorizontally(left, dx);
			} else {
				return super.clampViewPositionHorizontal(child, left, dx);
			}
		}
	};
	private Callback mMyCallback;
	private ViewDragHelper mHelper;
	
	private View mDraggable;
	
	public DragRelativeLayout(Context context) {
		this(context, null);
	}
	
	public DragRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DragRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		mHelper = ViewDragHelper.create(this, 1.0f, mCallback);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mDraggable == null) return super.onInterceptHoverEvent(ev);
		
		boolean ret = mHelper.shouldInterceptTouchEvent(ev);
		
		if (!ret) {
			float x = ev.getX();
			float y = ev.getY();
			
			ret = ev.getActionMasked() == MotionEvent.ACTION_DOWN && insideDraggable(x, y);
		}
		
		if (DEBUG) {
			Log.d(TAG, "onInterceptTouchEvent: " + ret);
		}
		
		return ret;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mDraggable == null) return super.onTouchEvent(event);
		
		float x = event.getX();
		float y = event.getY();
		
		if (insideDraggable(x, y)) {
			mHelper.captureChildView(mDraggable, event.getPointerId(0));
		}
		
		try {
			mHelper.processTouchEvent(event);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	private boolean insideDraggable(float x, float y) {
		int slop = mDraggable.getWidth();

		int left = mDraggable.getLeft() - slop + (int) mDraggable.getTranslationX();
		int top = mDraggable.getTop() - slop + (int) mDraggable.getTranslationY();
		int right = mDraggable.getRight() + slop + (int) mDraggable.getTranslationX();
		int bottom = mDraggable.getBottom() + slop + (int) mDraggable.getTranslationY();

		return x > left && x < right && y > top && y < bottom;
	}
	
	public void setDraggableChild(View child) {
		mDraggable = child;
	}
	
	public void setCallback(Callback callback) {
		mMyCallback = callback;
	}
}
