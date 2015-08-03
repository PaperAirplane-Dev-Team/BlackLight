/*
 * Copyright (C) 2015 Peter Cai
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

package info.papdt.blacklight.ui.statuses;

import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.statuses.PostApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.comments.CommentOnActivity;
import info.papdt.blacklight.ui.comments.ReplyToActivity;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.ColorPickerFragment;
import info.papdt.blacklight.ui.common.EmoticonFragment;
import info.papdt.blacklight.ui.common.MultiPicturePicker;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class NewPostActivity extends AbsActivity implements View.OnLongClickListener
{
	private static final String TAG = NewPostActivity.class.getSimpleName();
	private static final String DRAFT="post_draft";
	private static final int REQUEST_PICK_IMG = 1001, REQUEST_CAPTURE_PHOTO = 1002;

	protected EditText mText;
	private TextView mCount;
	private DrawerLayout mDrawer;
	private ImageView mAvatar;
	private HorizontalScrollView mScroll;
	private LinearLayout mPicsParent;

	// Actions
	protected ImageView mPic;
	protected ImageView mEmoji;
	protected ImageView mAt;
	protected ImageView mTopic;
	protected ImageView mSend;

	// Funny hints
	private String[] mHints;

	private ImageView[] mPics = new ImageView[9];

	private LoginApiCache mLoginCache;
	private UserApiCache mUserCache;
	private UserModel mUser;

	// Fragments
	private EmoticonFragment mEmoticonFragment;
	private ColorPickerFragment mColorPickerFragment;

	// Picked picture
	private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();
	private ArrayList<String> mPaths = new ArrayList<String>();

	// Long?
	private boolean mIsLong = false;

	// Filter color
	private int mFilter;

	// Foreground
	private int mForeground;

	// Version
	protected String mVersion = "";

	//cache drafts
	private SharedPreferences mCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.post_status;
		super.onCreate(savedInstanceState);

		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new GetAvatarTask().execute();

		// Initialize views
		mText = Utility.findViewById(this, R.id.post_edit);
		mCount = Utility.findViewById(this, R.id.post_count);
		mDrawer = Utility.findViewById(this, R.id.post_drawer);
		mAvatar = Utility.findViewById(this, R.id.post_avatar);
		mScroll = Utility.findViewById(this, R.id.post_scroll);
		mPicsParent = Utility.findViewById(this, R.id.post_pics);
		mPic = Utility.findViewById(this, R.id.post_pic);
		mEmoji = Utility.findViewById(this, R.id.post_emoji);
		mAt = Utility.findViewById(this, R.id.post_at);
		mTopic = Utility.findViewById(this, R.id.post_topic);
		mSend = Utility.findViewById(this, R.id.post_send);
		mCache=getSharedPreferences("post_cache",MODE_PRIVATE);

		// Bind onClick events
		Utility.bindOnClick(this, mPic, "pic");
		Utility.bindOnClick(this, mEmoji, "emoji");
		Utility.bindOnClick(this, mAt, "at");
		Utility.bindOnClick(this, mTopic, "topic");
		Utility.bindOnClick(this, mSend, "send");
		Utility.bindOnClick(this,mAvatar,"avatar");

		// Version
		try {
			mVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
		}

		// Hints
		if (Math.random() < 0.42){ // Make this a matter of possibility.
			mHints = getResources().getStringArray(R.array.splashes);
			mText.setHint(mHints[new Random().nextInt(mHints.length)]);
		}

		//draft
		if (needCache())mText.setText(mCache.getString(DRAFT,""));

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

				if (needCache()) mCache.edit().putString(DRAFT, s.toString()).apply();
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
						mEmoji.setImageResource(R.drawable.ic_mode_edit_black_36dp);
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
					addPicture(bitmap, null);
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
			if (Build.VERSION.SDK_INT >= 19) {
				try {
					ParcelFileDescriptor parcelFileDescriptor =
							getContentResolver().openFileDescriptor(data.getData(), "r");
					FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
					Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
					parcelFileDescriptor.close();
					addPicture(image, null);
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
				addPicture(null, filePath);
			}
		} else if (requestCode == REQUEST_CAPTURE_PHOTO && resultCode == RESULT_OK) {
			addPicture(null, Utility.lastPicPath);
		} else if (resultCode == MultiPicturePicker.PICK_OK) {
			ArrayList<String> paths = data.getStringArrayListExtra("img");

			for (String path : paths) {
				addPicture(null, path);

				if (mBitmaps.size() >= 9) {
					break;
				}
			}
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


	@Binded
	public void send() {
		try {

			if (!TextUtils.isEmpty(mText.getText().toString().trim())) {
				new Uploader().execute();
			} else {
				if (mBitmaps.size() != 0 & !mIsLong){
					mText.setText(R.string.post_photo);
					new Uploader().execute();
				} else {
					Toast.makeText(this, R.string.empty_weibo, Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {

		}
	}

	@Binded
	public void pic() {
		if (mBitmaps.size() < 9) {
			showPicturePicker();
		}
	}

	@Binded
	public void emoji() {
		if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
			mDrawer.closeDrawer(Gravity.RIGHT);
		} else {
			mDrawer.openDrawer(Gravity.RIGHT);
		}
	}

	@Binded
	public void avatar(){
		mHints = getResources().getStringArray(R.array.splashes);
		mText.setHint(mHints[new Random().nextInt(mHints.length)]);
	}

	@Binded
	public void at() {
		mText.getText().insert(mText.getSelectionStart(), "@");
	}

	@Binded
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
								if (Build.VERSION.SDK_INT >= 19) {
									i.setAction(Intent.ACTION_OPEN_DOCUMENT);
									i.addCategory(Intent.CATEGORY_OPENABLE);
									i.setType("image/*");
								} else {
									i.setAction(Intent.ACTION_PICK);
									i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
								}
								startActivityForResult(i, REQUEST_PICK_IMG);
								break;
							case 1:
								Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								Uri uri = Utility.getOutputMediaFileUri();
								captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
								startActivityForResult(captureIntent, REQUEST_CAPTURE_PHOTO);
								break;
							case 2:
								Intent multi = new Intent();
								multi.setAction("us.shandian.blacklight.MULTI_PICK");
								multi.setClass(NewPostActivity.this, MultiPicturePicker.class);
								startActivityForResult(multi, 1000);
								break;
						}
					}
				}
		).show();
	}

	private void addPicture(Bitmap bitmap, String path){
		if (bitmap == null) {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, o);
			o.inJustDecodeBounds = false;
			o.inSampleSize = Utility.computeSampleSize(o, -1, 128*128);
			try {
				bitmap = BitmapFactory.decodeFile(path, o);
			} catch (OutOfMemoryError e) {
				return;
			}
		}
		mBitmaps.add(bitmap);
		mPaths.add(path);
		updatePictureView();
	}

	private void removePicture(int id) {
		mBitmaps.remove(id);
		mPaths.remove(id);
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
				return PostApi.newPost(mText.getText().toString(), mVersion);
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
				String path = mPaths.get(0);

				if (path != null) {
					try {
						bmp = BitmapFactory.decodeFile(path);
					} catch (OutOfMemoryError e) {

					}
				}

				mBitmaps.remove(0);
				mPaths.remove(0);
			}

			bmp = Utility.parseLongPost(this, mText.getText().toString(), bmp);
			mBitmaps.add(0, bmp);
			mPaths.add(0, null);

			return postPics(Utility.parseLongContent(this, mText.getText().toString()));
		}
	}

	private boolean postPics(String status) {
		// Upload pictures first
		String pics = "";

		for (int i = 0; i < mBitmaps.size(); i++) {
			Bitmap bmp = mBitmaps.get(i);
			String path = mPaths.get(i);
			if (path != null) {
				try {
					bmp = BitmapFactory.decodeFile(path);
				} catch (OutOfMemoryError e) {
					continue;
				}
			}
			String id = PostApi.uploadPicture(bmp);
			if (id == null || id.trim().equals("")) return false;

			pics += id;

			if (i < mBitmaps.size() - 1) {
				pics += ",";
			}
		}

		// Upload text
		return PostApi.newPostWithMultiPics(status, pics, mVersion);
	}

	//for draft
	protected boolean needCache(){
		return true;
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
				if (needCache()) mCache.edit().putString(DRAFT,"").apply();
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
