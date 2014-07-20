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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.UserAdapter;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class DevelopersActivity extends Activity implements AdapterView.OnItemClickListener
{
	private UserAdapter mAdapter;
	private UserListModel mUsers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (hasSmartBar()) {
			Utility.enableTint(this);
		}

		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);

		mUsers = new UserListModel();
		ListView developers = new ListView(this);
		developers.setOnItemClickListener(this);
		mAdapter = new UserAdapter(this, mUsers);
		developers.setAdapter(mAdapter);
		setContentView(developers);
		new UserGetter().execute(mUsers);
		
	}
	
	private class UserGetter extends AsyncTask<UserListModel,Void,Boolean>{

		@Override
		protected Boolean doInBackground(UserListModel... users) {
			String[] developerWeiboUids=getResources().getStringArray(R.array.developer_weibo_uids);
			for(String uid:developerWeiboUids){
				try{
					users[0].getList().add(UserApi.getUser(uid));
				}
				catch(Exception e){
					//错误处理
				}
			};
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mAdapter.notifyDataSetChangedAndClear();
		}
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, UserTimeLineActivity.class);
		i.putExtra("user", mUsers.get(position));
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
