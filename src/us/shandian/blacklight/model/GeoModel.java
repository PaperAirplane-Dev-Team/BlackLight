package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

/* Maps a geo info from Weibo Api */
public class GeoModel implements Parcelable
{
	public String type;
	public double[] coordinates = new double[]{0.0, 0.0}; // Position
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeDoubleArray(coordinates);
	}
	
	public static final Parcelable.Creator<GeoModel> CREATOR = new Parcelable.Creator<GeoModel>() {

		@Override
		public GeoModel createFromParcel(Parcel in) {
			GeoModel ret = new GeoModel();
			ret.type = in.readString();
			in.readDoubleArray(ret.coordinates);
			return ret;
		}

		@Override
		public GeoModel[] newArray(int size) {
			return new GeoModel[size];
		}

		
	};
	
}
