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
 
package us.shandian.blacklight.snapshot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;

import info.papdt.blacklight.support.Settings;

public class SettingsFragment extends info.papdt.blacklight.ui.settings.SettingsFragment
{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference("debug_submit_log").setEnabled(false);
		findPreference(Settings.AUTO_SUBMIT_LOG).setEnabled(false);
		findPreference("version").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				new AlertDialog.Builder(getActivity())
					.setCancelable(true)
					.setMessage(R.string.snapshot_alert)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface p1, int p2) {
							
						}
					})
					.show();
				return true;
			}
		});
	}
}
