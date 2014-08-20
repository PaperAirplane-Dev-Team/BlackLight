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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.DirectMessageUserModel;
import us.shandian.blacklight.model.DirectMessageUserListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.StatusTimeUtils;

public class DirectMessageUserAdapter extends BaseAdapter
{
	private DirectMessageUserListModel mList;
	private LayoutInflater mInflater;
	private UserApiCache mUserApi;
	private Context mContext;
	private HashMap<DirectMessageUserModel, View> mViews = new HashMap<DirectMessageUserModel, View>();
	
	public DirectMessageUserAdapter(Context context, DirectMessageUserListModel list) {
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUserApi = new UserApiCache(context);
		mContext = context;
	}
	
	@Override
	public int getCount() {
		return mList.getSize();
	}

	@Override
	public Object getItem(int position) {
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
			DirectMessageUserModel user = mList.get(position);
			View v;
			ViewHolder h;
			
			if (mViews.containsKey(user)) {
				v = mViews.get(user);
				h = (ViewHolder) v.getTag();
			} else {
				v = mInflater.inflate(R.layout.direct_message_user, null);
				h = new ViewHolder(v, user);
				
				TextView name = h.getName();
				TextView text = h.getText();
				
				name.setText(user.user.getName());
				text.setText(user.direct_message.text);
				
				new AvatarDownloader().execute(v);
				
				mViews.put(user, v);
			}
			
			TextView date = h.getDate();
			
			date.setText(StatusTimeUtils.instance(mContext).buildTimeString(user.direct_message.created_at));
			
			return v;
		}
	}
	
	public void notifyDataSetChangedAndClear() {
		notifyDataSetChanged();
		
		mViews.clear();
	}
	
	private class AvatarDownloader extends AsyncTask<View, Void, Object[]> {
		@Override
		protected Object[] doInBackground(View... params) {
			if (params[0] != null) {
				DirectMessageUserModel u = ((ViewHolder) params[0].getTag()).user;
				
				Bitmap img = mUserApi.getSmallAvatar(u.user);
				
				return new Object[] {params[0], img};
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			
			if (result != null && result.length == 2) {
				View v = (View) result[0];
				Bitmap img = (Bitmap) result[1];
				((ViewHolder) v.getTag()).getAvatar().setImageBitmap(img);
			}
		}
	}
	
	private class ViewHolder {
		public DirectMessageUserModel user;
		private ImageView avatar;
		private TextView name, text, date;
		private View v;
		
		public ViewHolder(View v, DirectMessageUserModel user) {
			this.v = v;
			this.user = user;
			
			v.setTag(this);
		}
		
		public ImageView getAvatar() {
			if (avatar == null) {
				avatar = (ImageView) v.findViewById(R.id.direct_message_avatar);
			}
			
			return avatar;
		}
		
		public TextView getName() {
			if (name == null) {
				name = (TextView) v.findViewById(R.id.direct_message_name);
			}
			
			return name;
		}
		
		public TextView getText() {
			if (text == null) {
				text = (TextView) v.findViewById(R.id.direct_message_text);
			}
			
			return text;
		}
		
		public TextView getDate() {
			if (date == null) {
				date = (TextView) v.findViewById(R.id.direct_message_date);
			}
			
			return date;
		}
	}
}
