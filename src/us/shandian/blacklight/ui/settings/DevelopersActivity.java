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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import us.shandian.blacklight.R;
import us.shandian.blacklight.api.user.UserApi;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.adapter.UserAdapter;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;
import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class DevelopersActivity extends AbsActivity implements AdapterView.OnItemClickListener
{
	private UserAdapter mAdapterOfDevelopers;
	private UserAdapter mAdapterOfThanks;
	private UserListModel mUserListOfDevelopers;
	private UserListModel mUserListOfThanks;
	private ListView mDevelopers;
	private ListView mThanks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserListOfThanks = new UserListModel();
		mUserListOfDevelopers = new UserListModel();
		mDevelopers = new ListView(this);
		mThanks = new ListView(this);
		mThanks.setOnItemClickListener(this);
		mDevelopers.setOnItemClickListener(this);
		LinearLayout mainLayout=new LinearLayout(this);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		//FIXME   It's very ugly! 
		mainLayout.addView(mDevelopers);
		TextView thanks = new TextView(this);
		thanks.setText("感谢");
		mainLayout.addView(thanks);
		mainLayout.addView(mThanks);
		setContentView(mainLayout);
		
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
			String[] thankWeiboUids=getResources().getStringArray(R.array.thank_weibo_uids);
	         for(String uid : thankWeiboUids){
	                try{
	                    UserModel m = UserApi.getUser(uid);
	                    if (m != null) {
	                        mUserListOfThanks.getList().add(m);
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
			mAdapterOfThanks = new UserAdapter(DevelopersActivity.this, mUserListOfThanks);
			mThanks.setAdapter(mAdapterOfThanks);
		}
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, UserTimeLineActivity.class);
		if(view==mDevelopers){
		    i.putExtra("user", mUserListOfDevelopers.get(position));
		}
		else if(view==mThanks){
		    i.putExtra("user", mUserListOfThanks.get(position));
		}
		else{
		    throw new RuntimeException("What the hell?");
		}
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
