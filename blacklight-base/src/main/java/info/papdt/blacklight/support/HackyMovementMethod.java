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

package info.papdt.blacklight.support;

import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import info.papdt.blacklight.R;

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
			
			/*if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
			}*/
			
			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();
			
			/*if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
			}*/
			
			x += widget.getScrollX();
			y += widget.getScrollY();
			
			int line = widget.getLayout().getLineForVertical(y);
			int offset = widget.getLayout().getOffsetForHorizontal(line, x);
			
			ClickableSpan[] spans = buffer.getSpans(offset, offset, ClickableSpan.class);
			
			/*if (DEBUG) {
				Log.d(TAG, "x = " + x + " y = " + y);
				Log.d(TAG, "line = " + line + " offset = " + offset);
				Log.d(TAG, "spans.lenth = " + spans.length);
			}*/
			
			if (spans.length != 0) {
				int start = buffer.getSpanStart(spans[0]);
				int end = buffer.getSpanEnd(spans[0]);
				
				mIsLinkHit = true;
				
				if (action == MotionEvent.ACTION_DOWN) {
					/*if (DEBUG) {
						Log.d(TAG, "Down event detected");
					}*/
					
					buffer.setSpan(mGray, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (action == MotionEvent.ACTION_UP) {
					/*if (DEBUG) {
						Log.d(TAG, "Up event detected");
					}*/
					
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
