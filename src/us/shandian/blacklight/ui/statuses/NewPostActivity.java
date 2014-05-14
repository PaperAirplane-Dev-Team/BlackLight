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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.os.Bundle;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import java.io.ByteArrayOutputStream;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.statuses.PostApi;

public class NewPostActivity extends SwipeBackActivity
{
	private static final int REQUEST_PICK_IMG = 1001;
	
	private EditText mText;
	private ImageView mBackground;
	
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
			mText.setBackgroundColor(getResources().getColor(R.color.gray_alpha_lighter));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.new_post, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.post_send:
				new Uploader().execute();
				return true;
			case R.id.post_pic:
				Intent i = new Intent();
				i.setAction(Intent.ACTION_PICK);
				i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, REQUEST_PICK_IMG);
				return true;
				
		}
		return super.onOptionsItemSelected(item);
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
			if (mBitmap == null) {
				return PostApi.newPost(mText.getText().toString());
			} else {
				return PostApi.newPostWithPic(mText.getText().toString(), mBitmap);
			}
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
