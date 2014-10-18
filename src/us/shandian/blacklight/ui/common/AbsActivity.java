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
import android.view.View;
import android.view.ViewGroup;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import us.shandian.blacklight.R;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.ShakeDetector;
import us.shandian.blacklight.support.ShakeDetector.ShakeListener;
import us.shandian.blacklight.support.Utility;

public class AbsActivity extends SwipeBackActivity implements ShakeListener {

	private ShakeDetector mDetector;
	private Settings mSettings;
	private boolean mIsFinishing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utility.initDarkMode(this);

		super.onCreate(savedInstanceState);

		// Common ActionBar settings
		getActionBar().setCustomView(R.layout.action_custom_up);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setHomeButtonEnabled(false);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);

		// Custom
		ViewGroup custom = (ViewGroup) getActionBar().getCustomView();
		Utility.addActionViewToCustom(this, Utility.action_bar_title, custom);
		custom.findViewById(R.id.action_up).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		getActionBar().setDisplayShowTitleEnabled(false);

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

	@Override
	public void finish() {
		if (!mIsFinishing) {
			scrollToFinishActivity();
			mIsFinishing = true;
		} else {
			super.finish();
		}
	}

}
