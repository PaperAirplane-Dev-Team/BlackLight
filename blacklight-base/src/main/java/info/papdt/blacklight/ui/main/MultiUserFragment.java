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
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;

public class MultiUserFragment extends Fragment implements AdapterView.OnItemClickListener
{
	private ListView mList;
	private ArrayAdapter<String> mAdapter;
	private LoginApiCache mCache;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_drawer_group, container, false);
		mList = Utility.findViewById(v, R.id.drawer_group_list);
		
		if (mAdapter != null) {
			mList.setAdapter(mAdapter);
		}
		
		mList.setOnItemClickListener(this);
		
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCache = new LoginApiCache(activity);
		reload(activity);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		new SwitchTask().execute(pos);
	}
	
	public void reload() {
		mCache.reloadMultiUser();
		reload(getActivity());
	}
	
	private void reload(Activity activity) {
		mAdapter = new ArrayAdapter<String>(activity, R.layout.main_drawer_group_item, R.id.group_title, mCache.getUserNames());
		
		if (mList != null)
			mList.setAdapter(mAdapter);
	}
	
	private class SwitchTask extends AsyncTask<Integer, Void, Void> {
		ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			mMuCallBack.closeDrawer();
			prog = new ProgressDialog(getActivity());
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			mCache.switchToUser(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			prog.dismiss();
			mMuCallBack.syncAccount();
		}
	}
	interface MuCallBack{
		void syncAccount();
		void closeDrawer();
	}

	static MuCallBack mMuCallBack;
	static void setMuCallBack(MuCallBack muCallBack){
		mMuCallBack=muCallBack;
	}
	
}
