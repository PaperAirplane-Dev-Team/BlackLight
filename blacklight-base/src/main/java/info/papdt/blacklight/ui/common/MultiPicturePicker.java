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
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import info.papdt.blacklight.R;
import info.papdt.blacklight.model.GalleryModel;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.adapter.GalleryAdapter;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class MultiPicturePicker extends AbsActivity {
	private static final String TAG = MultiPicturePicker.class.getSimpleName();

	public static final int PICK_OK = 123456;

	private GridView mGrid;

	private GalleryAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.img_picker;
		super.onCreate(savedInstanceState);

		// Views
		mGrid = Utility.findViewById(this, R.id.picker_grid);

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
		if (item.getItemId() == R.id.pick_ok) {
			ArrayList<String> res = mAdapter.getChecked();
			Intent i = new Intent();
			i.putStringArrayListExtra("img", res);
			setResult(PICK_OK, i);
			finish();
			return true;
		} else {
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
					m.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));

					if (DEBUG) {
						Log.d(TAG, "m.path = " + m.path);
					}

					model.add(m);
				}
			}
		} catch (Exception e) {
		}

		mAdapter = new GalleryAdapter(this, model, mGrid);
	}
}
