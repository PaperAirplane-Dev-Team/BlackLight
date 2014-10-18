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

package us.shandian.blacklight.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;
import butterknife.OnItemSelected;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;
import static us.shandian.blacklight.support.Utility.hasSmartBar;

/* BlackMagic Login Activity */
public class LoginActivity extends AbsActivity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	@InjectView(R.id.tail) Spinner mTail;
	@InjectView(R.id.username) TextView mUsername;
	@InjectView(R.id.passwd) TextView mPasswd;
	
	private MenuItem mMenuItem;
	
	private String[] mTailNames;
	private String[] mKeys;
	
	private String mAppId;
	private String mAppSecret;
	
	private LoginApiCache mLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (hasSmartBar()) {
			getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// Inject
		ButterKnife.inject(this);
		
		// Create login instance
		mLogin = new LoginApiCache(this);
		
		// Get views
		mTailNames = getResources().getStringArray(R.array.bm_tails);
		mKeys = getResources().getStringArray(R.array.bm_keys);
		
		mTail.setAdapter(new ArrayAdapter(this, R.layout.spinner_item_text, mTailNames));
		
		changeTail(null, null, 0, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenuItem = menu.add(R.string.login);
		mMenuItem.setShowAsAction(1);
		return true;
	}

	@OnItemSelected(R.id.tail)
	public void changeTail(AdapterView<?> parent, View view, int position, long id) {
		String[] key = mKeys[position].split("\\|");
		mAppId = key[0];
		mAppSecret = key[1];
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == mMenuItem) {
			login();
			return true;
		} else if (item.getItemId() == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@OnEditorAction(R.id.passwd)
	public boolean finishEnteringPasswd(TextView textView, int actionId, KeyEvent keyEvent) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			login();
			return true;
		}
		return false;
	}

	private void login() {
		if (mUsername.getText().length() < 1) {
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_empty_username),
					Toast.LENGTH_SHORT
			).show();
			return;
		}
		if (mPasswd.getText().length() < 1) {
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.toast_empty_password),
					Toast.LENGTH_SHORT
			).show();
			return;
		}
		new LoginTask().execute(new String[]{
				mAppId,
				mAppSecret,
				mUsername.getText().toString(),
				mPasswd.getText().toString()
		});
	}

	private class LoginTask extends AsyncTask<String, Void, Void>
	{
		private ProgressDialog progDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDialog = new ProgressDialog(LoginActivity.this);
			progDialog.setMessage(getResources().getString(R.string.plz_wait));
			progDialog.setCancelable(false);
			progDialog.show();
		}
		
		@Override
		protected Void doInBackground(String[] params) {
			if (DEBUG) {
				Log.d(TAG, "doInBackground...");
			}
			mLogin.login(params[0], params[1], params[2], params[3]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progDialog.dismiss();
			
			if (mLogin.getAccessToken() != null) {
				if (DEBUG) {
					Log.d(TAG, "Access Token:" + mLogin.getAccessToken());
					Log.d(TAG, "Expires in:" + mLogin.getExpireDate());
				}
				mLogin.cache();
				BaseApi.setAccessToken(mLogin.getAccessToken());
				
				// Expire date
				String msg = String.format(getResources().getString(R.string.expires_in), Utility.expireTimeInDays(mLogin.getExpireDate()));
				new AlertDialog.Builder(LoginActivity.this)
								.setMessage(msg)
								.setCancelable(false)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
										Intent i = new Intent();
										i.setAction(Intent.ACTION_MAIN);
										i.setClass(LoginActivity.this, MainActivity.class);
										startActivity(i);
										finish();
									}
								})
								.create()
								.show();
			} else {
				// Wrong username or password
				new AlertDialog.Builder(LoginActivity.this)
								.setMessage(R.string.login_fail)
								.setCancelable(true)
								.create()
								.show();
			}
		}
		
	}
}
