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

package info.papdt.blacklight.support.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import info.papdt.blacklight.R;
import info.papdt.blacklight.model.GalleryModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import static info.papdt.blacklight.BuildConfig.DEBUG;

public class GalleryAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
	private static final String TAG = GalleryAdapter.class.getSimpleName();

	private ArrayList<GalleryModel> mList = new ArrayList<GalleryModel>();
	private HashMap<String,  WeakReference<Bitmap>> mBitmaps = new HashMap<String, WeakReference<Bitmap>>();

	private LayoutInflater mInflater;

	public GalleryAdapter(Context context, ArrayList<GalleryModel> list) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = list;
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

			WeakReference<Bitmap> w = mBitmaps.get(gallery.path);
			Bitmap bmp = w != null ? w.get() : null;
			
			if (bmp == null) {
				h.img.setImageBitmap(null);
				new LoadTask().execute(h, gallery.path);
			} else {
				h.img.setImageBitmap(bmp);
			}
			
			if (gallery.checked) {
				h.check.setChecked(true);
				h.check.setVisibility(View.VISIBLE);
			} else {
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
			h.check.setChecked(true);
			h.check.setVisibility(View.VISIBLE);
		} else {
			h.check.setVisibility(View.GONE);
		}

		if (DEBUG) {
			Log.d(TAG, "m.path = " + m.path);
		}
	}

	public ArrayList<String> getChecked() {
		ArrayList<String> ret = new ArrayList<String>();
		
		for (GalleryModel m : mList) {
			if (m.checked) {
				ret.add(m.path);
			}
		}

		return ret;
	}

	class ViewHolder {
		private View v;

		public ImageView img;
		public CheckBox check;
		public String path;

		public ViewHolder(View v) {
			this.v = v;
			img = Utility.findViewById(v, R.id.img_picker_img);
			check = Utility.findViewById(v, R.id.img_picker_check);
		}
	}

	class LoadTask extends AsyncTask<Object, Void, Bitmap> {
		String path = "";
		ViewHolder h = null;

		@Override
		protected Bitmap doInBackground(Object... params) {
			h = (ViewHolder) params[0];
			path = (String) params[1];

			// Load
			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, op);
			op.inJustDecodeBounds = false;
			op.inSampleSize = Utility.computeSampleSize(op, -1, 160 * 160);
			return BitmapFactory.decodeFile(path, op);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (path.equals(h.path)) {
				h.img.setImageBitmap(result);
				mBitmaps.put(path, new WeakReference<Bitmap>(result));
			}
		}
	}
}
