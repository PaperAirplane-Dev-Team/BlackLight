package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/*
  A list of comments
*/

public class CommentListModel extends MessageListModel
{
	private List<CommentModel> comments = new ArrayList<CommentModel>();
	
	@Override
	public int getSize() {
		return comments.size();
	}

	@Override
	public CommentModel get(int position) {
		return comments.get(position);
	}

	@Override
	public List<? extends MessageModel> getList() {
		return comments;
	}

	@Override
	public void addAll(boolean toTop, MessageListModel values) {
		if (values instanceof CommentListModel && values != null && values.getSize() > 0) {
			for (MessageModel msg : values.getList()) {
				if (!comments.contains(msg)) {
					comments.add(toTop ? values.getList().indexOf(msg) : comments.size(), (CommentModel) msg);
				}
			}
			total_number = values.total_number;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(total_number);
		dest.writeString(previous_cursor);
		dest.writeString(next_cursor);
		dest.writeTypedList(comments);
	}

	public static final Parcelable.Creator<MessageListModel> CREATOR = new Parcelable.Creator<MessageListModel>() {

		@Override
		public CommentListModel createFromParcel(Parcel in) {
			CommentListModel ret = new CommentListModel();
			ret.total_number = in.readInt();
			ret.previous_cursor = in.readString();
			ret.next_cursor = in.readString();
			in.readTypedList(ret.comments, CommentModel.CREATOR);

			return ret;
		}

		@Override
		public CommentListModel[] newArray(int size) {
			return new CommentListModel[size];
		}


	};
	
}
