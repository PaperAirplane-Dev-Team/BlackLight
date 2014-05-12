package us.shandian.blacklight.ui.common;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ListView;
import android.util.AttributeSet;

import static us.shandian.blacklight.BuildConfig.DEBUG;

/*
  Tiny hack of ListView. Prevents crash when refreshing.
*/
public class HackyListView extends ListView
{
	public HackyListView(Context context) {
		super(context);
	}
	
	public HackyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			return super.onTouchEvent(ev);
		} catch (Exception e) {
			// Ignore!!
			if (DEBUG) {
				e.printStackTrace();
			}
			
			return false;
		}
	}
}
