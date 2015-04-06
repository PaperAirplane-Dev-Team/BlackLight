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

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.papdt.blacklight.R;
import info.papdt.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import info.papdt.blacklight.ui.common.SlidingTabLayout;
import info.papdt.blacklight.ui.common.SlidingTabStrip.SimpleTabColorizer;
import info.papdt.blacklight.ui.main.MainActivity;
import info.papdt.blacklight.support.Utility;

/*
 * This class combines MentionsTimeLine and CommentMentionsTimeLine together
 * */
public class MentionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MainActivity.Refresher {
	private SlidingTabLayout mIndicator;
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
		mIndicator.setCustomTabColorizer(new SimpleTabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.white);
			}
			
			@Override
			public int getSelectedTitleColor(int position) {
				return getResources().getColor(R.color.white);
			}
		});
		mIndicator.setDistributeEvenly(true);
		
		if (Build.VERSION.SDK_INT >= 21) {
			mIndicator.setElevation(((MainActivity) getActivity()).getToolbarElevation());
		}
		
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
			
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
					case 0:
						return getString(R.string.retweet);
					case 1:
						return getString(R.string.comment);
					default:
						return "";
				}
			}
		});
		
		mIndicator.setViewPager(mPager);

		return v;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (!hidden) {
			((MainActivity) getActivity()).getToolbar().setTranslationY(0);
			((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.drawer_at));
			mIndicator.notifyIndicatorColorChanged();
			
			if (Build.VERSION.SDK_INT >= 21) {
				((MainActivity) getActivity()).getToolbar().setElevation(0f);
			}
		} else if (Build.VERSION.SDK_INT >= 21) {
			MainActivity activity = (MainActivity) getActivity();
			
			if (activity != null && activity.getToolbar() != null)
				activity.getToolbar().setElevation(activity.getToolbarElevation());
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

	@Override
	public void goToTop() {
		mRetweet.goToTop();
		mComment.goToTop();
	}
}
