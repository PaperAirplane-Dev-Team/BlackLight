package us.shandian.blacklight.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.main.MainActivity;
import static  us.shandian.blacklight.BuildConfig.DEBUG;

/*
  Non-BlackMagic login activity
  Login by a webpage
*/
public class WebLoginActivity extends Activity
{
	private static final String TAG = WebLoginActivity.class.getSimpleName();
	
	private WebView mWebView;
	private LoginApiCache mLogin;
	
	private boolean mExecuting = false;
	private int mClickCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_login);
		
		// Action Bar
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Init webview
		mWebView = (WebView) findViewById(R.id.login_web);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setSaveFormData(false);
		settings.setSavePassword(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		
		// Clear cookie
		CookieManager.getInstance().removeAllCookie();
		
		// Load
		mWebView.loadUrl(Constants.LOGIN_URL);
		
		// Api
		mLogin = new LoginApiCache(this);
		
		// Client
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView v, String url, Bitmap icon) {
				if (url.startsWith(Constants.REDIRECT_URL)) {
					if (url.contains("error")) {
						v.loadUrl(Constants.LOGIN_URL);
					} else if (!mExecuting) {
						v.stopLoading();
						String code = url.substring(url.lastIndexOf("=") + 1, url.length());
						
						if (DEBUG) {
							Log.d(TAG, "url = " + url + " code = " + code);
						}
						
						new LoginTask().execute(new String[]{code});
					}
				}
				
				super.onPageStarted(v, url, icon);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView v, String url) {
				v.loadUrl(url);
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mClickCount < 30) {
				mClickCount++;
			} else {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(this, LoginActivity.class);
				startActivity(i);
				finish();
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class LoginTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDialog = new ProgressDialog(WebLoginActivity.this);
			progDialog.setMessage(getResources().getString(R.string.plz_wait));
			progDialog.setCancelable(false);
			progDialog.show();
			mExecuting = true;
		}

		@Override
		protected Void doInBackground(String[] params) {
			mLogin.webLogin(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progDialog.dismiss();
			mExecuting = false;

			if (mLogin.getAccessToken() != null) {
				mLogin.cache();
				BaseApi.setAccessToken(mLogin.getAccessToken());

				// Expire date
				String msg = String.format(getResources().getString(R.string.expires_in), Utility.expireTimeInDays(mLogin.getExpireDate()));
				new AlertDialog.Builder(WebLoginActivity.this)
					.setMessage(msg)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							Intent i = new Intent();
							i.setAction(Intent.ACTION_MAIN);
							i.setClass(WebLoginActivity.this, MainActivity.class);
							startActivity(i);
							finish();
						}
					})
					.create()
					.show();
			} else {
				// Wrong username or password
				mWebView.loadUrl(Constants.LOGIN_URL);
			}
		}

	}
	
}
