package us.shandian.blacklight.support;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import us.shandian.blacklight.R;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/*
  Hack to fix conflict between URLSpan and parent's OnClickListener
*/
public class HackyMovementMethod extends LinkMovementMethod
{
	private static final String TAG = HackyMovementMethod.class.getSimpleName();
	
	private static HackyMovementMethod sInstance;
	
	private BackgroundColorSpan mGray;
	
	private boolean mIsLinkHit = false;
	
	public static HackyMovementMethod getInstance() {
		if (sInstance == null) {
			sInstance = new HackyMovementMethod();
		}
		
		return sInstance;
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
		if (mGray == null) {
			mGray = new BackgroundColorSpan(widget.getContext().getResources().getColor(R.color.selector_gray));
		}
		
		mIsLinkHit = false;
		
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			
			if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
			}
			
			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();
			
			if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
			}
			
			x += widget.getScrollX();
			y += widget.getScrollY();
			
			int line = widget.getLayout().getLineForVertical(y);
			int offset = widget.getLayout().getOffsetForHorizontal(line, x);
			
			ClickableSpan[] spans = buffer.getSpans(offset, offset, ClickableSpan.class);
			
			if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
				Log.d(TAG, "line = " + line + " offset = " + offset);
				Log.d(TAG, "spans.lenth = " + spans.length);
			}
			
			if (spans.length != 0) {
				int start = buffer.getSpanStart(spans[0]);
				int end = buffer.getSpanEnd(spans[0]);
				
				mIsLinkHit = true;
				
				if (action == MotionEvent.ACTION_DOWN) {
					if (DEBUG) {
						Log.d(TAG, "Down event detected");
					}
					
					buffer.setSpan(mGray, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (action == MotionEvent.ACTION_UP) {
					if (DEBUG) {
						Log.d(TAG, "Up event detected");
					}
					
					spans[0].onClick(widget);
					
					buffer.removeSpan(mGray);
				}
				
				return true;
			}
		} else {
			buffer.removeSpan(mGray);
		}
		
		return Touch.onTouchEvent(widget, buffer, event);
	}
	
	public boolean isLinkHit() {
		boolean ret = mIsLinkHit;
		mIsLinkHit = false;
		return ret;
	}
}
