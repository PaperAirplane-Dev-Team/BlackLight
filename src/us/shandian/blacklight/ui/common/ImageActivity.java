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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;

import java.util.ArrayList;

import org.roisoleil.gifview.GifView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class ImageActivity extends AbsActivity implements OnPhotoTapListener
{
	private static final String TAG = ImageActivity.class.getSimpleName();
	
	private ImageAdapter mAdapter;
	private ViewPager mPager;
	private MessageModel mModel;
	private HomeTimeLineApiCache mApiCache;

	private TextView mPage;

	private boolean[] mLoaded;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ActionBar
		getActionBar().setTitle("");

		mApiCache = new HomeTimeLineApiCache(this);
		
		mModel = getIntent().getParcelableExtra("model");
		int def = getIntent().getIntExtra("defaultId", 0);
		
		setContentView(R.layout.image_activity);
		
		// Initialize the adapter
		mAdapter = new ImageAdapter();
		mLoaded = new boolean[mAdapter.getCount()];

		// Page indicator
		mPage = (TextView) findViewById(R.id.image_page);
		mPage.setText((def + 1) + " / " + mAdapter.getCount());

		mPager = (ViewPager) findViewById(R.id.image_pager);
		mPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				mPage.setText((page + 1) + " / " + mAdapter.getCount());
			}
		});
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(def);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.image, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.save) {
			int current = mPager.getCurrentItem();
			if (!mLoaded[current]) {
				Toast.makeText(this, R.string.not_loaded, Toast.LENGTH_SHORT).show();
			} else {
				String path = mApiCache.saveLargePic(mModel, current);
				if (path == null) {
					Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
				} else {
					String msg = String.format(getResources().getString(R.string.saved_to), path);
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPhotoTap(View v, float x, float y) {
		if (getActionBar().isShowing()) {
			getActionBar().hide();
		} else {
			getActionBar().show();
		}
	}

	private class ImageAdapter extends PagerAdapter {
		private ArrayList<View> mViews = new ArrayList<View>();
		
		public ImageAdapter() {
			for (int i = 0; i < getCount(); i++) {
				mViews.add(null);
			}
		}
		
		@Override
		public int getCount() {
			return mModel.hasMultiplePictures() ? mModel.pic_urls.size() : 1;
		}

		@Override
		public boolean isViewFromObject(View v, Object obj) {
			return v == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = mViews.get(position);
			if (v != null) {
				container.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				return v;
			} else {
				LinearLayout ll = new LinearLayout(ImageActivity.this);
				ll.setGravity(Gravity.CENTER);
				ll.addView(new ProgressBar(ImageActivity.this));
				container.addView(ll, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				mViews.set(position, ll);
				new DownloadTask().execute(new Object[]{ll, position});
				return ll;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
	}
	
	private class DownloadTask extends AsyncTask<Object, Void, Object[]> {

		@Override
		protected Object[] doInBackground(Object[] params) {
			int id = Integer.parseInt(params[1].toString());
			Object img = mApiCache.getLargePic(mModel, id);
			mLoaded[id] = true;
			return new Object[]{params[0], img};
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			ViewGroup v = (ViewGroup) result[0];
			Object img = result[1];
			
			if (img != null) {
				v.removeAllViews();
				if (img instanceof Bitmap) {
					PhotoView p = new PhotoView(ImageActivity.this);
					
					// Disable hardware acceleration if too large
					Bitmap image = (Bitmap) img;
					int maxSize = Utility.getSupportedMaxPictureSize();
					if (image.getWidth() > maxSize || image.getHeight() > maxSize) {
						p.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
						
						if (DEBUG) {
							Log.d(TAG, "Image too large, hardware acceleration disabled. max size: " + maxSize);
						}
					}
					
					p.setImageBitmap(image);
					p.setMaxScale(20.0f);
					p.setMaximumScale(20.0f);
					p.setOnPhotoTapListener(ImageActivity.this);
					v.addView(p, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				} else if (img instanceof Movie) {
					GifView g = new GifView(ImageActivity.this);
					g.setMovie((Movie) img);
					v.addView(g, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				}
			}
		}

	}
	
}
