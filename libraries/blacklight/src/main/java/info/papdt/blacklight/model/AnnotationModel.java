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

public class AnnotationModel implements Parcelable {
	public String bl_version = "";

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(bl_version);
	}

	public static final Parcelable.Creator<AnnotationModel> CREATOR = new Parcelable.Creator<AnnotationModel>() {
		@Override
		public AnnotationModel createFromParcel(Parcel in) {
			AnnotationModel ret = new AnnotationModel();
			ret.bl_version = in.readString();
			return ret;
		}

		@Override
		public AnnotationModel[] newArray(int size) {
			return new AnnotationModel[size];
		}
	};
}
