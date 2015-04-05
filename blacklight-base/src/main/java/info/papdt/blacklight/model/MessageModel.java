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
import android.text.SpannableString;

import java.util.ArrayList;

/* 
  This class represents a message (or comment)
  In timelines or message pages
  Including auto-highlight function
  Matching with Json from Weibo Api
  
  credits to: qii(github.com/qii/weiciyuan)
  author: PeterCxy
*/
public class MessageModel implements Parcelable
{
	
	public static class PictureUrl implements Parcelable {
		// Picture url
		// OMG Sina why you use a special class for a simple data!
		
		private String thumbnail_pic;
		
		public String getThumbnail() {
			return thumbnail_pic;
		}
		
		public String getLarge() {
			return thumbnail_pic.replace("thumbnail", "large");
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(thumbnail_pic);
		}
		
		public static final Parcelable.Creator<PictureUrl> CREATOR = new Parcelable.Creator<PictureUrl>() {

			@Override
			public MessageModel.PictureUrl createFromParcel(Parcel in) {
				MessageModel.PictureUrl ret = new MessageModel.PictureUrl();
				ret.thumbnail_pic = in.readString();
				return ret;
			}

			@Override
			public MessageModel.PictureUrl[] newArray(int size) {
				return new MessageModel.PictureUrl[size];
			}

			
		};
		
	}
	
	// Json mapping fields
	public String created_at;
	public long id;
	public long mid;
	public String idstr;
	public String text; // content of this weibo
	public String source;
	public boolean favorited;
	public boolean truncated;
	public boolean liked;
	public String in_reply_to_status_id;
	public String in_reply_to_user_id;
	public String in_reply_to_screen_name;
	public String thumbnail_pic;
	public String bmiddle_pic;
	public String original_pic;
	
	public GeoModel geo;
	public UserModel user;
	public MessageModel retweeted_status; // if retweeted, this field will be the original post
	
	public int reposts_count;
	public int comments_count;
	public int attitudes_count;
	public ArrayList<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
	
	public transient SpannableString span, origSpan;
	public transient long millis;
	
	// public Object visible; ignored. We do not need this field at all.
	
	// Array field
	public ArrayList<PictureUrl> pic_urls = new ArrayList<PictureUrl>();

	public boolean inSingleActivity;

	public boolean hasMultiplePictures() {
		return pic_urls.size() > 1;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MessageModel) {
			return ((MessageModel) o).id == id;
		} else {
			return super.equals(o);
		}
	}

	@Override
	public int hashCode() {
		return idstr.hashCode();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(created_at);
		dest.writeLong(id);
		dest.writeLong(mid);
		dest.writeString(idstr);
		dest.writeString(text);
		dest.writeString(source);
		dest.writeBooleanArray(new boolean[]{favorited, truncated, liked, inSingleActivity});
		dest.writeString(in_reply_to_status_id);
		dest.writeString(in_reply_to_user_id);
		dest.writeString(in_reply_to_screen_name);
		dest.writeString(thumbnail_pic);
		dest.writeString(bmiddle_pic);
		dest.writeString(original_pic);
		dest.writeParcelable(geo, flags);
		dest.writeParcelable(user, flags);
		dest.writeParcelable(retweeted_status, flags);
		dest.writeInt(reposts_count);
		dest.writeInt(comments_count);
		dest.writeInt(attitudes_count);
		dest.writeTypedList(pic_urls);
		dest.writeLong(millis);
		dest.writeTypedList(annotations);
	}
	
	public static final Parcelable.Creator<MessageModel> CREATOR = new Parcelable.Creator<MessageModel>() {

		@Override
		public MessageModel createFromParcel(Parcel in) {
			MessageModel ret = new MessageModel();
			ret.created_at = in.readString();
			ret.id = in.readLong();
			ret.mid = in.readLong();
			ret.idstr = in.readString();
			ret.text = in.readString();
			ret.source = in.readString();
			
			boolean[] array = new boolean[4];
			in.readBooleanArray(array);
			
			ret.favorited = array[0];
			ret.truncated = array[1];
            ret.liked = array[2];

			ret.inSingleActivity = array[3];
			
			ret.in_reply_to_status_id = in.readString();
			ret.in_reply_to_user_id = in.readString();
			ret.in_reply_to_screen_name = in.readString();
			ret.thumbnail_pic = in.readString();
			ret.bmiddle_pic = in.readString();
			ret.original_pic = in.readString();
			ret.geo = in.readParcelable(GeoModel.class.getClassLoader());
			ret.user = in.readParcelable(UserModel.class.getClassLoader());
			ret.retweeted_status = in.readParcelable(MessageModel.class.getClassLoader());
			ret.reposts_count = in.readInt();
			ret.comments_count = in.readInt();
			ret.attitudes_count = in.readInt();
			
			in.readTypedList(ret.pic_urls, PictureUrl.CREATOR);

			ret.millis = in.readLong();

			in.readTypedList(ret.annotations, AnnotationModel.CREATOR);
			
			return ret;
		}

		@Override
		public MessageModel[] newArray(int size) {
			return new MessageModel[size];
		}

		
	};
	
}
