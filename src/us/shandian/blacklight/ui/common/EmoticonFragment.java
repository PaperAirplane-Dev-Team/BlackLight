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

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.support.Emoticons;
import us.shandian.blacklight.support.adapter.EmoticonAdapter;

public class EmoticonFragment extends Fragment implements AdapterView.OnItemClickListener
{
	public static interface EmoticonListener {
		public void onEmoticonSelected(String name);
	}
	
	private GridView mGrid;
	private EmoticonAdapter mAdapter;
	
	// listener
	private EmoticonListener mListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.emoticon_fragment, null);
		mGrid = (GridView) v.findViewById(R.id.emoticon_grid);
		
		// adapter
		mAdapter = new EmoticonAdapter(getActivity());
		mGrid.setAdapter(mAdapter);
		
		// listener
		mGrid.setOnItemClickListener(this);
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mListener != null) {
			mListener.onEmoticonSelected(mAdapter.getItem(position));
		}
	}
	
	public void setEmoticonListener(EmoticonListener listener) {
		mListener = listener;
	}
}
