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

package info.papdt.blacklight.ui.directmessage;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.directmessages.DirectMessagesApi;
import info.papdt.blacklight.model.DirectMessageListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.DirectMessageAdapter;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.EmoticonFragment;
import info.papdt.blacklight.ui.common.SwipeRefreshLayout;
import info.papdt.blacklight.ui.common.SwipeUpAndDownRefreshLayout;

import static info.papdt.blacklight.BuildConfig.DEBUG;
import static info.papdt.blacklight.support.Utility.hasSmartBar;

public class DirectMessageConversationActivity extends AbsActivity implements SwipeRefreshLayout.OnRefreshListener {
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
		mLayout = R.layout.direct_message_conversation;
		super.onCreate(savedInstanceState);

		// Argument
		mUser = getIntent().getParcelableExtra("user");
		getSupportActionBar().setTitle(mUser.getName());

		// Initialize views
		mList = Utility.findViewById(this, R.id.direct_message_conversation);
		mText = Utility.findViewById(this, R.id.direct_message_send_text);
		mSend = Utility.findViewById(this, R.id.direct_message_send);
		mSwipeRefresh = Utility.findViewById(this, R.id.direct_message_refresh);

		// Events
		Utility.bindOnClick(this, mSend, "send");

		// View
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setDownHasPriority();
		mSwipeRefresh.setColorScheme(R.color.ptr_green, R.color.ptr_orange, R.color.ptr_red, R.color.ptr_blue);

		mList.setStackFromBottom(true);
		mAdapter = new DirectMessageAdapter(this, mMsgList, mUser.id);
		mList.setAdapter(mAdapter);

		mList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
									   int oldTop, int oldRight, int oldBottom) {
				if (bottom<oldBottom)
					mList.smoothScrollToPosition(ListView.FOCUS_DOWN);
			}
		});

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

	@Binded
	void send() {
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

			mAdapter.notifyDataSetChanged();

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
