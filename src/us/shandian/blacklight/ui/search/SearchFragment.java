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

package us.shandian.blacklight.ui.search;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import us.shandian.blacklight.R;

public class SearchFragment extends Fragment
{
	public static interface Searcher {
		public void search(String q);
	}
	
	private View mAction;
	private Spinner mTypes;
	private EditText mText;
	
	private Fragment[] mFragments = new Fragment[2];
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// Create action bar view first
		getActivity().getActionBar().setCustomView(R.layout.search_action);
		
		mAction = getActivity().getActionBar().getCustomView();
		String[] types = getResources().getStringArray(R.array.search_type);
		
		mTypes = (Spinner) mAction.findViewById(R.id.search_spinner);
		mText = (EditText) mAction.findViewById(R.id.search_text);
		
		mTypes.setAdapter(new ArrayAdapter(getActivity(), R.layout.action_spinner_item, types));
		
		setHasOptionsMenu(true);
		
		// Then the main layout
		View v = inflater.inflate(R.layout.empty_frame, null);
		
		// Fragments
		mFragments[0] = new SearchStatusFragment();
		mFragments[1] = new SearchUserFragment();
		
		return v;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (getActivity() == null || getActivity().getActionBar() == null) return;
		
		if (!hidden) {
			getActivity().getActionBar().setDisplayShowCustomEnabled(true);
		} else {
			getActivity().getActionBar().setDisplayShowCustomEnabled(false);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getActivity().getMenuInflater().inflate(R.menu.search, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.do_search) {
			Fragment f = mFragments[mTypes.getSelectedItemPosition()];
			
			if (f instanceof Searcher) {
				((Searcher) f).search(mText.getText().toString());
				getFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
			}
			
			return true;
		}

        return false;
	}
}
