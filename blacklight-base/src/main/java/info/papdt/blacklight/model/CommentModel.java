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
  Stands for a comment
  Mapping with weibo api json
*/
public class CommentModel extends MessageModel
{
	// Additional mapping fields
	public MessageModel status;
	public CommentModel reply_comment;

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Write only needed fields
		dest.writeString(created_at);
		dest.writeLong(id);
		dest.writeString(text);
		dest.writeString(source);
		dest.writeParcelable(user, flags);
		dest.writeLong(mid);
		dest.writeString(idstr);
		dest.writeParcelable(status, flags);
		dest.writeParcelable(reply_comment, flags);
	}
	
	public static final Parcelable.Creator<CommentModel> CREATOR = new Parcelable.Creator<CommentModel>() {

		@Override
		public CommentModel createFromParcel(Parcel in) {
			CommentModel ret = new CommentModel();
			ret.created_at = in.readString();
			ret.id = in.readLong();
			ret.text = in.readString();
			ret.source = in.readString();
			ret.user = in.readParcelable(UserModel.class.getClassLoader());
			ret.mid = in.readLong();
			ret.idstr = in.readString();
			ret.status = in.readParcelable(MessageModel.class.getClassLoader());
			
			try {
				ret.reply_comment = in.readParcelable(CommentModel.class.getClassLoader());
			} catch (Exception e) {
				// This field might be null
				ret.reply_comment = null;
			}
			
			return ret;
		}

		@Override
		public CommentModel[] newArray(int size) {
			return new CommentModel[size];
		}

		
	};
}
