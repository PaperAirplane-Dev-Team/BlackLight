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

package us.shandian.blacklight.ui.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.widget.SlidingPaneLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.ShakeDetector;
import us.shandian.blacklight.support.ShakeDetector.ShakeListener;
import us.shandian.blacklight.support.Utility;

public class AbsActivity extends ToolbarActivity implements ShakeListener {

	private ShakeDetector mDetector;
	private Settings mSettings;
	private int mLang = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Language
		mLang = Utility.getCurrentLanguage(this);
		if (mLang > -1) {
			Utility.changeLanguage(this, mLang);
		}
		
		Utility.initDarkMode(this);

		super.onCreate(savedInstanceState);
		swipeInit();
		
		// Common ActionBar settings
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		// Shake Detector
		mDetector = ShakeDetector.getInstance(this);

		// Settings
		mSettings = Settings.getInstance(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		int lang = Utility.getCurrentLanguage(this);
		if (lang != mLang) {
			recreate();
		}

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (mSettings.getBoolean(Settings.SHAKE_TO_RETURN, true)) {
			mDetector.addListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDetector.removeListener(this);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onShake() {
		this.onBackPressed();
	}
	
	protected View getSwipeView() {
		return ((ViewGroup) getWindow().getDecorView()).getChildAt(0);
	}
	
	@TargetApi(21)
	private void swipeInit() {
		// Replace the view first
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SlidingPaneLayout v = (SlidingPaneLayout) inflater.inflate(R.layout.swipe_decor_wrapper, null);
		final ViewGroup frame = (ViewGroup) v.findViewById(R.id.swipe_container);
		View swipeView = getSwipeView();
		ViewGroup decor = (ViewGroup) swipeView.getParent();
		ViewGroup.LayoutParams params = swipeView.getLayoutParams();
		decor.removeView(swipeView);
		frame.addView(swipeView);
		decor.addView(v, params);
		decor.setBackgroundColor(0);
		
		// Elevation
		if (Build.VERSION.SDK_INT >= 21) {
			frame.setElevation(11.8f);
		} else {
			// TODO Shadow drawable
		}
		
		// Swipe gesture configurations
		v.setSliderFadeColor(0);
		v.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {

			@Override
			public void onPanelSlide(View v, float percent) {
				getWindow().getDecorView().setAlpha(1.0f - percent);
			}

			@Override
			public void onPanelOpened(View p1) {
				onBackPressed();
			}

			@Override
			public void onPanelClosed(View p1) {
			}
		});
		
		// Adjust window color
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
	}
}
