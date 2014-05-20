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

package us.shandian.blacklight.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;

import me.imid.swipebacklayout.lib.app.SwipeBackPreferenceActivity;

import us.shandian.blacklight.R;

public class SettingsActivity extends SwipeBackPreferenceActivity implements Preference.OnPreferenceClickListener
{
	private static final String LICENSE = "license";
	
	private Preference mPrefLicense;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Init
		mPrefLicense = findPreference(LICENSE);
		
		// Set
		mPrefLicense.setOnPreferenceClickListener(this);
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
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mPrefLicense){
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, LicenseActivity.class);
			startActivity(i);
			return true;
		} else {
			return false;
		}
	}
}
