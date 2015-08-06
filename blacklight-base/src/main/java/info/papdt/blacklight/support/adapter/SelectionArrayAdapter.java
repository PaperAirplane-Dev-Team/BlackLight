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

package info.papdt.blacklight.support.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SelectionArrayAdapter<T> extends ArrayAdapter<T>
{
	private int mSelection = -1;
	private int mSelectorColor = -1;

	public SelectionArrayAdapter(Context context, int layoutRes, int titleId, int selectorColor, T[] array) {
		super(context, layoutRes, titleId, array);
		mSelectorColor = selectorColor;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		if (v == null)
			return null;

		if (position == mSelection) {
			v.setBackgroundColor(mSelectorColor);
		} else {
			v.setBackgroundDrawable(null);
		}

		return v;
	}

	public void setSelection(int selection) {
		mSelection = selection;
		notifyDataSetChanged();
	}
}
