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

/*
 * The simplest model
 * Maps the json object of unread count
 */

public class UnreadModel {
	public int status = 0,
			follower = 0,
			cmt = 0,
			dm = 0,
			mention_status = 0,
			mention_cmt = 0,
			group = 0,
			private_group = 0,
			notice = 0,
			invite = 0,
			badge = 0,
			photo = 0,
			msgbox = 0;

	@Override
	public String toString() {
		return new StringBuilder(13).append(follower)
				.append(cmt).append(dm).append(mention_status)
				.append(mention_cmt).append(group).append(private_group)
				.append(notice).append(invite).append(badge).append(photo)
				.append(msgbox).toString();
	}


}
