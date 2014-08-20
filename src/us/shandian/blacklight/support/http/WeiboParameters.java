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

package us.shandian.blacklight.support.http;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

/*
  My own implementation of WeiboParameters
  Used to pass params to remote
*/
public class WeiboParameters extends HashMap<String, Object>
{
	
    private static final long serialVersionUID = 3453824930034213774L;

    // URL Encode
	public String encode() {
		StringBuilder str = new StringBuilder();
		Set<String> keys = keySet();
		boolean first = true;
		
		for (String key : keys) {
			Object value = get(key);
			
			if (value instanceof Bitmap) {
				// Bitmap detected, we should use multipart encode instead
				return null;
			} else {
				if (first) {
					first = false;
				} else {
					str.append("&");
				}
				
				try {
					str.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value.toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					
				}
			}
		}
		
		return str.toString();
	}
	
	public Object[] toBoundaryMsg() {
		String b = getBoundaryStr();
		StringBuilder str = new StringBuilder();
		str.append("--").append(b).append("\r\n");
		
		Set<String> keys = keySet();
		Bitmap bitmap = null;
		String bmKey = null;
		for (String key : keys) {
			Object value = get(key);
			
			if (value instanceof Bitmap) {
				bitmap = (Bitmap) value;
				bmKey = key;
			} else {
				str.append("Content-Disposition: form-data; name=\"");
				str.append(key).append("\"\r\n\r\n");
				str.append(value).append("\r\n--");
				str.append(b).append("\r\n");
			}
		}
		
		if (bitmap != null) {
			str.append("Content-Disposition: form-data; name=\"");
			str.append(bmKey).append("\"; filename=\"").append(System.currentTimeMillis()).append(".jpg");
			str.append("\"\r\nContent-Type: image/jpeg\r\n\r\n");
		}
		
		return new Object[]{b, bitmap, str.toString()};
	}
	
	private String getBoundaryStr() {
		return String.valueOf(System.currentTimeMillis() * Math.random() % 1024);
	}
}
