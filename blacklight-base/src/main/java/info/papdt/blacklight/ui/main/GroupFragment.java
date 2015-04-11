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

package info.papdt.blacklight.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.GroupsApi;
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class GroupFragment extends Fragment
{
	private static final String TAG = GroupFragment.class.getSimpleName();
	
	private ListView mList;
	private GroupListModel mGroups;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_drawer_group, container, false);
		mList = Utility.findViewById(v, R.id.drawer_group_list);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		new FetchTask().execute();
	}
	
	private class FetchTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mGroups = GroupsApi.getGroups();
			
			if (DEBUG) {
				Log.d(TAG, "group size " + mGroups.getSize());
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mGroups != null && mGroups.getSize() > 0) {
				// Get the name list
				String[] names = new String[mGroups.getSize() + 2];

				names[0] = getResources().getString(R.string.group_all);
				names[1] = getString(R.string.group_bilateral);
				for (int i = 0; i < mGroups.getSize(); i++) {
					names[i + 2] = mGroups.get(i).name;
				}
				
				if (DEBUG) {
					Log.d(TAG, "Setting adapter");
				}
				
				mList.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.main_drawer_group_item, R.id.group_title, names));
			}
		}

	}
	
}
