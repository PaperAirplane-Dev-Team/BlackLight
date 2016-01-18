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

package info.papdt.blacklight.ui.common;

import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.file.FileCacheManager;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.PermissionUtility;
import info.papdt.blacklight.support.Utility;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public abstract class AbsImageActivity<C> extends AbsActivity /*implements OnPhotoTapListener*/
{
	private static final String TAG = AbsImageActivity.class.getSimpleName();

	private ImageAdapter mAdapter;
	private ViewPager mPager;
	private C mApiCache;

	private TextView mPage;

	protected boolean[] mLoaded;

	protected abstract C buildApiCache();
	protected abstract String saveLargePic(int current);
	protected abstract Object[] doDownload(Object[] params);
	protected abstract int getCount();

	protected C getApiCache() {
		return mApiCache;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.image_activity;
		super.onCreate(savedInstanceState);

		// ActionBar
		mToolbar.bringToFront();
		mToolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_gradient));
		getSupportActionBar().setTitle("");

		findViewById(R.id.image_container).setBackgroundResource(R.color.black);

		mApiCache = buildApiCache();

		int def = getIntent().getIntExtra("defaultId", 0);

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
		ViewCompat.setTransitionName(mPager, "model");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.save || id == R.id.share) {
			final int current = mPager.getCurrentItem();
			if (!mLoaded[current]) {
				Toast.makeText(this, R.string.not_loaded, Toast.LENGTH_SHORT).show();
			} else {
				PermissionUtility.storage(this, new Runnable() {
					@Override
					public void run() {
						String path = saveLargePic(current);
						if (id == R.id.save) {
							if (path == null) {
								Toast.makeText(AbsImageActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
							} else {
								String msg = String.format(getResources().getString(R.string.saved_to), path);
								Toast.makeText(AbsImageActivity.this, msg, Toast.LENGTH_SHORT).show();
							}
						}
						if (id == R.id.share) {
							if (path != null) {
								File f = new File(path);
								Uri u = Uri.fromFile(f);

								ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
								Intent i = new Intent();
								i.setAction(Intent.ACTION_SEND);
								i.putExtra(Intent.EXTRA_STREAM,u);
								i.setType("image/*");
								share.setShareIntent(i);
							}
						}
					}
				});
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	protected class MyCallback implements FileCacheManager.ProgressCallback {
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
					p.setText(String.format("%.1f/%.1fM", (float) read / 1024 / 1024, (float) total / 1024 / 1024));
				}
			});
		}

		@Override
		public boolean shouldContinue() {
			if (Build.VERSION.SDK_INT >= 17)
				return !isFinishing() || !isDestroyed();
			else
				return !isFinishing();
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
			return AbsImageActivity.this.getCount();
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
				LinearLayout ll = new LinearLayout(AbsImageActivity.this);
				ll.setGravity(Gravity.CENTER);
				CircularProgressView p = new CircularProgressView(AbsImageActivity.this);
				int w = (int) Utility.dp2px(AbsImageActivity.this, 50);
				ll.addView(p, w, w);
				container.addView(ll, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				mViews.set(position, ll);
				new DownloadTask().execute(new Object[] {ll, position, new MyCallback(p)});
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
			return doDownload(params);
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			final ViewGroup v = (ViewGroup) result[0];
			String img = String.valueOf(result[1]);

			if (img != null) {
				v.removeAllViews();
				if (!img.endsWith(".gif")) {
					// If returned a String, it means that the image is a Bitmap
					// So we can use the included SubsamplingScaleImageView
					final SubsamplingScaleImageView iv = new SubsamplingScaleImageView(AbsImageActivity.this);
					iv.setImage(ImageSource.uri(img));
					iv.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (getSupportActionBar().isShowing()) {
								getSupportActionBar().hide();
							} else {
								getSupportActionBar().show();
							}
						}
					});
					iv.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
						@Override
						public void onReady() {

						}

						@Override
						public void onImageLoaded() {

						}

						@Override
						public void onPreviewLoadError(Exception e) {

						}

						@Override
						public void onImageLoadError(Exception e) {
							Log.d(TAG, "Image load failed");
							iv.setImage(ImageSource.resource(android.R.drawable.ic_menu_report_image));
						}

						@Override
						public void onTileLoadError(Exception e) {

						}
					});

					if (DEBUG) {
						Log.d(TAG, img);
					}
					iv.setMaxScale(3.0f);

					v.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

					final Runnable r = new Runnable() {
						@Override
						public void run() {
							float height = iv.getHeight();
							float sHeight = iv.getSHeight();
							float width = iv.getWidth();
							float sWidth = iv.getSWidth();

							if (height == 0) {
								v.postDelayed(this,500);
								return;
							}

							float hwRate = height / width;
							float shwRate = sHeight / sWidth;

							if ((sHeight > height) && shwRate > hwRate) { // Long image.
								PointF center = new PointF(sWidth / 2, height / 2);
								float scale = 0.0f;
								if (sWidth * 2 > width) {
									scale = width / sWidth; // Zoom in properly.
								}
								iv.setScaleAndCenter(scale, center);
							}
						}
					};
					v.postDelayed(r, 500);

				} else {
					try {
						GifDrawable g = new GifDrawable(img);
						GifImageView iv = new GifImageView(AbsImageActivity.this);
						iv.setImageDrawable(g);
						v.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					} catch (IOException e) {

					}
				}
			}
		}

	}

}
