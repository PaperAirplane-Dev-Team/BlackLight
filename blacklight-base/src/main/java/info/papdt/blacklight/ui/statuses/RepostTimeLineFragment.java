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

package info.papdt.blacklight.ui.statuses;

import android.os.Bundle;

import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.cache.statuses.RepostTimeLineApiCache;

public class RepostTimeLineFragment extends TimeLineFragment
{
	private long mId;
	
	public RepostTimeLineFragment() {
		init();
	}
	
	public RepostTimeLineFragment(long id) {
		Bundle args = new Bundle();
		args.putLong("id", id);
		setArguments(args);
		init();
	}
	
	private void init() {
		mBindOrig = false;
		mId = getArguments().getLong("id");
	}
	
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new RepostTimeLineApiCache(getActivity(), mId);
	}
	
	@Override
	protected void newPost() {
		((SingleActivity) getActivity()).repost();
	}
}
