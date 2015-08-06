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

package info.papdt.blacklight.support.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import info.papdt.blacklight.R;
import info.papdt.blacklight.model.GalleryModel;
import info.papdt.blacklight.support.Utility;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class GalleryAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
	private static final String TAG = GalleryAdapter.class.getSimpleName();

	private ArrayList<GalleryModel> mList = new ArrayList<>();
	private ArrayList<GalleryModel> mChecked = new ArrayList<>();

	private LayoutInflater mInflater;
	private boolean mScrolling = false;

	public GalleryAdapter(Context context, ArrayList<GalleryModel> list, AbsListView listView) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = list;

		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView v, int state) {
				mScrolling = (state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
			}

			@Override
			public void onScroll(AbsListView p1, int p2, int p3, int p4) {
			        // Nothing to do
			}
		});
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public GalleryModel getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			View v = convertView;
			ViewHolder h = null;
			if (v == null) {
				v = mInflater.inflate(R.layout.img_picker_item, null);
				h = new ViewHolder(v);
				v.setTag(h);
			} else {
				h = (ViewHolder) v.getTag();
			}

			GalleryModel gallery = mList.get(position);

			h.path = gallery.path;
			h.id = gallery.id;

			Picasso.with(v.getContext())
			.load(new File(gallery.path))
			.fit()
			.centerCrop()
			.into(h.img);

			if (gallery.checked) {
				h.check.setVisibility(View.VISIBLE);
				h.check.setChecked(true);
			} else {
				h.check.setChecked(false);
				h.check.setVisibility(View.GONE);
			}

			return v;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ViewHolder h = (ViewHolder) view.getTag();
		GalleryModel m = getItem(position);
		m.checked = !m.checked;

		if (m.checked) {
			h.check.setVisibility(View.VISIBLE);
			h.check.setChecked(true);
			mChecked.add(m);
		} else {
			h.check.setChecked(false);
			h.check.setVisibility(View.GONE);
			mChecked.remove(m);
		}

		if (DEBUG) {
			Log.d(TAG, "m.path = " + m.path);
		}
	}

	public ArrayList<String> getChecked() {
		ArrayList<String> ret = new ArrayList<String>();

		for (GalleryModel m : mChecked) {
			ret.add(m.path);
		}

		return ret;
	}

	private boolean waitUntilNotScrolling(ViewHolder h, String path) {
		while (mScrolling) {
			if (!h.path.equals(path))
				return false;

			try {
				Thread.sleep(200);
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	class ViewHolder {
		private View v;

		public ImageView img;
		public CheckBox check;
		public String path;
		public long id = -1;

		public ViewHolder(View v) {
			this.v = v;
			img = Utility.findViewById(v, R.id.img_picker_img);
			check = Utility.findViewById(v, R.id.img_picker_check);
		}
	}
}
