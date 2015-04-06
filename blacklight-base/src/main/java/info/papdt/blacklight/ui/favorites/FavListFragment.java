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

package info.papdt.blacklight.ui.favorites;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.favorites.FavListApiCache;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.ui.statuses.TimeLineFragment;

public class FavListFragment extends TimeLineFragment
{
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new FavListApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
		
	}
}
