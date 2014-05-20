package us.shandian.blacklight.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;

public class LicenseActivity extends SwipeBackActivity
{
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// This activity shares the same view with the login activity
		setContentView(R.layout.web_login);
		
		mWebView = (WebView) findViewById(R.id.login_web);
		
		// The license is in assets
		mWebView.loadUrl("file:///android_asset/licenses.html");
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
}
