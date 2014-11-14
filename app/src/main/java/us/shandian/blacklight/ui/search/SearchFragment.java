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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.shandian.blacklight.R;

public class SearchFragment extends Fragment
{
	public static interface Searcher {
		public void search(String q);
	}
	
	@InjectView(R.id.action_search) View mAction;
	@InjectView(R.id.search_spinner) Spinner mTypes;
	@InjectView(R.id.search_text) EditText mText;
	
	private Fragment[] mFragments = new Fragment[2];
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inject
		ButterKnife.inject(this, getActivity());

		String[] types = getResources().getStringArray(R.array.search_type);
		
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
		
		if (getActivity() == null || getActivity().getActionBar() == null || mAction == null) return;
		
		if (!hidden) {
			mAction.setVisibility(View.VISIBLE);
		} else {
			mAction.setVisibility(View.GONE);
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
