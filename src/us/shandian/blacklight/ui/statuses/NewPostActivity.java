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
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;

import android.support.v4.widget.DrawerLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import java.io.IOException;
import java.util.ArrayList;

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

public class NewPostActivity extends AbsActivity implements View.OnLongClickListener
{
	private static final String TAG = NewPostActivity.class.getSimpleName();
	
	private static final int REQUEST_PICK_IMG = 1001, REQUEST_CAPTURE_PHOTO = 1002;
	
	@InjectView(R.id.post_edit) protected EditText mText;
	@InjectView(R.id.post_back) ImageView mBackground;
	@InjectView(R.id.post_count) TextView mCount;
	@InjectView(R.id.post_drawer)  DrawerLayout mDrawer;
	@InjectView(R.id.post_avatar) ImageView mAvatar;
	@InjectView(R.id.post_scroll) HorizontalScrollView mScroll;
	@InjectView(R.id.post_pics) LinearLayout mPicsParent;

	// Actions
	@InjectView(R.id.post_pic) protected ImageView mPic;
	@InjectView(R.id.post_emoji) protected ImageView mEmoji;
	@InjectView(R.id.post_at) protected ImageView mAt;
	@InjectView(R.id.post_topic) protected ImageView mTopic;
	@InjectView(R.id.post_send) protected ImageView mSend;
	
	private ImageView[] mPics = new ImageView[9];

	private LoginApiCache mLoginCache;
	private UserApiCache mUserCache;
	private UserModel mUser;
	
	// Fragments
	private EmoticonFragment mEmoticonFragment;
	private ColorPickerFragment mColorPickerFragment;

	// Picked picture
	private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();

	// Long?
	private boolean mIsLong = false;

	// Filter color
	private int mFilter;

