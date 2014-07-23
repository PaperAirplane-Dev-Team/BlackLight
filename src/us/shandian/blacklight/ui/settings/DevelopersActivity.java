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
import android.widget.AdapterView;
import android.widget.ListView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.adapter.UserAdapter;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

public class DevelopersActivity extends AbsActivity implements AdapterView.OnItemClickListener
{
	private UserAdapter mAdapterOfDevelopers;
	private UserListModel mUserListOfDevelopers;
	private ListView mDevelopers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserListOfDevelopers = new UserListModel();
		mDevelopers = new ListView(this);
		mDevelopers.setOnItemClickListener(this);

		setContentView(mDevelopers);
		
		new UserGetter().execute();
		
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, UserTimeLineActivity.class);
		i.putExtra("user", mUserListOfDevelopers.get(position));
		startActivity(i);
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
