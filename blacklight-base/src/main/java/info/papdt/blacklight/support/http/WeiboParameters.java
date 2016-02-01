/*
 * Copyright (C) 2016 Paper Airplane Dev Team
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

package info.papdt.blacklight.support.http;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/*
  My own (a little bit poor) implementation of WeiboParameters
  Used to pass params to remote
*/
public class WeiboParameters extends HashMap<String, Object>
{

	// URL Encode
	public String encode() {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		Iterator iter = entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry entry = (HashMap.Entry) iter.next();
			String key = (String) entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Bitmap) return "pic";
			// Bitmap detected, we should use multipart encode instead
			else {
				if (first) first = false;
				else str.append("&");
				try {
					str.append(URLEncoder.encode(key, "UTF-8"))
								.append("=")
								.append(URLEncoder.encode(value.toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) { }
			}
		}
		return str.toString();
	}

	public String getFilename() {
		return String.valueOf(System.currentTimeMillis() * Math.random() % 1024 + ".jpg");
	}
}
