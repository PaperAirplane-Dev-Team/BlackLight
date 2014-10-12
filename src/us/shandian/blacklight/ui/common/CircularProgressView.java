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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

// This cannot be inflated by LayoutInflater.
public class CircularProgressView extends View {

	private int mForeground = Color.WHITE;
	private int mBackground = Color.GRAY;
	private float mProgress = 0.0f;

	public CircularProgressView(Context context) {
		super(context);
		setWillNotDraw(false);
	}

	public void setColor(int foreground, int background) {
		mForeground = foreground;
		mBackground = background;
		invalidate();
	}

	public void setProgress(float value) {
		mProgress = value;
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(mForeground);
		p.setStyle(Paint.Style.FILL);

		RectF r = new RectF();
		r.left = 1;
		r.top = 1;
		r.right = getWidth() - 1;
		r.bottom = getHeight() - 1;

		float angle = mProgress * 360;
		canvas.drawArc(r, 0, angle, true, p);
		p.setColor(mBackground);
		canvas.drawArc(r, angle, 360 - angle, true, p);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(2.0f);
		p.setColor(mForeground);
		canvas.drawArc(r, 0, 360.0f, false, p);
	}

}
