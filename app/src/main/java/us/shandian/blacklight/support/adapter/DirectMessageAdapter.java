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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.support.v7.widget.CardView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.DirectMessageListModel;
import us.shandian.blacklight.model.DirectMessageModel;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;
import us.shandian.blacklight.support.Utility;

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
			DirectMessageModel msg = mClone.get(calcPos(position));
			View v = null;
			ViewHolder h = null;
			
			v = convertView != null ? convertView : mInflater.inflate(R.layout.direct_message_conversation_item, null);
			h = v.getTag() != null ? (ViewHolder) v.getTag() : new ViewHolder(v);
			
			LinearLayout container = h.container;
			if (msg.sender_id == mUid) {
				container.setGravity(Gravity.LEFT);
				h.card.setBackgroundResource(R.color.action_gray);
				h.content.setTextColor(mContext.getResources().getColor(R.color.white));
			} else {
				container.setGravity(Gravity.RIGHT);
				h.card.setBackgroundResource(R.color.white);
				h.content.setTextColor(mContext.getResources().getColor(R.color.action_gray));
			}
			
			h.content.setText(SpannableStringUtils.span(mContext, msg.text));
			h.content.setMovementMethod(HackyMovementMethod.getInstance());
			
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
		public CardView card;
		
		public ViewHolder(View v) {
			this.v = v;
			
			content = Utility.findViewById(v, R.id.direct_message_conversation_content);
			date = Utility.findViewById(v, R.id.direct_message_conversation_date);
			container = Utility.findViewById(v, R.id.direct_message_conversation_container);
			card = Utility.findViewById(v, R.id.direct_message_card);
			
			v.setTag(this);
		}
	}
}
