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

package info.papdt.blacklight.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import info.papdt.blacklight.R;
import info.papdt.blacklight.support.Utility;

public class DonationFragment extends PreferenceFragment
{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.donation);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String sum = preference.getSummary().toString();
		
		if (sum.startsWith("http")) { // An referral link
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse(sum));
			startActivity(i);
		} else {
			Utility.copyToClipboard(getActivity(), sum);
			Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
		}
		
		return true;
	}
}
