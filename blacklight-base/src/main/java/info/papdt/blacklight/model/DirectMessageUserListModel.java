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

/*
  Long-long class name. Pain.
*/
public class DirectMessageUserListModel extends BaseListModel<DirectMessageUserModel, DirectMessageUserListModel>
{
	
	public List<DirectMessageUserModel> user_list = new ArrayList<DirectMessageUserModel>();
	
	@Override
	public int getSize() {
		return user_list.size();
	}

	@Override
	public DirectMessageUserModel get(int position) {
		return user_list.get(position);
	}

	@Override
	public List<DirectMessageUserModel> getList() {
		return user_list;
	}

	@Override
	public void addAll(boolean toTop, DirectMessageUserListModel values) {
		if (values != null && values.getSize() > 0) {
			for (DirectMessageUserModel user : values.getList()) {
				if (!user_list.contains(user)) {
					user_list.add(toTop ? values.getList().indexOf(user) : user_list.size(), user);
				}
			}
			total_number = values.total_number;
			previous_cursor = values.previous_cursor;
			next_cursor = values.next_cursor;
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
		dest.writeTypedList(user_list);
	}
	
	public static final Parcelable.Creator<DirectMessageUserListModel> CREATOR = new Parcelable.Creator<DirectMessageUserListModel>() {
		@Override
		public DirectMessageUserListModel createFromParcel(Parcel in) {
			DirectMessageUserListModel ret = new DirectMessageUserListModel();
			ret.total_number = in.readInt();
			ret.previous_cursor = in.readString();
			ret.next_cursor = in.readString();
			in.readTypedList(ret.user_list, DirectMessageUserModel.CREATOR);
			return ret;
		}

		@Override
		public DirectMessageUserListModel[] newArray(int size) {
			return new DirectMessageUserListModel[size];
		}

		
	};
}
