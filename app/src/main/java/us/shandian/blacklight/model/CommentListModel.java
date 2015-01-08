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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;

/*
  A list of comments
*/

public class CommentListModel extends MessageListModel
{
	private List<CommentModel> comments = new ArrayList<CommentModel>();

	@Override
	public void spanAll(Context context) {
		super.spanAll(context);
		
		for (CommentModel comment : comments) {
			if (comment.reply_comment != null) {
				comment.reply_comment.origSpan = SpannableStringUtils.getOrigSpan(context, comment.reply_comment);
			} else if (comment.status != null) {
				comment.status.origSpan = SpannableStringUtils.getOrigSpan(context, comment.status);
			}
		}
	}

	@Override
	public void timestampAll(Context context) {
		super.timestampAll(context);

		StatusTimeUtils utils = StatusTimeUtils.instance(context);

		for (CommentModel comment : (List<CommentModel>) getList()) {
			if (comment.status != null) {
				comment.status.millis = utils.parseTimeString(comment.status.created_at);
			}
		}
	}
	
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
	public void addAll(boolean toTop, MessageListModel values, String myUid) {
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
	public void addAll(boolean toTop, boolean friendsOnly, MessageListModel values) {
		addAll(toTop, values);
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
