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

package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/*
 A list of reposts
*/

public class RepostListModel extends MessageListModel
{
	private List<MessageModel> reposts = new ArrayList<MessageModel>();

	@Override
	public int getSize() {
		return reposts.size();
	}

	@Override
	public MessageModel get(int position) {
		return reposts.get(position);
	}

	@Override
	public List<? extends MessageModel> getList() {
		return reposts;
	}

	@Override
	public void addAll(boolean toTop, MessageListModel values) {
		if (values != null && values.getSize() > 0) {
			for (MessageModel msg : values.getList()) {
				if (!reposts.contains(msg)) {
					reposts.add(toTop ? values.getList().indexOf(msg) : reposts.size(), msg);
				}
			}
			total_number = values.total_number;
		}
	}

	@Override
	public void addAll(boolean toTop, boolean friendsOnly, MessageListModel values, String myUid) {
		addAll(toTop, values);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(total_number);
		dest.writeString(previous_cursor);
		dest.writeString(next_cursor);
		dest.writeTypedList(reposts);
	}

	public static final Parcelable.Creator<MessageListModel> CREATOR = new Parcelable.Creator<MessageListModel>() {

		@Override
		public RepostListModel createFromParcel(Parcel in) {
			RepostListModel ret = new RepostListModel();
			ret.total_number = in.readInt();
			ret.previous_cursor = in.readString();
			ret.next_cursor = in.readString();
			in.readTypedList(ret.reposts, MessageModel.CREATOR);

			return ret;
		}

		@Override
		public RepostListModel[] newArray(int size) {
			return new RepostListModel[size];
		}

	};
}
