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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.	 If not, see <http://www.gnu.org/licenses/>.
 */

package info.papdt.blacklight.ui.common;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/*
 * From GitHub Gist: https://gist.github.com/Jogan/9def6110edf3247825c9
 */
public class FloatingActionButton extends View implements Animator.AnimatorListener {

	final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
	final static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

	Context context;
	Paint mButtonPaint;
	Paint mDrawablePaint;
	Bitmap mBitmap;
	Drawable mRipple;
	boolean mHidden = false;

	public FloatingActionButton(Context context) {
		super(context);
		this.context = context;
		init(Color.WHITE);
	}

	public void setFloatingActionButtonColor(int FloatingActionButtonColor) {
		init(FloatingActionButtonColor);
	}

	public void setFloatingActionButtonDrawable(Drawable FloatingActionButtonDrawable) {
		mBitmap = ((BitmapDrawable) FloatingActionButtonDrawable).getBitmap();
		invalidate();
	}

	public void init(int FloatingActionButtonColor) {
		setWillNotDraw(false);
		setClickable(true);

		mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if (Build.VERSION.SDK_INT >= 21) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);

			mRipple = new RippleDrawable(new ColorStateList(new int[][]{
				{}
			}, new int[]{
				Color.WHITE
			}), new ColorDrawable(FloatingActionButtonColor), null);
			setBackgroundDrawable(mRipple);
			setOutlineProvider(new ViewOutlineProvider() {
				@Override
				public void getOutline(View view, Outline outline) {
					outline.setOval(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + getRealWidth(), getPaddingTop() + getRealHeight());
				}
			});
			setClipToOutline(true);
			setElevation(19.6f);
		} else {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);

			mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mButtonPaint.setColor(FloatingActionButtonColor);
			mButtonPaint.setStyle(Paint.Style.FILL);
			mButtonPaint.setShadowLayer(10.0f, 0.0f, 3.5f, Color.argb(100, 0, 0, 0));
		}

		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mRipple != null) {
			mRipple.setBounds(0, 0, getWidth(), getHeight());
			mRipple.draw(canvas);
		} else {
			canvas.drawCircle(getPaddingLeft() + getRealWidth() / 2,
				getPaddingTop() + getRealHeight() / 2,
				(float) getRealWidth() / 2.6f, mButtonPaint);
		}

		canvas.drawBitmap(mBitmap, getPaddingLeft() + (getRealWidth() - mBitmap.getWidth()) / 2,
				getPaddingTop() + (getRealHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);

	}

	private int getRealWidth() {
		return getWidth() - getPaddingLeft() - getPaddingRight();
	}

	private int getRealHeight() {
		return getHeight() - getPaddingTop() - getPaddingBottom();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		if (mRipple == null) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				setAlpha(1.0f);
			} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setAlpha(0.6f);
			}
		}
		return ret;
	}

	@Override
	public void onAnimationCancel(Animator anim) {

	}

	@Override
	public void onAnimationEnd(Animator anim) {
		if (mHidden) {
			setVisibility(View.GONE);
		}
	}

	@Override
	public void onAnimationRepeat(Animator anim) {

	}

	@Override
	public void onAnimationStart(Animator anim) {

	}


	public void hideFloatingActionButton() {
		if (!mHidden) {
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(scaleX, scaleY);
			animSetXY.setInterpolator(accelerateInterpolator);
			animSetXY.setDuration(100);
			animSetXY.start();
			animSetXY.addListener(this);
			mHidden = true;
		}
	}

	public void showFloatingActionButton() {
		if (mHidden) {
			setVisibility(View.VISIBLE);
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(scaleX, scaleY);
			animSetXY.setInterpolator(overshootInterpolator);
			animSetXY.setDuration(200);
			animSetXY.start();
			mHidden = false;
		}
	}

	public boolean isHidden() {
		return mHidden;
	}

	static public class Builder {
		private FrameLayout.LayoutParams params;
		private final Activity activity;
		int gravity = Gravity.BOTTOM | Gravity.RIGHT; // default bottom right
		Drawable drawable;
		int color = Color.WHITE;
		int size = 0;
		float scale = 0;
		int paddingLeft = 0,
			paddingTop = 0,
			paddingBottom = 0,
			paddingRight = 0;

		public Builder(Activity context) {
			scale = context.getResources().getDisplayMetrics().density;
			size = convertToPixels(72, scale); // default size is 72dp by 72dp
			params = new FrameLayout.LayoutParams(size, size);
			params.gravity = gravity;

			this.activity = context;
		}

		/**
		 * Sets the gravity for the FAB
		 */
		public Builder withGravity(int gravity) {
			this.gravity = gravity;
			return this;
		}

		/**
		 * Sets the margins for the FAB in dp
		 */
		public Builder withPaddings(int left, int top, int right, int bottom) {
			paddingLeft = convertToPixels(left, scale);
			paddingTop = convertToPixels(top, scale);
			paddingRight = convertToPixels(right, scale);
			paddingBottom = convertToPixels(bottom, scale);
			return this;
		}

		/**
		 * Sets the FAB drawable
		 */
		public Builder withDrawable(final Drawable drawable) {
			this.drawable = drawable;
			return this;
		}

		/**
		 * Sets the FAB color
		 */
		public Builder withButtonColor(final int color) {
			this.color = color;
			return this;
		}

		/**
		 * Sets the FAB size in dp
		 */
		public Builder withButtonSize(int size) {
			size = convertToPixels(size, scale);
			params = new FrameLayout.LayoutParams(size, size);
			return this;
		}

		public FloatingActionButton create() {
			final FloatingActionButton button = new FloatingActionButton(activity);
			button.setFloatingActionButtonColor(this.color);
			button.setFloatingActionButtonDrawable(this.drawable);
			button.setPadding(paddingLeft, paddingTop, paddingBottom, paddingRight);
			params.gravity = this.gravity;
			ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
			root.addView(button, params);
			return button;
		}

		// The calculation (value * scale + 0.5f) is a widely used to convert to dps to pixel units
		// based on density scale
		// see developer.android.com (Supporting Multiple Screen Sizes)
		private int convertToPixels(int dp, float scale){
			return (int) (dp * scale + 0.5f) ;
		}
	}
}
