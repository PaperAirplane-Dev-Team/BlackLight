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

package us.shandian.blacklight.support.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.GalleryModel;
import us.shandian.blacklight.support.Utility;

public class GalleryAdapter extends BaseAdapter {
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
			WeakReference<Bitmap> w = mBitmaps.get(gallery.path);
			Bitmap bmp = w != null ? w.get() : null;
			
			if (bmp == null) {
				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(gallery.path, op);
				op.inJustDecodeBounds = false;
				op.inSampleSize = Utility.computeSampleSize(op, -1, 160 * 160);
				bmp = BitmapFactory.decodeFile(gallery.path, op);
				mBitmaps.put(gallery.path, new WeakReference<Bitmap>(bmp));
			}

			h.img.setImageBitmap(bmp);
			h.check.setChecked(gallery.checked);

			return v;
		}
	}

	class ViewHolder {
		private View v;

		@InjectView(R.id.img_picker_img) public ImageView img;
		@InjectView(R.id.img_picker_check) public CheckBox check;

		public ViewHolder(View v) {
			this.v = v;
			ButterKnife.inject(this, v);
		}
	}
}
