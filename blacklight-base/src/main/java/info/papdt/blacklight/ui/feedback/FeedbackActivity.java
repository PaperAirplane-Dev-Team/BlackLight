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

package info.papdt.blacklight.ui.feedback;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import info.papdt.blacklight.R;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.http.FeedbackUtility;
import info.papdt.blacklight.ui.common.AbsActivity;

public class FeedbackActivity extends AbsActivity {
	private EditText mTitle;
	private EditText mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.feedback;
		super.onCreate(savedInstanceState);

		// views
		mTitle = Utility.findViewById(this, R.id.fb_title);
		mContent = Utility.findViewById(this, R.id.fb_content);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.feedback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.fb_send) {
			new SubmitFeedback().execute(mTitle.getText().toString(), mContent.getText().toString());
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private class SubmitFeedback extends AsyncTask<String, Void, Void> {
		private ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			prog = new ProgressDialog(FeedbackActivity.this);
			prog.setMessage(getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			LoginApiCache login = new LoginApiCache(FeedbackActivity.this);
			UserApiCache user = new UserApiCache(FeedbackActivity.this);
			String uid = login.getUid();

			if (uid != null) {
				UserModel m = user.getUser(uid);
				String name = m.screen_name;
				String contact = "http://weibo.com/u/" + uid;
				FeedbackUtility.sendFeedback(name, contact, params[0], params[1]);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();

			Toast.makeText(FeedbackActivity.this, R.string.fb_sent, Toast.LENGTH_SHORT).show();

			finish();
		}
	}
}
