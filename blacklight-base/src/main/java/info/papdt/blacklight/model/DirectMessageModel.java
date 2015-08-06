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
  A map of Direct Message from Weibo Api
*/
public class DirectMessageModel implements Parcelable
{
	// JSON Mapping
	public long id;
	public String idstr;
	public String created_at;
	public String text;
	public long sender_id;
	public long recipient_id;
	public String sender_screen_name;
	public String recipient_screen_name;
	public long[] att_ids = {0,0};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(idstr);
		dest.writeString(created_at);
		dest.writeString(text);
		dest.writeLong(sender_id);
		dest.writeLong(recipient_id);
		dest.writeString(sender_screen_name);
		dest.writeString(recipient_screen_name);
		dest.writeLongArray(att_ids);
	}
	
	public static final Parcelable.Creator<DirectMessageModel> CREATOR = new Parcelable.Creator<DirectMessageModel>() {
		@Override
		public DirectMessageModel createFromParcel(Parcel in) {
			DirectMessageModel ret = new DirectMessageModel();
			ret.id = in.readLong();
			ret.idstr = in.readString();
			ret.created_at = in.readString();
			ret.text = in.readString();
			ret.sender_id = in.readLong();
			ret.recipient_id = in.readLong();
			ret.sender_screen_name = in.readString();
			ret.recipient_screen_name = in.readString();
			in.readLongArray(ret.att_ids);
			return ret;
		}

		@Override
		public DirectMessageModel[] newArray(int size) {
			return new DirectMessageModel[size];
		}
	};
}
