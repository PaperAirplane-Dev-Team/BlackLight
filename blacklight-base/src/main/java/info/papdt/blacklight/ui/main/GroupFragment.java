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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.GroupsApi;
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.SelectionArrayAdapter;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class GroupFragment extends Fragment implements AdapterView.OnItemClickListener
{
	private static final String TAG = GroupFragment.class.getSimpleName();

	private static final String BILATERAL = "bilateral";

	private ListView mList;
	private GroupListModel mGroups;
	private String mCurrentGroup;
	private SelectionArrayAdapter<String> mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_drawer_group, container, false);
		mList = Utility.findViewById(v, R.id.drawer_group_list);
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCurrentGroup = Settings.getInstance(activity).getString(Settings.CURRENT_GROUP, null);
		((MainActivity) activity).setCurrentGroup(mCurrentGroup, false);
		new FetchTask().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0)
			mCurrentGroup = null;
		else if (position == 1)
			mCurrentGroup = BILATERAL;
		else
			mCurrentGroup = mGroups.get(position - 2).idstr;

		Settings.getInstance(null).putString(Settings.CURRENT_GROUP, mCurrentGroup);
		mAdapter.setSelection(position);
		((MainActivity) getActivity()).setCurrentGroup(mCurrentGroup, true);
		mGfCallBack.onItemClick();
	}

	interface GFCallBack{
		void onItemClick();
	}

	private static GFCallBack mGfCallBack;

	static void setGfCallBack(GFCallBack gfCallBack){
		mGfCallBack=gfCallBack;
	}

	void reload() {
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

				if (!isAdded()){
                    return;
                }
				mAdapter = new SelectionArrayAdapter<String>(getActivity(), R.layout.main_drawer_group_item, R.id.group_title, Utility.getSelectorGrey(getActivity()), names);
				mList.setAdapter(mAdapter);
				mList.setOnItemClickListener(GroupFragment.this);

				// Search for current
				int current = 0;

				if (mCurrentGroup == null) {
					current = 0;
				} else if (mCurrentGroup.equals(BILATERAL)) {
					current = 1;
				} else {
					for (int i = 0; i < mGroups.getSize(); i++) {
						if (mGroups.get(i).idstr.equals(mCurrentGroup)) {
							current = i + 2;
							break;
						}
					}
				}

				if (DEBUG) {
					Log.d(TAG, "selection = " + current);
				}

				mAdapter.setSelection(current);
			}
		}

	}

}
