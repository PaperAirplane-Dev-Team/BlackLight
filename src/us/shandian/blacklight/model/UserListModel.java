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

public class UserListModel extends BaseListModel<UserModel, UserListModel>
{
	
	private List<UserModel> users = new ArrayList<UserModel>();
	
	@Override
	public int getSize() {
		return users.size();
	}

	@Override
	public UserModel get(int position) {
		return users.get(position);
	}

	@Override
	public List<? extends UserModel> getList() {
		return users;
	}

	@Override
	public void addAll(boolean toTop, UserListModel values) {
		if (values != null && values.getSize() > 0) {
			for (UserModel user : values.getList()) {
				if (!users.contains(user)) {
					users.add(toTop ? values.getList().indexOf(user) : users.size(), user);
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
		dest.writeTypedList(users);
	}
	
	public static final Parcelable.Creator<UserListModel> CREATOR = new Parcelable.Creator<UserListModel>() {
		@Override
		public UserListModel createFromParcel(Parcel in) {
			UserListModel ret = new UserListModel();
			ret.total_number = in.readInt();
			ret.previous_cursor = in.readString();
			ret.next_cursor = in.readString();
			in.readTypedList(ret.users, UserModel.CREATOR);
			return ret;
		}

		@Override
		public UserListModel[] newArray(int size) {
			return new UserListModel[size];
		}
	};

}
