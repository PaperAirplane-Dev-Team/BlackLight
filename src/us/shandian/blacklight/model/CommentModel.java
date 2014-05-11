package us.shandian.blacklight.model;

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
