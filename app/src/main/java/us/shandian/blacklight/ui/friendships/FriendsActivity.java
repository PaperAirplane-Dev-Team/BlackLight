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

package us.shandian.blacklight.ui.friendships;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import us.shandian.blacklight.R;
import us.shandian.blacklight.ui.common.AbsActivity;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class FriendsActivity extends AbsActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        if (hasSmartBar()) {
            getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        }

		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_frame);
		
		// Arguments
		String uid = getIntent().getStringExtra("uid");
		
		getFragmentManager().beginTransaction().replace(R.id.frame, new FriendsFragment(uid)).commit();
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
