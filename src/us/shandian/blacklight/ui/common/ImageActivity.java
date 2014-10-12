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

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.file.FileCacheManager;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class ImageActivity extends AbsActivity /*implements OnPhotoTapListener*/
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
		mPager.setOffscreenPageLimit(1);
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

	private class MyCallback implements FileCacheManager.ProgressCallback {
		private CircularProgressView p;

		public MyCallback(CircularProgressView p) {
			this.p = p;
		}

		@Override
		public void onProgressChanged(final int read, final int total) {
			if (DEBUG) {
				Log.d(TAG, "read = " + read + "; total = " + total);
			}

			p.post(new Runnable() {
				@Override
				public void run() {
					p.setProgress((float) read / total);
				}
			});
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
				CircularProgressView p = new CircularProgressView(ImageActivity.this);
				int w = (int) Utility.dp2px(ImageActivity.this, 50);
				ll.addView(p, w, w);
				container.addView(ll, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				mViews.set(position, ll);
				new DownloadTask().execute(new Object[]{ll, position, new MyCallback(p)});
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
			Object img = mApiCache.getLargePic(mModel, id, (MyCallback) params[2]);
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
				if (img instanceof String) {
					// If returned a String, it means that the image is a Bitmap
					// So we can use the included SubsamplingScaleImageView
					SubsamplingScaleImageView iv = new SubsamplingScaleImageView(ImageActivity.this);
					iv.setImageFile((String) img);
					iv.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (getActionBar().isShowing()) {
								getActionBar().hide();
							} else {
								getActionBar().show();
							}
						}
					});

					if (DEBUG) {
						Log.d(TAG, img.toString());
					}
					iv.setMaxScale(3.0f);

					v.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				} else if (img instanceof Movie) {
					GifView g = new GifView(ImageActivity.this);
					g.setMovie((Movie) img);
					v.addView(g, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				}
			}
		}

	}
	
}
