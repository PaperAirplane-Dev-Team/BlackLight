package us.shandian.blacklight.model;

import android.os.Parcelable;

import java.util.List;

/* Type of all weibo json lists */
public interface BaseListModel<ITEM, LIST> extends Parcelable
{
	public int total_number;
	public String previous_cursor, next_cursor;
	
	public int getSize();
	public ITEM get(int position);
	public List<ITEM> getList();
	
	/*
	  @param toTop If true, add to top, else add to bottom
	  @param values All values needed to be added
	*/
	public void addAll(boolean toTop, LIST values);
}
