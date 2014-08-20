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

import android.annotation.SuppressLint;
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
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.model.UserListModel;
import us.shandian.blacklight.support.AsyncTask;

public class UserAdapter extends BaseAdapter
{
	private UserListModel mUsers;
	private LayoutInflater mInflater;
	private UserApiCache mUserApi;
	
	private HashMap<String, View> mViews = new HashMap<String, View>();
	
	public UserAdapter(Context context, UserListModel users) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUserApi = new UserApiCache(context);
		mUsers = users;
	}
	
	@Override
	public int getCount() {
		return mUsers.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			UserModel usr = mUsers.get(position);
			
			if (mViews.containsKey(usr.id)) {
				return mViews.get(usr.id);
			} else {
				View v = mInflater.inflate(R.layout.user_list_item, null);
				
				ImageView avatar = (ImageView) v.findViewById(R.id.user_list_avatar);
				TextView name = (TextView) v.findViewById(R.id.user_list_name);
				TextView des = (TextView) v.findViewById(R.id.user_list_des);
				
				name.setText(usr.getName());
				des.setText(usr.description);
				
				new AvatarDownloader().execute(avatar, usr);
				
				mViews.put(usr.id, v);
				
				return v;
			}
		}
	}
	
	public void notifyDataSetChangedAndClear() {
		super.notifyDataSetChanged();

		// Fix memory leak
		mViews.clear();
	}
	
	private class AvatarDownloader extends AsyncTask<Object, Void, Object[]> {
		@Override
		protected Object[] doInBackground(Object... params) {
			UserModel usr = (UserModel) params[1];
			
			Bitmap bmp = mUserApi.getSmallAvatar(usr);
			
			return new Object[]{params[0], bmp};
		}
		
		@Override
		protected void onPostExecute(Object[] result) {
			if (result[0] != null && result[1] != null) {
				((ImageView) result[0]).setImageBitmap((Bitmap) result[1]);
			}
		}
	}
}
