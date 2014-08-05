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

import java.util.HashMap;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.DirectMessageModel;
import us.shandian.blacklight.model.DirectMessageListModel;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;

public class DirectMessageAdapter extends BaseAdapter
{
	private Context mContext;
	private LayoutInflater mInflater;
	private DirectMessageListModel mList;
	private HashMap<DirectMessageModel, View> mViews = new HashMap<DirectMessageModel, View>();
	private long mUid;
	
	public DirectMessageAdapter(Context context, DirectMessageListModel list, String uid) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = list;
		mUid = Long.parseLong(uid);
	}
	
	@Override
	public int getCount() {
		return mList.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(calcPos(position));
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
			DirectMessageModel msg = mList.get(calcPos(position));
			View v = null;
			ViewHolder h = null;
			
			if (mViews.containsKey(msg)) {
				v = mViews.get(msg);
				h = (ViewHolder) v.getTag();
			} else {
				v = mInflater.inflate(R.layout.direct_message_conversation_item, null);
				h = new ViewHolder(v);
				
				LinearLayout container = (LinearLayout) v.findViewById(R.id.direct_message_conversation_container);
				if (msg.sender_id == mUid) {
					container.setGravity(Gravity.LEFT);
				} else {
					container.setGravity(Gravity.RIGHT);
					container.setAlpha(0.8f);
				}
				
				h.getContent().setText(SpannableStringUtils.span(mContext, msg.text));
				h.getContent().setMovementMethod(HackyMovementMethod.getInstance());
				
				mViews.put(msg, v);
			}
			
			h.getDate().setText(StatusTimeUtils.instance(mContext).buildTimeString(msg.created_at));
			
			return v;
		}
	}
	
	public void notifyDataSetChangedAndClear() {
		notifyDataSetChanged();

		mViews.clear();
	}
	
	// Convert position to real position (upside-down list)
	private int calcPos(int position) {
		return getCount() - position - 1;
	}
	
	private class ViewHolder {
		private View v;
		private TextView content, date;
		
		public ViewHolder(View v) {
			this.v = v;
			v.setTag(this);
		}
		
		public TextView getContent() {
			if (content == null) {
				content = (TextView) v.findViewById(R.id.direct_message_conversation_content);
			}
			return content;
		}
		
		public TextView getDate() {
			if (date == null) {
				date = (TextView) v.findViewById(R.id.direct_message_conversation_date);
			}
			return date;
		}
	}
}
