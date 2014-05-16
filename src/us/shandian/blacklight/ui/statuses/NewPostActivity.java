package us.shandian.blacklight.ui.statuses;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.support.v4.widget.DrawerLayout;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.common.EmoticonFragment;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class NewPostActivity extends SwipeBackActivity
{
	private static final String TAG = NewPostActivity.class.getSimpleName();
	
	private static final int REQUEST_PICK_IMG = 1001;
	
	protected EditText mText;
	private ImageView mBackground;
	private TextView mCount;
	private DrawerLayout mDrawer;
	
	// Fragments
	private EmoticonFragment mEmoticonFragment;
	
	// Picked picture
	private Bitmap mBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_status);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Init
		mText = (EditText) findViewById(R.id.post_edit);
		mBackground = (ImageView) findViewById(R.id.post_back);
		mCount = (TextView) findViewById(R.id.post_count);
		mDrawer = (DrawerLayout) findViewById(R.id.post_drawer);
		
		// Fragments
		mEmoticonFragment = new EmoticonFragment();
		getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
		
		// Listeners
		mEmoticonFragment.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
				@Override
				public void onEmoticonSelected(String name) {
					mText.getText().insert(mText.getSelectionStart(), name);
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
					
					if (length > 140) {
						mCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
					} else {
						mCount.setTextColor(getResources().getColor(R.color.gray));
					}
					
					mCount.setText(String.valueOf(140 - length));
				} catch (Exception e) {
					
				}
			}
		});
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
			mBitmap = BitmapFactory.decodeFile(filePath);
			mBackground.setImageBitmap(mBitmap);
			mBackground.setVisibility(View.VISIBLE);
			mCount.setBackgroundColor(getResources().getColor(R.color.gray_alpha_lighter));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.new_post, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.isCheckable() && item.isEnabled()) {
			item.setChecked(!item.isChecked());
		}
		
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.post_send:
				try {
					if (Utility.lengthOfString(mText.getText().toString()) <= 140) {
						new Uploader().execute();
					}
				} catch (Exception e) {
					
				}
				return true;
			case R.id.post_pic:
				Intent i = new Intent();
				i.setAction(Intent.ACTION_PICK);
				i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, REQUEST_PICK_IMG);
				return true;
			case R.id.post_emiticon:
				if (mDrawer.isDrawerOpen(Gravity.END)) {
					mDrawer.closeDrawer(Gravity.END);
				} else {
					mDrawer.openDrawer(Gravity.END);
				}
				
				return true;
				
		}
		return super.onOptionsItemSelected(item);
	}
	
	// if extended, this should be overridden
	protected boolean post() {
		if (mBitmap == null) {
			return PostApi.newPost(mText.getText().toString());
		} else {
			return PostApi.newPostWithPic(mText.getText().toString(), mBitmap);
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
		protected Boolean doInBackground(Void[] params) {
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
}
