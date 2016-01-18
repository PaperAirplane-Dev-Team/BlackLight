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

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.directmessages.DirectMessagesApi;
import info.papdt.blacklight.model.DirectMessageListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.support.LogF;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.DirectMessageAdapter;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.EmoticonFragment;
import info.papdt.blacklight.ui.common.SwipeRefreshLayout;
import info.papdt.blacklight.ui.common.SwipeUpAndDownRefreshLayout;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class DirectMessageConversationActivity extends AbsActivity implements SwipeRefreshLayout.OnRefreshListener
		, PopupMenu.OnMenuItemClickListener {
	private static final String TAG = DirectMessageConversationActivity.class.getSimpleName();
	private static final int REQUEST_PICK_IMG = 1001, REQUEST_CAPTURE_PHOTO = 1002;
	private static final int MENU_PICK = 0, MENU_TAKE = 1;

	private UserModel mUser;
	private DirectMessageListModel mMsgList = new DirectMessageListModel();
	private int mPage = 0;
	private boolean mRefreshing = false;

	private ListView mList;
	private EditText mText;
	private ImageView mPickPic;
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
		mPickPic = Utility.findViewById(this, R.id.direct_message_pick_pic);
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

		// Picture Picker
		final PopupMenu pickPicMenu = new PopupMenu(this,mPickPic);
		pickPicMenu.inflate(R.menu.pic_popup);
		pickPicMenu.setOnMenuItemClickListener(this);
		mPickPic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				pickPicMenu.show();
			}
		});

		new Refresher().execute(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Image picked, decode
		if (requestCode == REQUEST_PICK_IMG && resultCode == RESULT_OK) {
			if (Build.VERSION.SDK_INT >= 19) {
				try {
					ParcelFileDescriptor parcelFileDescriptor =
							getContentResolver().openFileDescriptor(data.getData(), "r");
					FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
					Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
					parcelFileDescriptor.close();
					sendPicture(image);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Cursor cursor = getContentResolver().query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
				cursor.moveToFirst();
				String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();

				// Then decode
				sendPicture(filePath);
			}
		} else if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
			sendPicture(Utility.lastPicPath);
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

	private void sendPicture (Bitmap pic){
		Log.d(TAG,"send bitmap");
		new Uploader().execute(pic);
	}

	private void sendPicture (String path){
		Log.d(TAG,"send url");
		try {
			sendPicture(BitmapFactory.decodeFile(path));
		} catch (OutOfMemoryError e) {

			return;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		int i1 = menuItem.getItemId();
		if (i1 == R.id.dm_pic_gallery) {
			Intent i = new Intent();
			if (Build.VERSION.SDK_INT >= 19) {
				i.setAction(Intent.ACTION_OPEN_DOCUMENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("image/*");
			} else {
				i.setAction(Intent.ACTION_PICK);
				i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			}
			startActivityForResult(i, REQUEST_PICK_IMG);

		} else if (i1 == R.id.dm_pic_take) {
			Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri uri = Utility.getOutputMediaFileUri();
			captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(captureIntent, REQUEST_CAPTURE_PHOTO);

		}
		return true;
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

	private class Sender extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mRefreshing = true;
			mSwipeRefresh.setIsDown(true);
			mSwipeRefresh.setRefreshing(true);

			mText.setEnabled(false);
		}

		@Override
		protected Void doInBackground(String... params) {
			if (DEBUG) {
				Log.d(TAG, "Begin sending direct message");
				if (params.length > 0) {
					LogF.d(TAG, "Begin sending dm with pic uploaded: %s", params[0]);
				}
			}

			if (TextUtils.isEmpty(mText.getText())) {
				//TODO Abort!
			}

			DirectMessagesApi.send(mUser.id, mText.getText().toString(),params);

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

	private class Uploader extends AsyncTask<Bitmap,Void,String>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mRefreshing = true;
			mSwipeRefresh.setIsDown(true);
			mSwipeRefresh.setRefreshing(true);

			mText.setEnabled(false);

			if (TextUtils.isEmpty(mText.getText())){
				mText.setText(R.string.post_photo);
			}
		}

		@Override
		protected String doInBackground(Bitmap... params) {
			if (DEBUG) {
				Log.d(TAG, "Begin uploading photo");
			}

			String pic_id = DirectMessagesApi.uploadPicture(params[0], mUser.id);

			if (DEBUG) {
				Log.d(TAG, "Finished");
			}

			return pic_id;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new Sender().execute(result);
		}
	}
}
