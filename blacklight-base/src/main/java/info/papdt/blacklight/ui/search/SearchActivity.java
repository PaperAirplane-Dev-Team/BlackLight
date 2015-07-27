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

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;

import info.papdt.blacklight.R;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.SlidingTabLayout;
import info.papdt.blacklight.ui.common.SlidingTabStrip;

public class SearchActivity extends AbsActivity
{
	public static interface Searcher {
		public void search(String q);
	}

	private ViewPager mPager;
	private SlidingTabLayout mTab;

	private Fragment[] mFragments = new Fragment[]{
		new SearchStatusFragment(),
		new SearchUserFragment()
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.search;
		super.onCreate(savedInstanceState);

		mPager = Utility.findViewById(this, R.id.search_pager);
		mTab = Utility.findViewById(this, R.id.search_tab);

		if (Build.VERSION.SDK_INT >= 21) {
			mTab.setElevation(getToolbarElevation());
		}

		final String[] titles = getResources().getStringArray(R.array.search_type);
		mPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				return mFragments[position];
			}

			@Override
			public String getPageTitle(int position) {
				return titles[position];
			}

			@Override
			public int getCount() {
				return mFragments.length;
			}
		});
		mTab.setDistributeEvenly(true);
		mTab.setViewPager(mPager);

		final int color = getResources().getColor(R.color.white);
		mTab.setCustomTabColorizer(new SlidingTabStrip.SimpleTabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return color;
			}

			@Override
			public int getSelectedTitleColor(int position) {
				return color;
			}
		});

		mTab.notifyIndicatorColorChanged();

		mPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Intent i = getIntent();
				final String keyword = i.getStringExtra("keyword");

				mPager.postDelayed(new Runnable() {
					@Override
					public void run() {
						for (Fragment f : mFragments) {
							if (f instanceof Searcher) {
								((Searcher) f).search(keyword);
							}
						}
					}
				}, 500);

				mPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}
}
