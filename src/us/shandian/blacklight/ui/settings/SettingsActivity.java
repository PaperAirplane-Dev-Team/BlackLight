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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.view.MenuItem;
import android.widget.Toast;

import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackPreferenceActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.support.CrashHandler;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class SettingsActivity extends SwipeBackPreferenceActivity implements
		Preference.OnPreferenceClickListener,
		Preference.OnPreferenceChangeListener {
	private static final String VERSION = "version";
	private static final String SOURCE_CODE = "source_code";
	private static final String LICENSE = "license";
	private static final String DEBUG_LOG = "debug_log";
	private static final String DEBUG_CRASH = "debug_crash";
	private static final String DEVELOPERS = "developers";
	private static final String GOOD = "good";

	private Settings mSettings;

	// About
	private Preference mPrefLicense;
	private Preference mPrefVersion;
	private Preference mPrefSourceCode;
	private Preference mPrefGood;
	private Preference mPrefLog;
	private Preference mPrefCrash;
	private Preference mPrefDevelopers;

	// Actions
	private CheckBoxPreference mPrefFastScroll;
	private CheckBoxPreference mPrefShakeToReturn;
	private CheckBoxPreference mPrefRightHanded;

	// Notification
	private CheckBoxPreference mPrefNotificationSound,
			mPrefNotificationVibrate;
	private Preference mPrefInterval;

	// Network
	private CheckBoxPreference mPrefAutoNoPic;

	// SwipeBack
	private boolean mIsFinishing = false;

	@SuppressWarnings("deprecation")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		mSettings = Settings.getInstance(this);

		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);

		// Init
		mPrefLicense = findPreference(LICENSE);
		mPrefVersion = findPreference(VERSION);
		mPrefSourceCode = findPreference(SOURCE_CODE);
		mPrefGood = findPreference(GOOD);
		mPrefFastScroll = (CheckBoxPreference) findPreference(Settings.FAST_SCROLL);
		mPrefShakeToReturn = (CheckBoxPreference) findPreference(Settings.SHAKE_TO_RETURN);
		mPrefRightHanded = (CheckBoxPreference) findPreference(Settings.RIGHT_HANDED);
		mPrefLog = findPreference(DEBUG_LOG);
		mPrefCrash = findPreference(DEBUG_CRASH);
		mPrefNotificationSound = (CheckBoxPreference) findPreference(Settings.NOTIFICATION_SOUND);
		mPrefNotificationVibrate = (CheckBoxPreference) findPreference(Settings.NOTIFICATION_VIBRATE);
		mPrefDevelopers = findPreference(DEVELOPERS);
		mPrefInterval = findPreference(Settings.NOTIFICATION_INTERVAL);
		mPrefAutoNoPic = (CheckBoxPreference) findPreference(Settings.AUTO_NOPIC);
		
		// Data
		String version = "Unknown";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
			// Keep the default value
		}
		mPrefVersion.setSummary(version);
		mPrefFastScroll.setChecked(mSettings.getBoolean(Settings.FAST_SCROLL,
				false));
		mPrefShakeToReturn.setChecked(mSettings.getBoolean(
				Settings.SHAKE_TO_RETURN, true));
		mPrefRightHanded.setChecked(mSettings.getBoolean(
				Settings.RIGHT_HANDED, false));
		mPrefNotificationSound.setChecked(mSettings.getBoolean(
				Settings.NOTIFICATION_SOUND, true));
		mPrefNotificationVibrate.setChecked(mSettings.getBoolean(
				Settings.NOTIFICATION_VIBRATE, true));
		mPrefLog.setSummary(CrashHandler.CRASH_LOG);
		mPrefInterval.setSummary(
				this.getResources()
				.getStringArray(R.array.interval_name) [mSettings.getInt(Settings.NOTIFICATION_INTERVAL, 1)]
						);
		mPrefAutoNoPic.setChecked(mSettings.getBoolean(Settings.AUTO_NOPIC, true));
		
		// Set
		mPrefLicense.setOnPreferenceClickListener(this);
		mPrefSourceCode.setOnPreferenceClickListener(this);
		mPrefGood.setOnPreferenceClickListener(this);
		mPrefFastScroll.setOnPreferenceChangeListener(this);
		mPrefShakeToReturn.setOnPreferenceChangeListener(this);
		mPrefRightHanded.setOnPreferenceChangeListener(this);
		mPrefNotificationSound.setOnPreferenceChangeListener(this);
		mPrefNotificationVibrate.setOnPreferenceChangeListener(this);
		mPrefCrash.setOnPreferenceClickListener(this);
		mPrefDevelopers.setOnPreferenceClickListener(this);
		mPrefInterval.setOnPreferenceClickListener(this);
		mPrefAutoNoPic.setOnPreferenceChangeListener(this);
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
		if (preference == mPrefLicense) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, LicenseActivity.class);
			startActivity(i);
			return true;
		} else if (preference == mPrefSourceCode) {
			// Visit source code
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse(mPrefSourceCode.getSummary().toString()));
			startActivity(i);
			return true;
		} else if (preference == mPrefCrash) {
			throw new RuntimeException("Debug crash");
		} else if (preference == mPrefDevelopers) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, DevelopersActivity.class);
			startActivity(i);
			return true;
		} else if (preference == mPrefInterval) {
			showIntervalSetDialog();
			return true;
		} else if (preference == mPrefGood) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse(getResources().getString(R.string.play_url)));
			startActivity(i);
			return true;
		}

		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mPrefFastScroll) {
			mSettings.putBoolean(Settings.FAST_SCROLL,
					Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (preference == mPrefNotificationSound) {
			mSettings.putBoolean(Settings.NOTIFICATION_SOUND,
					Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (preference == mPrefNotificationVibrate) {
			mSettings.putBoolean(Settings.NOTIFICATION_VIBRATE,
					Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (preference == mPrefRightHanded) {
			mSettings.putBoolean(Settings.RIGHT_HANDED,
					Boolean.parseBoolean(newValue.toString()));
			Toast.makeText(this, R.string.needs_restart, Toast.LENGTH_SHORT).show();
			return true;
		} else if (preference == mPrefShakeToReturn) {
			mSettings.putBoolean(Settings.SHAKE_TO_RETURN,
					Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (preference == mPrefAutoNoPic) {
			mSettings.putBoolean(Settings.AUTO_NOPIC,
					Boolean.parseBoolean(newValue.toString()));
			Toast.makeText(this, R.string.needs_restart, Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;
	}

	@Override
	public void finish() {
		if (!mIsFinishing) {
			Utils.convertActivityToTranslucent(this);
			scrollToFinishActivity();
			mIsFinishing = true;
		} else {
			super.finish();
		}
	}
	
	private void showIntervalSetDialog(){
		new AlertDialog.Builder(this)
			.setTitle(getString(R.string.set_interval))
			.setSingleChoiceItems(
					getResources().getStringArray(R.array.interval_name),
					mSettings.getInt(Settings.NOTIFICATION_INTERVAL, 1),
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSettings.putInt(Settings.NOTIFICATION_INTERVAL, which);
							mPrefInterval.setSummary(
									getResources()
									.getStringArray(R.array.interval_name) [
									mSettings.getInt(Settings.NOTIFICATION_INTERVAL, 1)
									]
											);
							Utility.restartServices(SettingsActivity.this);
							dialog.dismiss();
						}
					})
			.show();
		
	}
	
}
