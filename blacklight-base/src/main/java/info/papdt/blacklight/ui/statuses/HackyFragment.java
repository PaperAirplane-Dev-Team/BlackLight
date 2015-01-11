package info.papdt.blacklight.ui.statuses;/* 
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

import android.content.Context;
import android.view.ViewGroup;

import java.util.List;

import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.MessageModel;

public class HackyFragment extends TimeLineFragment {

	public HackyFragment() {
		mShowCommentStatus = false;
	}

	@Override
	protected void bindSwipeToRefresh(ViewGroup v) {

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
			mMessages = new MessageListModel();
			((List<MessageModel>) mMessages.getList()).add((MessageModel)getArguments()
					.getParcelable("msg"));
		}

		@Override
		public void loadFromCache() {

		}

		@Override
		public void load(boolean newWeibo) {

		}

		@Override
		public void cache() {

		}
	}
}
