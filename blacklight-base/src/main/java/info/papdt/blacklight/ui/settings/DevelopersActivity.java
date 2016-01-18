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

package info.papdt.blacklight.ui.settings;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.Random;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.user.UserApi;
import info.papdt.blacklight.model.UserListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.UserAdapter;
import info.papdt.blacklight.ui.common.AbsActivity;

public class DevelopersActivity extends AbsActivity
{
	private UserAdapter mAdapterOfDevelopers;
	private UserListModel mUserListOfDevelopers;
	private RecyclerView mDevelopers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.settings;
		super.onCreate(savedInstanceState);

		mUserListOfDevelopers = new UserListModel();
		mDevelopers = new RecyclerView(this);
		mDevelopers.setLayoutManager(new LinearLayoutManager(this));

		ViewGroup vg = Utility.findViewById(this, R.id.settings);
		vg.addView(mDevelopers);

		new UserGetter().execute();

	}

	private class UserGetter extends AsyncTask<Void, Void, Boolean>{

		private void addDeveloper(String uid){
			try{
				UserModel m = UserApi.getUser(uid);
				if (m != null) {
					mUserListOfDevelopers.getList().add(m);
				}
			} catch(Exception e) {

			}
		}

		@Override
		protected Boolean doInBackground(Void... args) {
			addDeveloper("1789004515"); //We will NEVER fire Peter!

			String[] contributorWeiboUids = getResources().getStringArray(R.array.contributor_weibo_uids);
			int developerNum = contributorWeiboUids.length;
			boolean[] added = new boolean[developerNum];
			int addNum = 0;
			Random r = new Random();
			while (addNum != developerNum){
				int n ;
				do {
					n = r.nextInt(developerNum);
				} while (added[n]);
				addDeveloper(contributorWeiboUids[n]);
				added[n] = true;
				addNum++;
			}

			String[] thankWeiboUids = getResources().getStringArray(R.array.thank_weibo_uids);
			for(String uid : thankWeiboUids){
				addDeveloper(uid);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mAdapterOfDevelopers = new UserAdapter(DevelopersActivity.this, mUserListOfDevelopers, mDevelopers);
			mDevelopers.setAdapter(mAdapterOfDevelopers);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
