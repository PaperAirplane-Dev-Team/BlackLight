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
