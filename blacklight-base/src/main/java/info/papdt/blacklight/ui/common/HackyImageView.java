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
import android.util.AttributeSet;
import android.widget.ImageView;

// Hack ImageView to make it square
public class HackyImageView extends ImageView
{
	public HackyImageView(Context context) {
		this(context, null);
	}
	
	public HackyImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public HackyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// This is not a typo
		// This hack makes it square
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
