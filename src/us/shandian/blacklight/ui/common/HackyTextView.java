package us.shandian.blacklight.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import android.text.method.MovementMethod;

import us.shandian.blacklight.support.HackyMovementMethod;

/*
  Hack to fix conflict between MovementMethod and OnClickListener
*/
public class HackyTextView extends TextView
{
	public HackyTextView(Context context) {
		super(context);
	}
	
	public HackyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		
		MovementMethod method = getMovementMethod();
		if (method instanceof HackyMovementMethod) {
			return ((HackyMovementMethod) method).isLinkHit();
		}
		
		return ret;
	}
}
