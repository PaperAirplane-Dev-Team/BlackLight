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

package info.papdt.blacklight.ui.statuses;

import android.view.ViewGroup;

import info.papdt.blacklight.api.remind.RemindApi;
import info.papdt.blacklight.api.remind.RemindApi.Type;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.cache.statuses.MentionsTimeLineApiCache;

public class MentionsTimeLineFragment extends TimeLineFragment
{

	public boolean mDoRefresh = false;

	@Override
	protected void bindSwipeToRefresh(ViewGroup vg) {
		super.bindSwipeToRefresh(vg);

		if (mDoRefresh) {
			onRefresh();
		}

		mDoRefresh = true;

	}

	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new MentionsTimeLineApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
	}

	@Override
	protected void load(boolean param) {
		super.load(param);

		if (param) {
			RemindApi.clearUnread(Type.Mention_Status.str);
		}
	}
}
