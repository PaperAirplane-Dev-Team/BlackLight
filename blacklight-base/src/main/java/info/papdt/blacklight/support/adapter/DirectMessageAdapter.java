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
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.support.v7.widget.CardView;

import com.squareup.picasso.Picasso;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.model.DirectMessageListModel;
import info.papdt.blacklight.model.DirectMessageModel;
import info.papdt.blacklight.support.HackyMovementMethod;
import info.papdt.blacklight.support.LogF;
import info.papdt.blacklight.support.SpannableStringUtils;
import info.papdt.blacklight.support.StatusTimeUtils;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.directmessage.DirectMessageImageActivity;

public class DirectMessageAdapter extends BaseAdapter
{
	private Context mContext;
	private LayoutInflater mInflater;
	private DirectMessageListModel mList;
	private DirectMessageListModel mClone;
	private long mUid;

	public DirectMessageAdapter(Context context, DirectMessageListModel list, String uid) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = list;
		mUid = Long.parseLong(uid);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mClone.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mClone.get(calcPos(position));
	}

	@Override
	public long getItemId(int position) {
		return calcPos(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			final DirectMessageModel msg = mClone.get(calcPos(position));
			View v = null;

			v = convertView != null ? convertView : mInflater.inflate(R.layout.direct_message_conversation_item, null);
			final ViewHolder h = v.getTag() != null ? (ViewHolder) v.getTag() : new ViewHolder(v);

			LinearLayout container = h.container;
			if (msg.sender_id == mUid) {
				container.setGravity(Gravity.LEFT);
				h.card.setBackgroundResource(R.color.purple_500);
				h.content.setTextColor(mContext.getResources().getColor(R.color.white));
			} else {
				container.setGravity(Gravity.RIGHT);
				h.card.setBackgroundResource(R.color.white);
				h.content.setTextColor(mContext.getResources().getColor(R.color.action_gray));
			}

			h.content.setText(SpannableStringUtils.span(mContext, msg.text));
			h.content.setMovementMethod(HackyMovementMethod.getInstance());

			if (msg.att_ids[0] != 0) { // has pic
				Log.d("DirectMessage", "has pic" + msg.att_ids[0]);
				Runnable r = new Runnable() {
					@Override
					public void run() {
						String url = info.papdt.blacklight.api.Constants.DIRECT_MESSAGES_THUMB_PIC;
						url = String.format(url,msg.att_ids[0], BaseApi.getAccessToken(),240,240);
						Picasso.with(mContext).load(url).into(h.pic);
					}
				};
				v.postDelayed(r, 200);
				h.pic.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(mContext, DirectMessageImageActivity.class);
						i.putExtra("fid",msg.att_ids[0]);
						mContext.startActivity(i);
					}
				});
			}

			h.date.setText(StatusTimeUtils.instance(mContext).buildTimeString(msg.created_at));



			return v;
		}
	}

	@Override
	public void notifyDataSetChanged() {
		mClone = mList.clone();
		super.notifyDataSetChanged();
	}

	// Convert position to real position (upside-down list)
	private int calcPos(int position) {
		return getCount() - position - 1;
	}

	class ViewHolder {
		private View v;
		public TextView content;
		public TextView date;
		public LinearLayout container;
		public ImageView pic;
		public View card;

		public ViewHolder(View v) {
			this.v = v;

			content = Utility.findViewById(v, R.id.direct_message_conversation_content);
			date = Utility.findViewById(v, R.id.direct_message_conversation_date);
			container = Utility.findViewById(v, R.id.direct_message_conversation_container);
			card = Utility.findViewById(v, R.id.direct_message_card);
			pic = Utility.findViewById(v, R.id.direct_message_pic);

			v.setTag(this);
		}
	}
}
