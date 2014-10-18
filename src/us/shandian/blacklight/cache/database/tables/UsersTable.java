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

package us.shandian.blacklight.cache.database.tables;

public class UsersTable {
	public static final String NAME = "users";

	public static final String UID = "uid";

	public static final String USERNAME = "username";

	public static final String TIMESTAMP = "timestamp";

	public static final String JSON = "json";

	public static final String CREATE = "create table " + NAME
			+ "("
			+ UID + " integer primary key autoincrement,"
			+ TIMESTAMP + " integer,"
			+ USERNAME + " text,"
			+ JSON + " text"
			+ ");";
}
