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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.GalleryModel;
import us.shandian.blacklight.support.adapter.GalleryAdapter;
import static us.shandian.blacklight.BuildConfig.DEBUG;

public class MultiPicturePicker extends AbsActivity {
	private static final String TAG = MultiPicturePicker.class.getSimpleName();

	public static final int PICK_OK = 123456;

	@InjectView(R.id.picker_grid) GridView mGrid;

	private GalleryAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.img_picker);

		ButterKnife.inject(this);

		buildAdapter();

		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(mAdapter);
		mGrid.setFastScrollEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.pick_ok:
				ArrayList<String> res = mAdapter.getChecked();
				Intent i = new Intent();
				i.putStringArrayListExtra("img", res);
				setResult(PICK_OK, i);
				finish();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void buildAdapter() {
		ArrayList<GalleryModel> model = new ArrayList<GalleryModel>();

		try {
			String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED};
			String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";

			Cursor cursor = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					null, null, orderBy);

			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					GalleryModel m = new GalleryModel();
					m.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

					if (DEBUG) {
						Log.d(TAG, "m.path = " + m.path);
					}

					model.add(m);
				}
			}
		} catch (Exception e) {
		}

		mAdapter = new GalleryAdapter(this, model);
	}
}
