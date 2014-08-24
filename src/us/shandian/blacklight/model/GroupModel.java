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

public class GroupModel implements Parcelable {
	// Json mapping
	public long id;
	public String idstr;
	public String name;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(idstr);
		dest.writeString(name);
	}

	public static final Parcelable.Creator<GroupModel> CREATOR = new Parcelable.Creator<GroupModel>() {
		@Override
		public GroupModel createFromParcel(Parcel in) {
			GroupModel ret = new GroupModel();
			ret.id = in.readLong();
			ret.idstr = in.readString();
			ret.name = in.readString();
			return ret;
		}

		@Override
		public GroupModel[] newArray(int size) {
			return new GroupModel[size];
		}
	};
}
