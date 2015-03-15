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

package info.papdt.blacklight.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import info.papdt.blacklight.support.FilterUtility;
import info.papdt.blacklight.support.SpannableStringUtils;
import info.papdt.blacklight.support.StatusTimeUtils;

/*
  List of messages
  From timelines
  
  credits to: qii
  author: PeterCxy
*/
public class MessageListModel extends BaseListModel<MessageModel, MessageListModel>
{
	private class AD {
		public long id = -1;
		public String mark = "";

		@Override
		public boolean equals(Object o) {
			if (o instanceof AD) {
				return ((AD) o).id == id;
			} else {
				return super.equals(o);
			}
		}

		@Override
		public int hashCode() {
			return String.valueOf(id).hashCode();
		}
	}
	
	private List<MessageModel> statuses = new ArrayList<MessageModel>();
	private List<AD> ad = new ArrayList<AD>();
	
	public void spanAll(Context context) {
		for (MessageModel msg : getList()) {
			msg.span = SpannableStringUtils.getSpan(context, msg);
			
			if (msg.retweeted_status != null) {
				msg.retweeted_status.origSpan = SpannableStringUtils.getOrigSpan(context, msg.retweeted_status);
			}
		}
	}

	public void timestampAll(Context context) {
		StatusTimeUtils utils = StatusTimeUtils.instance(context);
		for (MessageModel msg : getList()) {
			msg.millis = utils.parseTimeString(msg.created_at);

			if (msg.retweeted_status != null) {
				msg.retweeted_status.millis = utils.parseTimeString(msg.retweeted_status.created_at);
			}
		}
	}
	
	@Override
	public int getSize() {
		return statuses.size();
	}

	@Override
	public MessageModel get(int position) {
		return statuses.get(position);
	}

	@Override
	public List<? extends MessageModel> getList() {
		return statuses;
	}
	
	@Override
	public void addAll(boolean toTop, MessageListModel values) {
		addAll(toTop, false, values, null);
	}

	public void addAll(boolean toTop, boolean friendsOnly, MessageListModel values, String myUid) {
		if (values != null && values.getSize() > 0) {
			for (MessageModel msg : values.getList()) {
				if (!statuses.contains(msg) && !values.ad.contains(msg.id) && msg.user != null
					&& (msg.user.following || msg.user.id.equals(myUid) || !friendsOnly)
					&& !FilterUtility.shouldFilter(msg.text)) {
					
					statuses.add(toTop ? values.getList().indexOf(msg) : statuses.size(), msg);
				}
			}
			total_number = values.total_number;
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(total_number);
		dest.writeString(previous_cursor);
		dest.writeString(next_cursor);
		dest.writeTypedList(statuses);
	}
	
	public static final Parcelable.Creator<MessageListModel> CREATOR = new Parcelable.Creator<MessageListModel>() {

		@Override
		public MessageListModel createFromParcel(Parcel in) {
			MessageListModel ret = new MessageListModel();
			ret.total_number = in.readInt();
			ret.previous_cursor = in.readString();
			ret.next_cursor = in.readString();
			in.readTypedList(ret.statuses, MessageModel.CREATOR);
			
			return ret;
		}

		@Override
		public MessageListModel[] newArray(int size) {
			return new MessageListModel[size];
		}

		
	};

}
