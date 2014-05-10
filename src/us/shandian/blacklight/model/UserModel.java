package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable
{
	public transient long timestamp = System.currentTimeMillis(); // Time when wrote to database
	
	// Json mapping fields
	public String id;
	public String screen_name;
	public String name;
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
	
	public String getName() {
		return screen_name == null ? name : screen_name;
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
		dest.writeBooleanArray(new boolean[]{following, allow_all_act_msg, geo_enabled, verified, allow_all_comment});
	}
	
	public static final Parcelable.Creator<UserModel> CREATOR = new Parcelable.Creator<UserModel>() {
		@Override
		public UserModel createFromParcel(Parcel input) {
			UserModel ret = new UserModel();
			ret.id = input.readString();
			ret.screen_name = input.readString();
			ret.name = input.readString();
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
