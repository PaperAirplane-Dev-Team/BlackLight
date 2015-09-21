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

package info.papdt.blacklight.ui.entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import info.papdt.blacklight.support.WeiboUrlUtility;

public class UrlEntryActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			if (null != uri) {
				new UrlParseTask().execute(uri);
				return;
			}
		}
		finish();
	}

	private class UrlParseTask extends AsyncTask<Uri, Void, Intent> {
		@Override
		protected Intent doInBackground(Uri... uris) {
			WeiboUrlUtility parser = new WeiboUrlUtility(uris[0]);
			return parser.getIntent(UrlEntryActivity.this);
		}

		@Override
		protected void onPostExecute(Intent intent) {
			if (null != intent) {
				startActivity(intent);
				String data = intent.getDataString();
				if (!TextUtils.isEmpty(data)) {
					Toast.makeText(UrlEntryActivity.this, data, Toast.LENGTH_SHORT).show();
				}
			}
			finish();
		}
	}
}
