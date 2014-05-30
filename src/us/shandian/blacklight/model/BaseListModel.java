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

import android.os.Parcelable;

import java.util.List;

/* Type of all weibo json lists */
public interface BaseListModel<I, L> extends Parcelable
{
	public int total_number;
	public String previous_cursor, next_cursor;
	
	public int getSize();
	public I get(int position);
	public List<? extends I> getList();
	
	/*
	  @param toTop If true, add to top, else add to bottom
	  @param values All values needed to be added
	*/
	public void addAll(boolean toTop, L values);
}
