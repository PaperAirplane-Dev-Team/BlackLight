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

package us.shandian.blacklight.ui.common;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.ShakeDetector;
import us.shandian.blacklight.support.ShakeDetector.ShakeListener;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

public class AbsActivity extends SwipeBackActivity implements ShakeListener {

	private ShakeDetector mDetector;
	private Settings mSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// On SmartBar devices, allow all Acvities to tint statusbar
		if (Utility.hasSmartBar() && !(this instanceof ImageActivity) &&
			!(this instanceof UserTimeLineActivity)) {
			Utility.enableTint(this);
		}
		
		// Common ActionBar settings
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);

		// Shake Detector
		mDetector = ShakeDetector.getInstance(this);

		// Settings
		mSettings = Settings.getInstance(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (mSettings.getBoolean(Settings.SHAKE_TO_RETURN, true)) {
			mDetector.addListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDetector.removeListener(this);
	}

	@Override
	public void onShake() {
		this.onBackPressed();
	}

}
