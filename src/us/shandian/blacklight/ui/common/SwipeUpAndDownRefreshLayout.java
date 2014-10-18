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

/*
  Adds Swipe Down To Refresh
*/
public class SwipeUpAndDownRefreshLayout extends SwipeRefreshLayout {
	private Canvas mCanvas;
	private Bitmap mBitmap;
	private int mTopMargin;
	private int mWidth, mHeight;

	private boolean mIsDown = false;
	private boolean mDownPriority = false;

	public SwipeUpAndDownRefreshLayout(Context context) {
		this(context, null);
	}

	public SwipeUpAndDownRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/*private void initFields() throws NoSuchFieldException, NoSuchMethodException {
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
	}*/

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
		return mTarget.canScrollVertically(1);
	}

	public void setTopMargin(int margin) {
		mTopMargin = margin;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();

		mBitmap = Bitmap.createBitmap(mWidth, mProgressBarHeight, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
	}

	@Override
	public void draw(Canvas canvas) {
		// A little hack
		mProgressBar.setBounds(0, 0, 0, 0);

		super.draw(canvas);

		Paint p = new Paint();
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mCanvas.drawPaint(p);
		mProgressBar.setBounds(0, 0, mWidth, mProgressBarHeight);
		mProgressBar.draw(mCanvas);

		canvas.drawBitmap(mBitmap, 0, isDown() ? mHeight - mProgressBarHeight : mTopMargin, null);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean handled = super.onInterceptTouchEvent(ev);

		if (!handled && !mReturningToStart && !canChildScrollDown()) {
			handled = onTouchEvent(ev);
		}

		return handled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		boolean ret;

		if (event.getAction() == MotionEvent.ACTION_MOVE
				&& mDownEvent != null && !mReturningToStart && !canChildScrollDown() && (mDownPriority || canChildScrollUp())) {
			mDownEvent.setLocation(mDownEvent.getX(), -mDownEvent.getY());
			event.setLocation(event.getX(), -event.getY());

			ret = super.onTouchEvent(event);

			mDownEvent.setLocation(mDownEvent.getX(), -mDownEvent.getY());
			event.setLocation(event.getX(), -event.getY());

			float yDiff = event.getY() - mDownEvent.getY() - mTouchSlop;

			updateContentOffsetTop((int) yDiff, -1);

			// Abandon the offset animation when swiping up
				/*try {
					mUpdateContentOffsetTop.invoke(this, -100);
				} catch (Exception e) {
					
				}*/

		} else {
			ret = super.onTouchEvent(event);
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
