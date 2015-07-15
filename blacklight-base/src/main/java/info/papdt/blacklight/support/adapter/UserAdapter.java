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
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.UserListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

public class UserAdapter extends HeaderViewAdapter<UserAdapter.ViewHolder>
{
	private UserListModel mUsers;
	private UserListModel mClone;
	private LayoutInflater mInflater;
	private UserApiCache mUserApi;

	public UserAdapter(Context context, UserListModel users, RecyclerView recycler) {
		super(recycler);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUserApi = new UserApiCache(context);
		mUsers = users;
		notifyDataSetChangedAndClone();
	}

	@Override
	public int getCount() {
		return mClone.getSize();
	}

	@Override
	public long getItemViewId(int position) {
		return position;
	}

	@Override
	public int getViewType(int position) {
		return 0;
	}

	@Override
	public void doRecycleView(ViewHolder h) {
		h.avatar.setImageResource(R.color.gray);
	}

	@Override
	public ViewHolder doCreateViewHolder(ViewGroup parent, int viewType) {
		View v = mInflater.inflate(R.layout.user_list_item, parent, false);
		return new ViewHolder(null, v);
	}

	@Override
	public ViewHolder doCreateHeaderHolder(View header) {
		return new ViewHolder(header);
	}

	@Override
	public void doBindViewHolder(ViewHolder h, int position) {
		if (position >= getCount()) return;
			UserModel usr = mClone.get(position);

			h.user = usr;

			h.name.setText(usr.getName());
			h.des.setText(usr.description);

			Picasso.with(h.avatar.getContext())
				.load(usr.profile_image_url)
				.fit()
				.centerCrop()
				.into(h.avatar);
	}

	public void notifyDataSetChangedAndClone() {
		mClone = mUsers.clone();
		super.notifyDataSetChanged();
	}

	public static class ViewHolder extends HeaderViewAdapter.ViewHolder {
		public View v;
		public ImageView avatar;
		public TextView name, des;
		public UserModel user;

		public ViewHolder(View header) {
			super(header);
		}

		public ViewHolder(UserModel user, View v) {
			super(v);
			this.v = v;
			this.user = user;

			avatar = Utility.findViewById(v, R.id.user_list_avatar);
			name = Utility.findViewById(v, R.id.user_list_name);
			des = Utility.findViewById(v, R.id.user_list_des);

			Utility.bindOnClick(this, v, "show");
		}

		@Binded
		void show() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", user);
			v.getContext().startActivity(i);
		}
	}
}
