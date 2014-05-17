package us.shandian.blacklight.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.ArrayList;

public class FavListModel
{
	private List<FavModel> favorites = new ArrayList<FavModel>();

	public MessageListModel toMsgList() {
		MessageListModel msg = new MessageListModel();
		msg.total_number = 0;
		msg.previous_cursor = "";
		msg.next_cursor = "";
		
		List<MessageModel> msgs = (List<MessageModel>) msg.getList();
		
		for (FavModel fav : favorites) {
			msgs.add(fav.status);
		}
		
		return msg;
	}
}
