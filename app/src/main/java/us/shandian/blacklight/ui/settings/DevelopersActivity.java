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

package us.shandian.blacklight.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.UserAdapter;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

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

	@Override
	protected View getSwipeView() {
		return findViewById(R.id.settings);
	}
	
	private class UserGetter extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... args) {
			String[] developerWeiboUids=getResources().getStringArray(R.array.developer_weibo_uids);
			for(String uid : developerWeiboUids){
				try{
					UserModel m = UserApi.getUser(uid);
					if (m != null) {
						mUserListOfDevelopers.getList().add(m);
					}
				} catch(Exception e) {
					
				}
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mAdapterOfDevelopers = new UserAdapter(DevelopersActivity.this, mUserListOfDevelopers);
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
