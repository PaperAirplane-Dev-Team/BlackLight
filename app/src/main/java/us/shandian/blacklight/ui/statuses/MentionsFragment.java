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

package us.shandian.blacklight.ui.statuses;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import us.shandian.blacklight.R;
import us.shandian.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import us.shandian.blacklight.ui.common.LinearViewPagerIndicator;
import us.shandian.blacklight.ui.common.SwipeRefreshLayout;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.Utility;

/*
 * This class combines MentionsTimeLine and CommentMentionsTimeLine together
 * */
public class MentionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MainActivity.Refresher {
	private LinearViewPagerIndicator mIndicator;
	private ViewPager mPager;

	private MentionsTimeLineFragment mRetweet;
	private CommentMentionsTimeLineFragment mComment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mentions, null);
		
		// Initialize views
		mIndicator = Utility.findViewById(v, R.id.mentions_indicator);
		mPager = Utility.findViewById(v, R.id.mentions_pager);

		// Initialize indicator
		mIndicator.setViewPager(mPager);
		mIndicator.addTab(getString(R.string.retweet));
		mIndicator.addTab(getString(R.string.comment));
		mIndicator.setForeground(getResources().getColor(R.color.white));

		// View Pager
		mRetweet = new MentionsTimeLineFragment();
		mComment = new CommentMentionsTimeLineFragment();

		mPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(int position) {
				switch (position) {
					case 0:
						return mRetweet;
					case 1:
						return mComment;
					default:
						return null;
				}
			}
		});

		return v;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (!hidden) {
			getActivity().getActionBar().setTitle(getString(R.string.drawer_at));
		}
	}

	@Override
	public void onRefresh() {
		// Will be called by MainActivity
		mRetweet.mDoRefresh = true;
		mComment.mDoRefresh = true;
	}

	@Override
	public void doRefresh() {
		mRetweet.doRefresh();
		mComment.doRefresh();
	}
}
