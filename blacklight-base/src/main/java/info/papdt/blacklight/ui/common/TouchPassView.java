package info.papdt.blacklight.ui.common;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class TouchPassView extends ViewGroup
{
	private View mTarget;
	
	public TouchPassView(Context context, View target) {
		super(context);
		mTarget = target;
		setClickable(true);
	}

	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5) {
		// THIS VIEW DOES NOT IMPLEMENT THIS
	}

	@Override
	public boolean canScrollHorizontally(int direction) {
		return mTarget.canScrollHorizontally(direction);
	}

	@Override
	public boolean canScrollVertically(int direction) {
		return mTarget.canScrollVertically(direction);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mTarget.dispatchTouchEvent(event);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mTarget.onTouchEvent(event);
		return true;
	}
}
