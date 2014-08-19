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

package us.shandian.blacklight.ui.statuses;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import android.support.v4.widget.DrawerLayout;

import java.io.IOException;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.comments.CommentOnActivity;
import us.shandian.blacklight.ui.comments.ReplyToActivity;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.common.ColorPickerFragment;
import us.shandian.blacklight.ui.common.EmoticonFragment;
import us.shandian.blacklight.ui.search.AtUserSuggestDialog;
import us.shandian.blacklight.ui.statuses.RepostActivity;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class NewPostActivity extends AbsActivity
{
	private static final String TAG = NewPostActivity.class.getSimpleName();
	
	private static final int REQUEST_PICK_IMG = 1001, REQUEST_CAPTURE_PHOTO = 1002;
	
	protected EditText mText;
	private ImageView mBackground;
	private TextView mCount;
	private DrawerLayout mDrawer;

	private LoginApiCache mLoginCache;
	private UserApiCache mUserCache;
	private UserModel mUser;
	
	// Fragments
	private EmoticonFragment mEmoticonFragment;
	private ColorPickerFragment mColorPickerFragment;

	// Menu
	private MenuItem mEmoticonMenu;
	
	// Picked picture
	private Bitmap mBitmap;

	// Long?
	private boolean mIsLong = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_status);

		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new GetNameTask().execute();
		
		// Init
		mText = (EditText) findViewById(R.id.post_edit);
		mBackground = (ImageView) findViewById(R.id.post_back);
		mCount = (TextView) findViewById(R.id.post_count);
		mDrawer = (DrawerLayout) findViewById(R.id.post_drawer);
		
		// Fragments
		mEmoticonFragment = new EmoticonFragment();
		mColorPickerFragment = new ColorPickerFragment();
		getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
		
		// Listeners
		mEmoticonFragment.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
				@Override
				public void onEmoticonSelected(String name) {
					mText.getText().insert(mText.getSelectionStart(), name);
				}
		});

		mColorPickerFragment.setOnColorSelectedListener(new ColorPickerFragment.OnColorSelectedListener() {
			@Override
			public void onSelected(String hex) {
				int sel = mText.getSelectionStart();
				mText.getText().insert(sel, "[" + hex + "  [d");
				mText.setSelection(sel + 9);
			}
		});
		
		mText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// How many Chinese characters (1 Chinses character = 2 English characters)
				try {
					int length = Utility.lengthOfString(s.toString());
					
					if (DEBUG) {
						Log.d(TAG, "Text length = " + length);
					}
					
					if (length <= 140 && !s.toString().contains("\n")) {
						mCount.setTextColor(getResources().getColor(R.color.gray));
						mCount.setText(String.valueOf(140 - length));
						mIsLong = false;
					} else if (!(NewPostActivity.this instanceof RepostActivity) 
							&& !(NewPostActivity.this instanceof CommentOnActivity)
							&& !(NewPostActivity.this instanceof ReplyToActivity)) {
						mCount.setText(getResources().getString(R.string.long_post));
						mIsLong = true;
					} else {
						mCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
						mCount.setText(String.valueOf(140 - length));
						mIsLong = false;
					}
					
				} catch (Exception e) {
					
				}

				if (mEmoticonMenu != null) {
					if (mIsLong) {
						getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mColorPickerFragment).commit();
						mEmoticonMenu.setIcon(R.drawable.ic_action_edit);
						mEmoticonMenu.setTitle(R.string.color);
					} else {
						getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
						mEmoticonMenu.setIcon(R.drawable.ic_action_emoticon);
						mEmoticonMenu.setTitle(R.string.emoticon);
					}
				}
			}
		});

		// Handle share intent
		Intent i = getIntent();

		if (i != null && i.getType() != null) {
			if (i.getType().indexOf("text/plain") != -1) {
				mText.setText(i.getStringExtra(Intent.EXTRA_TEXT));
			} else if (i.getType().indexOf("image/") != -1) {
				Uri uri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					setPicture(bitmap);
				} catch (IOException e) {
					if (DEBUG) {
						Log.d(TAG, Log.getStackTraceString(e));
					}
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Image picked, decode
		if (requestCode == REQUEST_PICK_IMG && resultCode == RESULT_OK) {
			Cursor cursor = getContentResolver().query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null, null);
			cursor.moveToFirst();
			String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			cursor.close();
			
			// Then decode
			setPicture(BitmapFactory.decodeFile(filePath));
		}

		// Captured photo
		if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			setPicture((Bitmap) extras.get("data"));
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.new_post, menu);
		if (mBitmap != null){
			menu.findItem(R.id.post_pic)
					.setTitle(R.string.delete_picture)
					.setIcon(android.R.drawable.ic_menu_delete);
		}
		mEmoticonMenu = menu.findItem(R.id.post_emoticon);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.isCheckable() && item.isEnabled()) {
			item.setChecked(!item.isChecked());
		}
		
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.post_send) {
			try {
				if (!TextUtils.isEmpty(mText.getText().toString())) {
					new Uploader().execute();
				} else {
					Toast.makeText(this, R.string.empty_weibo, Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
					
			}
			return true;
		} else if (id == R.id.post_pic) {
			if (mBitmap == null){
				showPicturePicker();
			} else {
				setPicture(null);
			}
			return true;
		} else if (id == R.id.post_emoticon) {
			if (mDrawer.isDrawerOpen(Gravity.END)) {
				mDrawer.closeDrawer(Gravity.END);
			} else {
				mDrawer.openDrawer(Gravity.END);
			}
			return true;
		} else if (id == R.id.post_at) {
			AtUserSuggestDialog diag = new AtUserSuggestDialog(this);
			diag.setListener(new AtUserSuggestDialog.AtUserListener() {
				@Override
				public void onChooseUser(String name) {
					mText.getText().insert(mText.getSelectionStart(), " @" + name +" ");
				}
			});
			diag.show();
			return true;		
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void showPicturePicker(){
		new AlertDialog.Builder(this).setItems(getResources().getStringArray(R.array.picture_picker_array),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int id) {
						switch (id) {
							case 0:
								Intent i = new Intent();
								i.setAction(Intent.ACTION_PICK);
								i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
								startActivityForResult(i, REQUEST_PICK_IMG);
								break;
							case 1:
								Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								startActivityForResult(captureIntent, REQUEST_CAPTURE_PHOTO);
								break;
						}
					}
				}
		).show();
	}

	private void setPicture(Bitmap bitmap){
		mBitmap = bitmap;
		if (bitmap != null) {
			mBackground.setImageBitmap(bitmap);
			mBackground.setVisibility(View.VISIBLE);
			mCount.setBackgroundColor(getResources().getColor(R.color.gray_alpha_lighter));
		} else {
			mBackground.setVisibility(View.INVISIBLE);
			mCount.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		}
		invalidateOptionsMenu();
	}

	// if extended, this should be overridden
	protected boolean post() {
		if (!mIsLong) {
			if (mBitmap == null) {
				return PostApi.newPost(mText.getText().toString());
			} else {
				return PostApi.newPostWithPic(mText.getText().toString(), mBitmap);
			}
		} else {
			return PostApi.newPostWithPic(Utility.parseLongContent(mText.getText().toString()),
					Utility.parseLongPost(this, mText.getText().toString(), mBitmap));
		}
	}
	
	private class Uploader extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			prog = new ProgressDialog(NewPostActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			return post();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			prog.dismiss();
			
			if (result) {
				finish();
			} else {
				new AlertDialog.Builder(NewPostActivity.this)
								.setMessage(R.string.send_fail)
								.setCancelable(true)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int index) {
										dialog.dismiss();
									}
								})
								.create()
								.show();
			}
		}

	}
	
	private class GetNameTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Username first
			mUser = mUserCache.getUser(mLoginCache.getUid());
			publishProgress();
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			getActionBar().setSubtitle(mUser.getName());
			super.onProgressUpdate();
		}
		
	}
}
