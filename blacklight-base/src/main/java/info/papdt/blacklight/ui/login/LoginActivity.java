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

package info.papdt.blacklight.ui.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.PrivateKey;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.main.MainActivity;

import static info.papdt.blacklight.BuildConfig.DEBUG;
import static info.papdt.blacklight.support.Utility.hasSmartBar;

/* Login Activity */
public class LoginActivity extends AbsActivity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	private WebView mWeb;
	
	private LoginApiCache mLogin;
	private boolean mIsMulti = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.web_login;
		super.onCreate(savedInstanceState);
		
		mIsMulti = getIntent().getBooleanExtra("multi", false);

		// Initialize views
		mWeb = Utility.findViewById(this, R.id.login_web);
		
		// Create login instance
		mLogin = new LoginApiCache(this);
		
		// Login page
		WebSettings settings = mWeb.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setSaveFormData(false);
		settings.setSavePassword(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		mWeb.setWebViewClient(new MyWebViewClient());
		mWeb.loadUrl(PrivateKey.getOauthLoginPage());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void handleRedirectedUrl(String url) {
		if (!url.contains("error")) {
			int tokenIndex = url.indexOf("access_token=");
			int expiresIndex = url.indexOf("expires_in=");
			String token = url.substring(tokenIndex + 13, url.indexOf("&", tokenIndex));
			String expiresIn = url.substring(expiresIndex + 11, url.indexOf("&", expiresIndex));
			
			if (DEBUG) {
				Log.d(TAG, "url = " + url);
				Log.d(TAG, "token = " + token);
				Log.d(TAG, "expires_in = " + expiresIn);
			}
			
			new LoginTask().execute(token, expiresIn);
		} else {
			showLoginFail();
		}
	}
	
	private void showLoginFail() {
		// Wrong username or password
		new AlertDialog.Builder(LoginActivity.this)
								.setMessage(R.string.login_fail)
								.setCancelable(true)
								.create()
								.show();
	}
	
	private class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (PrivateKey.isUrlRedirected(url)) {
				view.stopLoading();
				handleRedirectedUrl(url);
			} else {
				view.loadUrl(url);
			}
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (PrivateKey.isUrlRedirected(url)) {
				view.stopLoading();
				handleRedirectedUrl(url);
				return;
			}
			super.onPageStarted(view, url, favicon);
		}
	}

	private class LoginTask extends AsyncTask<String, Void, Long>
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
		protected Long doInBackground(String... params) {
			if (DEBUG) {
				Log.d(TAG, "doInBackground...");
			}
			
			if (!mIsMulti) {
				mLogin.login(params[0], params[1]);
				return mLogin.getExpireDate();
			} else {
				return mLogin.addUser(params[0], params[1]);
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			progDialog.dismiss();
			
			if (!mIsMulti && mLogin.getAccessToken() != null) {
				if (DEBUG) {
					Log.d(TAG, "Access Token:" + mLogin.getAccessToken());
					Log.d(TAG, "Expires in:" + mLogin.getExpireDate());
				}
				mLogin.cache();
				BaseApi.setAccessToken(mLogin.getAccessToken());
			} else if (!mIsMulti && mLogin.getAccessToken() == null) {
				showLoginFail();
				return;
			}
			

			// Expire date
			String msg = String.format(getResources().getString(R.string.expires_in), Utility.expireTimeInDays(result));
			new AlertDialog.Builder(LoginActivity.this)
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						if (!mIsMulti) {
							Intent i = new Intent();
							i.setAction(Intent.ACTION_MAIN);
							i.setClass(LoginActivity.this, MainActivity.class);
							startActivity(i);
							finish();
						} else {
							setResult(RESULT_OK);
							finish();
						}
					}
				})
				.create()
				.show();
		}
		
	}
}
