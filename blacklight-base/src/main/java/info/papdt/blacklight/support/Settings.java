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
import android.content.SharedPreferences;

/*
  Settings Provider
*/
public class Settings
{
	public static final String XML_NAME = "settings";
	
	// Actions
	public static final String FAST_SCROLL = "fast_scroll";
	public static final String SHAKE_TO_RETURN = "shake_to_return";
	public static final String RIGHT_HANDED = "right_handed";
	public static final String KEYWORD = "keyword";

	// Notification
	public static final String NOTIFICATION_SOUND = "notification_sound",
			NOTIFICATION_VIBRATE = "notification_vibrate",
			NOTIFICATION_INTERVAL = "notification_interval",
			NOTIFICATION_ONGOING = "notification_ongoing";

	// Network
	public static final String AUTO_NOPIC = "auto_nopic";

	// Theme
	public static final String THEME_DARK = "theme_dark";

	// Debug
	public static final String LANGUAGE = "language";
	public static final String AUTO_SUBMIT_LOG = "debug_autosubmit";

	// Group
	public static final String CURRENT_GROUP = "current_group";
	
	// Position
	public static final String LAST_POSITION = "last_position";
	
	private static Settings sInstance;
	
	private SharedPreferences mPrefs;
	
	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Settings(context);
		}
		
		return sInstance;
	}
	
	private Settings(Context context) {
		mPrefs = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
	}
	
	public Settings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
		return this;
	}
	
	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}
	
	public Settings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
		return this;
	}
	
	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}

	public Settings putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}
	
}
