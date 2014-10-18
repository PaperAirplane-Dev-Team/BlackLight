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
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import us.shandian.blacklight.support.HackyMovementMethod;

/*
  Hack to fix conflict between MovementMethod and OnClickListener
*/
public class HackyTextView extends TextView {
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
