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
import android.text.TextUtils;

public class UserModel implements Parcelable
{
	public transient long timestamp = System.currentTimeMillis(); // Time when wrote to database

	private String nameWithRemark;

	// Json mapping fields
	public String id;
	public String screen_name;
	public String name;
	public String remark;
	public String province;
	public String city;
	public String location;
	public String description;
	public String url;
	public String profile_image_url;
	public String domain;
	public String gender;
	public int followers_count = 0;
	public int friends_count = 0;
	public int statuses_count = 0;
	public int favourites_count = 0;
	public int verified_type = 0;
	public String created_at;
	public boolean following = false;
	public boolean allow_all_act_msg = false;
	public boolean geo_enabled = false;
	public boolean verified = false;
	public boolean allow_all_comment = false;
	public String avatar_large;
	public String verified_reason;
	public boolean follow_me = false;
	public int online_status = 0;
	public int bi_followers_count = 0;
	public String cover_image = "";
	public String cover_image_phone = "";

	public String getName() {
		if (TextUtils.isEmpty(remark)){
			return screen_name == null ? name : screen_name;
		} else if (nameWithRemark == null){
			nameWithRemark = String.format("%s(%s)", (screen_name == null ? name : screen_name), remark);
		}
		return nameWithRemark;
	}

	public String getNameNoRemark() {
		return screen_name == null ? name : screen_name;
	}

	public String getCover() {
		return cover_image.trim().equals("") ? cover_image_phone : cover_image;
	}

	// FIXME: 'boolean' has only two values.
	public boolean isMale() {
		return gender != null && gender.equals("m");
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(screen_name);
		dest.writeString(name);
		dest.writeString(remark);
		dest.writeString(province);
		dest.writeString(city);
		dest.writeString(location);
		dest.writeString(description);
		dest.writeString(url);
		dest.writeString(profile_image_url);
		dest.writeString(domain);
		dest.writeString(gender);
		dest.writeString(created_at);
		dest.writeString(avatar_large);
		dest.writeString(verified_reason);
		dest.writeInt(followers_count);
		dest.writeInt(friends_count);
		dest.writeInt(statuses_count);
		dest.writeInt(favourites_count);
		dest.writeInt(verified_type);
		dest.writeInt(online_status);
		dest.writeInt(bi_followers_count);
		dest.writeString(cover_image_phone);
		dest.writeString(cover_image);
		dest.writeBooleanArray(new boolean[]{following, allow_all_act_msg, geo_enabled, verified, allow_all_comment});
	}

	public static final Parcelable.Creator<UserModel> CREATOR = new Parcelable.Creator<UserModel>() {
		@Override
		public UserModel createFromParcel(Parcel input) {
			UserModel ret = new UserModel();
			ret.id = input.readString();
			ret.screen_name = input.readString();
			ret.name = input.readString();
			ret.remark = input.readString();
			ret.province = input.readString();
			ret.city = input.readString();
			ret.location = input.readString();
			ret.description = input.readString();
			ret.url = input.readString();
			ret.profile_image_url = input.readString();
			ret.domain = input.readString();
			ret.gender = input.readString();
			ret.created_at = input.readString();
			ret.avatar_large = input.readString();
			ret.verified_reason = input.readString();
			ret.followers_count = input.readInt();
			ret.friends_count = input.readInt();
			ret.statuses_count = input.readInt();
			ret.favourites_count = input.readInt();
			ret.verified_type = input.readInt();
			ret.online_status = input.readInt();
			ret.bi_followers_count = input.readInt();
			ret.cover_image_phone = input.readString();
			ret.cover_image = input.readString();

			boolean[] array = new boolean[5];
			input.readBooleanArray(array);

			ret.following = array[0];
			ret.allow_all_act_msg = array[1];
			ret.geo_enabled = array[2];
			ret.verified = array[3];
			ret.allow_all_comment = array[4];

			return ret;
		}

		@Override
		public UserModel[] newArray(int size) {
			return new UserModel[size];
		}
	};

}
