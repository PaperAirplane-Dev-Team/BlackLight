package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FavModel implements Parcelable
{
	// Json map
	public MessageModel status;
	public String favorited_time;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(status, flags);
		dest.writeString(favorited_time);
	}
	
	public static final Parcelable.Creator<FavModel> CREATOR = new Parcelable.Creator<FavModel>() {

		@Override
		public FavModel createFromParcel(Parcel in) {
			FavModel ret = new FavModel();
			ret.status = in.readParcelable(MessageModel.class.getClassLoader());
			ret.favorited_time = in.readString();
			return ret;
		}

		@Override
		public FavModel[] newArray(int size) {
			return new FavModel[size];
		}

		
	};
	
}
