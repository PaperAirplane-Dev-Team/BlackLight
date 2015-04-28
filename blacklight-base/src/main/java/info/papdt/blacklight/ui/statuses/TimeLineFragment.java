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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
 
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.support.adapter.WeiboAdapter;
import info.papdt.blacklight.support.adapter.HeaderViewAdapter;
 
public abstract class TimeLineFragment extends AbsTimeLineFragment<WeiboAdapter>
{
	protected HomeTimeLineApiCache mCache;

	protected boolean mBindOrig = true;
	protected boolean mShowCommentStatus = true;
	protected boolean mNotifyDataLoaded = false;
	
	@Override
	protected WeiboAdapter buildAdapter() {
		return new WeiboAdapter(getActivity(), mList, mCache.mMessages,
								mBindOrig, mShowCommentStatus);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = super.onCreateView(inflater, container, savedInstanceState);
		mNotifyDataLoaded = true;
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mNotifyDataLoaded) {
					mAdapter.notifyDataSetLoaded();
				}
				
				mNotifyDataLoaded = false;
			}
		});
		return v;
	}

	@Override
	protected void onCreateCache() {
		mCache = bindApiCache();
	}

	@Override
	protected void loadFromCache() {
		mCache.loadFromCache();
	}

	@Override
	protected int getCacheSize() {
		return mCache.mMessages.getSize();
	}

	@Override
	protected void cacheLoadNew(boolean param) {
		load(param);
	}

	@Override
	protected int getCurrentItemCount() {
		return mAdapter.getCount();
	}

	@Override
	protected void initTitle() {
		// Do nothing by default
	}

	@Override
	protected void onDataLoaded() {
		mNotifyDataLoaded = true;
	}
	
	protected void load(boolean param) {
		mCache.load(param);
		mCache.cache();
	}
	
	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}

 }
