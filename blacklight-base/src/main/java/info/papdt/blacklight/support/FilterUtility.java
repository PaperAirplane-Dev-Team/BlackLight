/* 
 * Copyright (C) 2015 Peter Cai
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

package info.papdt.blacklight.support;

import android.content.Context;

public class FilterUtility
{
	private static String[] mKeywords = new String[0];
	
	public static void init(Context context) {
		Settings settings = Settings.getInstance(context);
		String keywords = settings.getString(Settings.KEYWORD, "").trim();
		
		if (!keywords.equals(""))
			mKeywords = keywords.split(", ");
	}
	
	public static boolean shouldFilter(String text) {
		if (mKeywords.length == 0)
			return false;
		
		for (String key : mKeywords) {
			if (key.startsWith("`") && key.endsWith("`")) {
				// Regex
				if (text.matches(key.substring(1, key.length() - 1)))
					return true;
			} else if (text.contains(key)) {
				return true;
			}
		}
		
		return false;
	}
}
