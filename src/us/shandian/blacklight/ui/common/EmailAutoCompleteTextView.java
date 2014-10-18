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

package us.shandian.blacklight.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import us.shandian.blacklight.R;

public class EmailAutoCompleteTextView extends AutoCompleteTextView {
	private static final String TAG = "EmailAutoCompleteTextView";

	private String[] emailSufixs;

	public EmailAutoCompleteTextView(Context context) {
		super(context);
		init(context);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs,
									 int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setAdapterString(String[] es) {
		if (es != null && es.length > 0) this.emailSufixs = es;
	}

	private void init(final Context context) {
		emailSufixs = context.getResources().getStringArray(R.array.email_auto_complete_tips);

		this.setAdapter(new EmailAutoCompleteAdapter(
						context,
						R.layout.email_autocomplete_dropdown_item,
						emailSufixs)
		);

		this.setThreshold(1);

		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					String text = EmailAutoCompleteTextView.this.getText().toString();
					if (!"".equals(text))
						performFiltering(text, 0);
				}
			}

		});
	}

	@Override
	protected void replaceText(CharSequence text) {
		String t = this.getText().toString();
		int index = t.indexOf("@");
		if (index != -1) t = t.substring(0, index);
		super.replaceText(t + text);
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		String t = text.toString();

		int index = t.indexOf("@");
		if (index == -1) {
			if (t.matches("^[a-zA-Z0-9_]+$")) {
				super.performFiltering("@", keyCode);
			} else {
				this.dismissDropDown();
			}
		} else {
			super.performFiltering(t.substring(index), keyCode);
		}
	}

	private class EmailAutoCompleteAdapter extends ArrayAdapter<String> {

		public EmailAutoCompleteAdapter(Context context, int textViewResourceId, String[] email_s) {
			super(context, textViewResourceId, email_s);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater
						.from(getContext())
						.inflate(R.layout.email_autocomplete_dropdown_item, null);
			}

			TextView tv = (TextView) v.findViewById(android.R.id.text1);

			String t = EmailAutoCompleteTextView.this.getText().toString();
			int index = t.indexOf("@");
			if (index != -1) t = t.substring(0, index);
			tv.setText(t + getItem(position));
			return v;
		}
	}
}
