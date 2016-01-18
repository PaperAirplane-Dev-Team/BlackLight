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

package info.papdt.blacklight.ui.search;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.search.SearchApi;
import info.papdt.blacklight.support.AsyncTask;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/*
 * Displays a dropdown of people that the user may want to at
 */
public class AtUserSuggestTextView extends AutoCompleteTextView {
	private static final String TAG = AtUserSuggestTextView.class.getSimpleName();
	
	private Runnable mRunnable;

	public AtUserSuggestTextView(Context context) {
		this(context, null);
	}

	public AtUserSuggestTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AtUserSuggestTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setThreshold(1);
		setAdapter(new ArrayAdapter<String>(context, R.layout.at_suggestion_item, new String[]{}));
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		String t = text.toString();

		int cur = getSelectionStart();
		int start = t.substring(0, cur).lastIndexOf("@");

		if (DEBUG) {
			Log.d(TAG, "t = " + t + "; start = " + start);
		}

		if (start != -1) {
			String sub = t.substring(start + 1, cur);
			
			if (DEBUG) {
				Log.d(TAG, sub);
			}

			if (sub.length() != 0 && !sub.contains(" ")) {
				new SuggestTask().execute(sub);
				super.performFiltering(text, keyCode);
				return;
			}
		}

		this.dismissDropDown();
	}

	@Override
	protected void replaceText(CharSequence text) {
		String t = getText().toString();

		int cur = getSelectionStart();
		int start = t.substring(0, cur).lastIndexOf("@");

		if (start != -1) {
			String former = t.substring(0, start);
			String latter = t.substring(cur, t.length());
			setText(former + "@" + text + " " + latter);
			int selection = former.length() + text.length() + 2;
			setSelection(selection, selection);
		}
	}

	private void calculateDropDownOffset() {
		int start = getSelectionStart();
		Layout layout = getLayout();
		int line = layout.getLineForOffset(start);
		int baseline = layout.getLineBaseline(line);
		int ascent = layout.getLineAscent(line);
		setDropDownHorizontalOffset((int) layout.getPrimaryHorizontal(start));
		setDropDownVerticalOffset(-(getHeight() - baseline + ascent));

		if (DEBUG) {
			Log.d(TAG, "baseline = " + baseline + "; ascent = " + ascent);
		}
	}
	
	private synchronized void postChangeAdapter(final ArrayAdapter adapter) {
		if (mRunnable != null) {
			removeCallbacks(mRunnable);
		}
		
		mRunnable = new Runnable() {
			@Override
			public void run() {
				setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		};
		
		postDelayed(mRunnable, 200);
	}

	private class SuggestTask extends AsyncTask<String, Void, String[]> {
		String q = "";

		@Override
		protected String[] doInBackground(String... params) {
			q = params[0];
			ArrayList<String> a = SearchApi.suggestAtUser(params[0], 5);
			return a.toArray(new String[a.size()]);
		}

		@Override
		protected void onPostExecute(String[] result) {
			if (result != null && result.length != 0) {
				calculateDropDownOffset();
				ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.at_suggestion_item, result);
				postChangeAdapter(adapter);
			}
		}
	}
}


