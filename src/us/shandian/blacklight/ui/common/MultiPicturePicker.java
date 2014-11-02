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

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.GalleryModel;
import us.shandian.blacklight.support.adapter.GalleryAdapter;

public class MultiPicturePicker extends AbsActivity {
	@InjectView(R.id.picker_grid) GridView mGrid;

	private GalleryAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.img_picker);

		ButterKnife.inject(this);

		buildAdapter();

		mGrid.setAdapter(mAdapter);
	}

	private void buildAdapter() {
		ArrayList<GalleryModel> model = new ArrayList<GalleryModel>();

		try {
			String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
			String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";

			Cursor cursor = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					null, null, orderBy);

			if (cursor != null && cursor.getCount() > 0) {
				while(cursor.moveToNext()) {
					GalleryModel m = new GalleryModel();
					m.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					model.add(m);
				}
			}
		} catch (Exception e) {
		}

		mAdapter = new GalleryAdapter(this, model);
	}
}