	// Foreground
	private int mForeground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_status);

		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new GetAvatarTask().execute();
		
		// Inject
		ButterKnife.inject(this);
		
		// Fragments
		mEmoticonFragment = new EmoticonFragment();
		mColorPickerFragment = new ColorPickerFragment();
		getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();

		// Filter
		try {
			TypedArray array = getTheme().obtainStyledAttributes(R.styleable.BlackLight);
			mFilter = array.getColor(R.styleable.BlackLight_NewPostImgFilter, 0);
			mForeground = array.getColor(R.styleable.BlackLight_NewPostForeground, 0);
			array.recycle();
		} catch (Exception e) {
			mFilter = 0;
		}

		// Listeners
		mEmoticonFragment.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
				@Override
				public void onEmoticonSelected(String name) {
					mText.getText().insert(mText.getSelectionStart(), name);
					mDrawer.closeDrawer(Gravity.RIGHT);
				}
		});

		mColorPickerFragment.setOnColorSelectedListener(new ColorPickerFragment.OnColorSelectedListener() {
			@Override
			public void onSelected(String hex) {
				int sel = mText.getSelectionStart();
				mText.getText().insert(sel, "[" + hex + "  [d");
				mText.setSelection(sel + 9);
				mDrawer.closeDrawer(Gravity.RIGHT);
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
						mCount.setTextColor(mForeground);
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

				if (mEmoji != null) {
					if (mIsLong) {
						getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mColorPickerFragment).commit();
						mEmoji.setImageResource(R.drawable.ic_action_edit);
					} else {
						getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
						mEmoji.setImageResource(R.drawable.ic_emoji);

					}
				}
			}
		});

		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mText.requestFocus();
				mText.requestFocusFromTouch();
			}
		});

		// Imgs
		for (int i = 0; i < 9; i++) {
			mPics[i] = (ImageView) mPicsParent.getChildAt(i);
			mPics[i].setOnLongClickListener(this);
		}

		// Handle share intent
		Intent i = getIntent();

		if (i != null && i.getType() != null) {
			if (i.getType().contains("text/plain")) {
				mText.setText(i.getStringExtra(Intent.EXTRA_TEXT));
			} else if (i.getType().contains("image/")) {
				Uri uri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					addPicture(bitmap);
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
			Cursor cursor = getContentResolver().query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
			cursor.moveToFirst();
			String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			cursor.close();
			
			// Then decode
			addPicture(BitmapFactory.decodeFile(filePath));
		}

		// Captured photo
		if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
			addPicture(BitmapFactory.decodeFile(Utility.lastPicPath));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		for (int i = 0; i < 9; i++) {
			if (mPics[i] == v) {
				removePicture(i);
				break;
			}
		}
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}


	@OnClick(R.id.post_send)
	public void send() {
		try {
			if (!TextUtils.isEmpty(mText.getText().toString())) {
				new Uploader().execute();
			} else {
				Toast.makeText(this, R.string.empty_weibo, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
					
		}
	} 
	
	@OnClick(R.id.post_pic) 
	public void pic() {
		if (mBitmaps.size() < 9) {
			showPicturePicker();
		}
	}
	
	@OnClick(R.id.post_emoji)
	public void emoji() {
		if (mDrawer.isDrawerOpen(Gravity.END)) {
			mDrawer.closeDrawer(Gravity.END);
		} else {
			mDrawer.openDrawer(Gravity.END);
		}
	}
	
	@OnClick(R.id.post_at) 
	public void at() {
		AtUserSuggestDialog diag = new AtUserSuggestDialog(this);
		diag.setListener(new AtUserSuggestDialog.AtUserListener() {
			@Override
			public void onChooseUser(String name) {
				mText.getText().insert(mText.getSelectionStart(), " @" + name +" ");
			}
		});
		diag.show();
	}

	@OnClick(R.id.post_topic)
	public void topic() {
		CharSequence text = mText.getText();
		mText.getText().insert(mText.getSelectionStart(), "##");
		if(text instanceof Spannable) {
			Selection.setSelection((Spannable) text, text.length() - 1);
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
								Uri uri = Utility.getOutputMediaFileUri();
								captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
								startActivityForResult(captureIntent, REQUEST_CAPTURE_PHOTO);
								break;
						}
					}
				}
		).show();
	}

	private void addPicture(Bitmap bitmap){
		mBitmaps.add(bitmap);
		updatePictureView();
	}

	private void removePicture(int id) {
		mBitmaps.remove(id);
		updatePictureView();
	}

	private void updatePictureView() {
		if (mBitmaps.size() > 0) {
			mScroll.setVisibility(View.VISIBLE);
		} else {
			mScroll.setVisibility(View.GONE);
		}

		for (int i = 0; i < 9; i++) {
			Bitmap bmp = mBitmaps.size() > i ? mBitmaps.get(i) : null;
			if (bmp != null) {
				mPics[i].setVisibility(View.VISIBLE);
				mPics[i].setImageBitmap(bmp);
			} else {
				mPics[i].setVisibility(View.GONE);
				mPics[i].setImageBitmap(null);
			}
		}
	}

	// if extended, this should be overridden
	protected boolean post() {
		if (!mIsLong) {
			if (mBitmaps.size() == 0) {
				return PostApi.newPost(mText.getText().toString());
			} else {
				return postPics(mText.getText().toString());
			}
		} else {
			if (DEBUG) {
				Log.d(TAG, "Preparing to post a long post");
			}

			Bitmap bmp = null;

			// Post the first picture with long post
			if (mBitmaps.size() > 0) {
				bmp = mBitmaps.get(0);
				mBitmaps.remove(0);
			}

			bmp = Utility.parseLongPost(this, mText.getText().toString(), bmp);
			mBitmaps.add(0, bmp);

			return postPics(Utility.parseLongContent(this, mText.getText().toString()));
		}
	}

	private boolean postPics(String status) {
		// Upload pictures first
		String pics = "";
		
		for (int i = 0; i < mBitmaps.size(); i++) {
			Bitmap bmp = mBitmaps.get(i);
			String id = PostApi.uploadPicture(bmp);
			if (id == null || id.trim().equals("")) return false;

			pics += id;

			if (i < mBitmaps.size() - 1) {
				pics += ",";
			}
		}

		// Upload text
		return PostApi.newPostWithMultiPics(status, pics);
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
	
	private class GetAvatarTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// User first
			mUser = mUserCache.getUser(mLoginCache.getUid());

			// Avatar
			Bitmap avatar = mUserCache.getLargeAvatar(mUser);
			if (avatar != null)
				publishProgress(1, avatar);
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			switch (Integer.valueOf(values[0].toString())) {
				case 1:
					mAvatar.setImageBitmap((Bitmap) values[1]);
					break;
			}
			super.onProgressUpdate();
		}
		
	}
}
