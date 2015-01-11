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

package info.papdt.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DirectMessageListModel extends BaseListModel<DirectMessageModel, DirectMessageListModel>
{
	private List<DirectMessageModel> direct_messages = new ArrayList<DirectMessageModel>();
	
	@Override
	public int getSize() {
		return direct_messages.size();
	}

	@Override
	public DirectMessageModel get(int position) {
		return direct_messages.get(position);
	}

	@Override
	public List<? extends DirectMessageModel> getList() {
		return direct_messages;
	}

	@Override
	public void addAll(boolean toTop, DirectMessageListModel values) {
		if (values != null && values.getSize() > 0) {
			for (DirectMessageModel msg : values.getList()) {
				if (!direct_messages.contains(msg)) {
					direct_messages.add(toTop ? values.getList().indexOf(msg) : direct_messages.size(), msg);
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
		dest.writeTypedList(direct_messages);
	}
	
	public static final Parcelable.Creator<DirectMessageListModel> CREATOR = new Parcelable.Creator<DirectMessageListModel>() {
		@Override
		public DirectMessageListModel createFromParcel(Parcel in) {
			DirectMessageListModel ret = new DirectMessageListModel();
			in.readTypedList(ret.direct_messages, DirectMessageModel.CREATOR);
			return ret;
		}

		@Override
		public DirectMessageListModel[] newArray(int size) {
			return new DirectMessageListModel[size];
		}
	};

}
