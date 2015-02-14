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

package info.papdt.blacklight.api;

import android.util.Log;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class PrivateKey extends BaseApi
{
	private static final String TAG = PrivateKey.class.getSimpleName();
	
	private static final String APP_ID = "211160679";
	private static final String APP_KEY_HASH = "1e6e33db08f9192306c4afa0a61ad56c";
	private static final String REDIRECT_URI = "http://oauth.weico.cc";
	private static final String PACKAGE_NAME = "com.eico.weico";
	private static final String SCOPE = "email,direct_messages_read,direct_messages_write,friendships_groups_read,friendships_groups_write,statuses_to_me_read,follow_app_official_microblog,invitation_write";
	
	public static String getOauthLoginPage() {
		return Constants.OAUTH2_ACCESS_AUTHORIZE + "?" + "client_id=" + APP_ID
				+ "&response_type=token&redirect_uri=" + REDIRECT_URI 
				+ "&key_hash=" + APP_KEY_HASH + "&packagename=" + PACKAGE_NAME
				+ "&display=mobile" + "&scope=" + SCOPE;
	}
	
	public static boolean isUrlRedirected(String url) {
		return url.startsWith(REDIRECT_URI);
	}
}
