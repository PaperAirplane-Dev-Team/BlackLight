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

import android.net.http.AndroidHttpClient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.IOException;

public class FeedbackUtility {

	private final static String LOG_API = "http://bbug.typeblog.net/bl-crashlog";

	public static void sendLog(String user,String contact,String log){
		BasicHttpParams params = new BasicHttpParams();
		if(user != null){
			params.setParameter("user",user);
		}
		if(contact != null){
			params.setParameter("contact",contact);
		}
		params.setParameter("log",log);

		HttpUriRequest request = new HttpPost(LOG_API);
		request.setParams(params);
		HttpClient client = new DefaultHttpClient();
		try {
			client.execute(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendFeedback(String user,String contact,String feedback){

	}
}
