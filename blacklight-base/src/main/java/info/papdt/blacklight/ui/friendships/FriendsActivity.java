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

package info.papdt.blacklight.ui.friendships;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import info.papdt.blacklight.R;
import info.papdt.blacklight.ui.common.AbsActivity;

import static info.papdt.blacklight.support.Utility.hasSmartBar;

public class FriendsActivity extends AbsActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.empty_frame;
		super.onCreate(savedInstanceState);

		// Arguments
		String uid = getIntent().getStringExtra("uid");
        boolean isFriends = getIntent().getBooleanExtra("isFriends", true);
		if(!isFriends) getSupportActionBar().setTitle(R.string.followers);
		getFragmentManager().beginTransaction().replace(R.id.frame, new FriendsFragment(uid, isFriends)).commit();
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
