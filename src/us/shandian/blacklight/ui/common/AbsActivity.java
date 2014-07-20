package us.shandian.blacklight.ui.common;

import android.app.Activity;
import android.os.Bundle;

import us.shandian.blacklight.support.Utility;

public class AbsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// On SmartBar devices, allow all Acvities to tint statusbar
		if (Utility.hasSmartBar()) {
			Utility.enableTint(this);
		}
		
		// Common ActionBar settings
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
	}

}
