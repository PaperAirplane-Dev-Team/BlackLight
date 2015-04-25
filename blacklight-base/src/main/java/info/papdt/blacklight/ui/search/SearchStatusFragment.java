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

package info.papdt.blacklight.ui.search;

import android.content.Context;

import info.papdt.blacklight.api.search.SearchApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.ui.statuses.TimeLineFragment;

public class SearchStatusFragment extends TimeLineFragment implements SearchActivity.Searcher
{
	private String mSearch;
	
	@Override
	public void search(String q) {
		mSearch = q;
		
		try {
			onRefresh();
		} catch (NullPointerException e) {
			
		}
	}

	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new HackyApiCache(getActivity());
	}

	@Override
	protected void initTitle() {

	}
	
	private class HackyApiCache extends HomeTimeLineApiCache {
		public HackyApiCache(Context context) {
			super(context);
		}

		@Override
		public void loadFromCache() {
			mMessages = new MessageListModel();
		}

		@Override
		protected MessageListModel load() {
			return SearchApi.searchStatus(mSearch, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
		}

		@Override
		public void cache() {

		}
	}
}
