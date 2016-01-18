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

package info.papdt.blacklight.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.search.TopicsApi;
import info.papdt.blacklight.cache.Constants;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.statuses.TimeLineFragment;

/*
  Shows the topics
*/
public class TopicsActivity extends AbsActivity
{
	private String mTopic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.empty_frame;

		super.onCreate(savedInstanceState);
		
		// Argument
		mTopic = getIntent().getStringExtra("topic");
		
		// Fragment
		getFragmentManager().beginTransaction().replace(R.id.frame, new HackyFragment()).commit();
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
	
	private class HackyApiCache extends HomeTimeLineApiCache {
		public HackyApiCache(Context context) {
			super(context);
		}

		@Override
		public void loadFromCache() {
			// We don't need to cache
			mMessages = new MessageListModel();
		}

		@Override
		protected MessageListModel load() {
			return TopicsApi.searchTopic(mTopic, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
		}

		@Override
		public void cache() {
			// We don't need to cache
		}
	}
	
	private class HackyFragment extends TimeLineFragment {
		@Override
		protected HomeTimeLineApiCache bindApiCache() {
			return new HackyApiCache(getActivity());
		}

		@Override
		protected void initTitle() {

		}
	}
}
