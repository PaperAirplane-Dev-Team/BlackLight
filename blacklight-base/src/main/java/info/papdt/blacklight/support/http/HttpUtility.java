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
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.FormBody;

import info.papdt.blacklight.support.LogF;

import static info.papdt.blacklight.BuildConfig.DEBUG;

public class HttpUtility
{
	private static final String TAG = HttpUtility.class.getSimpleName();
  private static final OkHttpClient client = new OkHttpClient.Builder()
												.connectTimeout(5, TimeUnit.SECONDS)
												.readTimeout(5, TimeUnit.SECONDS)
												.writeTimeout(5, TimeUnit.SECONDS)
												.build();
	public static final String POST = "POST";
	public static final String GET = "GET";

	public static String doRequest(String url, WeiboParameters params, String method) throws Exception {
		boolean isGet = method.equals(GET);
		String myUrl = url;
		String send = params.encode();
		if (isGet) {
			myUrl += "?" + send;
		}

		if (DEBUG) {
			Log.d(TAG, "method = " + method);
			Log.d(TAG, "send = " + send);
			Log.d(TAG, "myUrl = " + myUrl);
		}

		Request.Builder builder = new Request.Builder()
										.url(myUrl)
										.cacheControl(CacheControl.FORCE_NETWORK)
										.addHeader("Connection", "Keep-Alive")
										.addHeader("Charset", "UTF-8");


		if(params.containsKey("access_token")) {
			builder.addHeader("Authorization", "OAuth2 " + params.get("access_token"));
		}

		if (!send.equals("pic")) { //No pictures
			if (!isGet) { //Post text only
				FormBody.Builder reqBuilder = new FormBody.Builder();
				Iterator iter = params.entrySet().iterator();
				while (iter.hasNext()) {
					WeiboParameters.Entry entry = (WeiboParameters.Entry) iter.next();
					reqBuilder.add((String)entry.getKey(), String.valueOf(entry.getValue()));
				}
				builder.post(reqBuilder.build());
			}
		} else { //Pictures to upload
				Bitmap bmp;
				MultipartBody.Builder mulBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
				Iterator iter = params.entrySet().iterator();
				while (iter.hasNext()) {
					WeiboParameters.Entry entry = (WeiboParameters.Entry) iter.next();
					String key = (String) entry.getKey();
					Object value = entry.getValue();
					if (value instanceof Bitmap){
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						((Bitmap)value).compress(Bitmap.CompressFormat.JPEG, 90, bytes);
						byte[] img = bytes.toByteArray();
						mulBuilder.addFormDataPart(key, params.getFilename(),
											RequestBody.create(MediaType.parse("image/jpeg"), img));
					}
					else{
						mulBuilder.addFormDataPart(key, String.valueOf(value));
					}
				}
				RequestBody body =  mulBuilder.build();
				builder.post(body);
			}

			Response response = client.newCall(builder.build()).execute();
			ResponseBody body = response.body();
			String result = null;
			if (!response.isSuccessful()){
				LogF.e(TAG, "Http request not sucessful, code:%d", response.code());
			}
			else{
				result = body.string();
				Log.d(TAG, result);
			}
			body.close();
			return result;
		}

		public static Response getUrl(String url) throws IOException{
			return client.newCall(new Request.Builder().url(url).get().build()).execute();
		}
}
