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
import info.papdt.blacklight.model.DirectMessageUserListModel;
import info.papdt.blacklight.model.DirectMessageUserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.StatusTimeUtils;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.directmessage.DirectMessageConversationActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

public class DirectMessageUserAdapter extends HeaderViewAdapter<DirectMessageUserAdapter.ViewHolder>
{
	private DirectMessageUserListModel mList;
	private DirectMessageUserListModel mClone;
	private LayoutInflater mInflater;
	private UserApiCache mUserApi;
	private Context mContext;

	public DirectMessageUserAdapter(Context context, DirectMessageUserListModel list, RecyclerView recycler) {
		super(recycler);
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUserApi = new UserApiCache(context);
		mContext = context;
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
		// dummy
	}

	@Override
	public ViewHolder doCreateViewHolder(ViewGroup parent, int position) {
		View v = mInflater.inflate(R.layout.direct_message_user, parent, false);
		return new ViewHolder(v, null);
	}

	@Override
	public ViewHolder doCreateHeaderHolder(View header) {
		return new ViewHolder(header);
	}

	@Override
	public void doBindViewHolder(ViewHolder h, int position) {
		DirectMessageUserModel user = mClone.get(position);

		h.user = user;

		TextView name = h.name;
		TextView text = h.text;

		name.setText(user.user.getName());
		text.setText(user.direct_message.text);
		h.avatar.setImageBitmap(null);

		Picasso.with(mContext)
			.load(h.user.user.profile_image_url)
			.fit()
			.centerCrop()
			.into(h.avatar);

		TextView date = h.date;

		date.setText(StatusTimeUtils.instance(mContext).buildTimeString(user.direct_message.created_at));
	}

	public void notifyDataSetChangedAndClone() {
		mClone = mList.clone();
		super.notifyDataSetChanged();
	}

	public static class ViewHolder extends HeaderViewAdapter.ViewHolder {
		public DirectMessageUserModel user;
		public ImageView avatar;
		public TextView name;
		public TextView text;
		public TextView date;
		private View v;

		public ViewHolder(View v) {
			super(v);
			isHeader = true;
		}

		public ViewHolder(View v, DirectMessageUserModel user) {
			super(v);
			this.v = v;
			this.user = user;

			avatar = Utility.findViewById(v, R.id.direct_message_avatar);
			name = Utility.findViewById(v, R.id.direct_message_name);
			text = Utility.findViewById(v, R.id.direct_message_text);
			date = Utility.findViewById(v, R.id.direct_message_date);

			v.setTag(this);

			Utility.bindOnClick(this, v, "show");
			Utility.bindOnLongClick(this, v, "showUser");
		}

		@Binded
		void show() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), DirectMessageConversationActivity.class);
			i.putExtra("user", user.user);
			v.getContext().startActivity(i);
		}

		@Binded
		boolean showUser() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", user.user);
			v.getContext().startActivity(i);
			return true;
		}

	}
}
