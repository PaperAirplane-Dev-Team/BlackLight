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

/*
  This is a connection between DirectMessage and User
*/
public class DirectMessageUserModel implements Parcelable
{
	// JSON Mapping
	public UserModel user;
	public DirectMessageModel direct_message;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, flags);
		dest.writeParcelable(direct_message, flags);
	}
	
	public static final Parcelable.Creator<DirectMessageUserModel> CREATOR = new Parcelable.Creator<DirectMessageUserModel>() {
		@Override
		public DirectMessageUserModel createFromParcel(Parcel in) {
			DirectMessageUserModel ret = new DirectMessageUserModel();
			ret.user = in.readParcelable(UserModel.class.getClassLoader());
			ret.direct_message = in.readParcelable(DirectMessageModel.class.getClassLoader());
			return ret;
		}

		@Override
		public DirectMessageUserModel[] newArray(int size) {
			return new DirectMessageUserModel[size];
		}
	};

}
