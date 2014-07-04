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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import android.support.v4.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
  Adds Swipe Down To Refresh
*/
public class SwipeUpAndDownRefreshLayout extends SwipeRefreshLayout
{
	private Canvas mCanvas;
	private Bitmap mBitmap;
	private int mWidth, mHeight, mProgressBarHeight;
	
	private boolean mIsDown = false;
	private boolean mDownPriority = false;
	
	private Field mTarget, mProgressBar, mReturningToStart, mDownEvent;
	private Method mSetBounds, mDraw, mUpdateContentOffsetTop;
	
	public SwipeUpAndDownRefreshLayout(Context context) {
		this(context, null);
	}
	
	public SwipeUpAndDownRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		try {
			initFields();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initFields() throws NoSuchFieldException, NoSuchMethodException {
		mTarget = SwipeRefreshLayout.class.getDeclaredField("mTarget");
		mTarget.setAccessible(true);
		
		mProgressBar = SwipeRefreshLayout.class.getDeclaredField("mProgressBar");
		mProgressBar.setAccessible(true);
		
		mReturningToStart = SwipeRefreshLayout.class.getDeclaredField("mReturningToStart");
		mReturningToStart.setAccessible(true);
		
		mDownEvent = SwipeRefreshLayout.class.getDeclaredField("mDownEvent");
		mDownEvent.setAccessible(true);
		
		mUpdateContentOffsetTop = SwipeRefreshLayout.class.getDeclaredMethod("updateContentOffsetTop", int.class);
		mUpdateContentOffsetTop.setAccessible(true);
	}
	
	public boolean isDown() {
		return mIsDown;
	}
	
	public void setIsDown(boolean isDown) {
		mIsDown = isDown;
	}
	
	public void setDownHasPriority() {
		mDownPriority = true;
	}
	
	public boolean canChildScrollDown() {
		try {
			View v = (View) mTarget.get(this);
			return v.canScrollVertically(1);
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		
		try {
			Field f = SwipeRefreshLayout.class.getDeclaredField("mProgressBarHeight");
			f.setAccessible(true);
			mProgressBarHeight = f.get(this);
		} catch (Exception e) {
			mProgressBarHeight = 0;
		}
		
		mBitmap = Bitmap.createBitmap(mWidth, mProgressBarHeight, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
	}

	@Override
	public void draw(Canvas canvas) {
		Object progressBar = null;
		
		try {
			progressBar = mProgressBar.get(this);
		} catch (Exception e) {
			
		}
		
		Method m = mSetBounds;
		
		if (m == null && progressBar != null) {
			try {
				m = progressBar.getClass().getDeclaredMethod("setBounds", int.class, int.class, int.class, int.class);
				m.setAccessible(true);
			} catch (Exception e) {
				
			}
		}
		
		if (m != null) {
			mSetBounds = m;
			
			try {
				m.invoke(progressBar, 0, 0, 0, 0);
			} catch (Exception e) {
				
			}
		}
		
		super.draw(canvas);
		
		if (m != null) {
			Paint p = new Paint();
			p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			mCanvas.drawPaint(p);
			try {
				m.invoke(progressBar, 0, 0, mWidth, mProgressBarHeight);
				
				Method method = mDraw;
				
				if (method == null) {
					method = progressBar.getClass().getDeclaredMethod("draw", Canvas.class);
					method.setAccessible(true);
				}
				
				mDraw = method;
				method.invoke(progressBar, mCanvas);
			} catch (Exception e) {
				
			}
			
			canvas.drawBitmap(mBitmap, 0, isDown() ? mHeight - mProgressBarHeight : 0, null);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean handled = super.onInterceptTouchEvent(ev);
		boolean returningToStart;
		try {
			returningToStart = mReturningToStart.get(this);
		} catch (Exception e) {
			return handled;
		}
		
		if (!handled && !returningToStart && !canChildScrollDown()) {
			handled = onTouchEvent(ev);
		}
		
		return handled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean returningToStart = false;
		MotionEvent downEvent = null;
		try {
			returningToStart = mReturningToStart.get(this);
			downEvent = (MotionEvent) mDownEvent.get(this);
		} catch (Exception e) {
			
		}
		
		boolean ret;
		
		if (event.getAction() == MotionEvent.ACTION_MOVE
			&& downEvent != null && !returningToStart && !canChildScrollDown() && (mDownPriority || canChildScrollUp())) {
				downEvent.setLocation(downEvent.getX(), -downEvent.getY());
				event.setLocation(event.getX(), -event.getY());
				
				ret = super.onTouchEvent(event);
				
				downEvent.setLocation(downEvent.getX(), -downEvent.getY());
				event.setLocation(event.getX(), -event.getY());
		} else {
			ret = super.onTouchEvent(event);
		}
		
		try {
			mUpdateContentOffsetTop.invoke(this, 0);
		} catch (Exception e) {

		}
		
		if (!canChildScrollDown()) {
			mIsDown = true;
		}
		
		if ((!mDownPriority || canChildScrollDown()) && !canChildScrollUp()) {
			mIsDown = false;
		}
		
		return ret;
	}
}
