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

package us.shandian.blacklight.ui.directmessage;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageView;

import android.support.v4.widget.SwipeRefreshLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.directmessages.DirectMessagesApi;
import us.shandian.blacklight.model.DirectMessageListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.adapter.DirectMessageAdapter;
import us.shandian.blacklight.ui.common.EmoticonFragment;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class DirectMessageConversationActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener
{
	private static final String TAG = DirectMessageConversationActivity.class.getSimpleName();
	
	private UserModel mUser;
	private DirectMessageListModel mMsgList = new DirectMessageListModel();
	private int mPage = 0;
	private boolean mRefreshing = false;
	
	private ListView mList;
	private EditText mText;
	private ImageView mSend;
	private DirectMessageAdapter mAdapter;
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;
	
	private EmoticonFragment mEmoticons;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.direct_message_conversation);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Argument
		mUser = getIntent().getParcelableExtra("user");
		getActionBar().setTitle(mUser.getName());
		
		// View
		mSwipeRefresh = (SwipeUpAndDownRefreshLayout) findViewById(R.id.direct_message_refresh);
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setDownHasPriority();
		mSwipeRefresh.setColorScheme(R.color.ptr_green, R.color.ptr_orange, R.color.ptr_red, R.color.ptr_blue);
		
		mList = (ListView) findViewById(R.id.direct_message_conversation);
		mText = (EditText) findViewById(R.id.direct_message_send_text);
		mSend = (ImageView) findViewById(R.id.direct_message_send);
		
		mList.setStackFromBottom(true);
		mAdapter = new DirectMessageAdapter(this, mMsgList, mUser.id);
		mList.setAdapter(mAdapter);
		
		mSend.setOnClickListener(this);
		
		// Emoticon Picker
		mEmoticons = new EmoticonFragment();
		mEmoticons.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
			@Override
			public void onEmoticonSelected(String name) {
				if (!mRefreshing) {
					mText.getText().append(name);
				}
			}
		});
		getFragmentManager().beginTransaction().replace(R.id.direct_message_emoticons, mEmoticons).commit();
		
		new Refresher().execute(true);
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

	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(mSwipeRefresh.isDown());
		}
	}
	
	@Override
	public void onClick(View v) {
		if (!mRefreshing) {
			new Sender().execute();
		}
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Boolean> {
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			
			mRefreshing = true;
			mSwipeRefresh.setRefreshing(true);
		}
		
		@Override
		public Boolean doInBackground(Boolean... params) {
			if (params[0]) {
				mPage = 0;
				mMsgList.getList().clear();
			}
			
			DirectMessageListModel list = DirectMessagesApi.getConversation(mUser.id, 10, ++mPage);
			
			mMsgList.addAll(params[0], list);
			
			return params[0];
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (result) {
				mAdapter.notifyDataSetChangedAndClear();
			} else {
				mAdapter.notifyDataSetChanged();
			}
			
			mRefreshing = false;
			mSwipeRefresh.setRefreshing(false);
		}
	}
	
	private class Sender extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mRefreshing = true;
			mSwipeRefresh.setIsDown(true);
			mSwipeRefresh.setRefreshing(true);
			
			mText.setEnabled(false);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if (DEBUG) {
				Log.d(TAG, "Begin sending direct message");
			}
			
			DirectMessagesApi.send(mUser.id, mText.getText().toString());
			
			if (DEBUG) {
				Log.d(TAG, "Finished");
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			mText.setText("");
			mText.setEnabled(true);
			new Refresher().execute(true);
		}
	}
}
