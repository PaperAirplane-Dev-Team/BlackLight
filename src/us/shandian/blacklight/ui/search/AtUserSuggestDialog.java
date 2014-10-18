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

package us.shandian.blacklight.ui.search;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import us.shandian.blacklight.R;
import us.shandian.blacklight.api.search.SearchApi;
import us.shandian.blacklight.support.AsyncTask;

public class AtUserSuggestDialog extends Dialog {
	public static interface AtUserListener {
		public void onChooseUser(String name);
	}
	
	private Context mContext;
	@InjectView(R.id.at_user_text) EditText mText;
	@InjectView(R.id.at_user_list) ListView mList;
	
	private String[] mStrs;
	private boolean mLoading = false;
	
	private AtUserListener mListener;
	
	public AtUserSuggestDialog(Context context) {
		super(context);
		
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hide title
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		// Views
		setContentView(R.layout.at_user_dialog);
		
		ButterKnife.inject(this);

		mText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				doSearch();
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	public void doSearch() {
		if (mLoading) return;
		
		new SearchTask().execute(mText.getText().toString());
	}

	@OnItemClick(R.id.at_user_list)
	public void chooseUser(AdapterView<?> parent, View view, int position, long id) {
		if (mListener != null && mStrs != null && position < mStrs.length) {
			mListener.onChooseUser(mStrs[position]);
			dismiss();
		}
	}
	
	public void setListener(AtUserListener listener) {
		mListener = listener;
	}
	
	private class SearchTask extends AsyncTask<String, Void, String[]> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoading = true;
		}
		
		@Override
		protected String[] doInBackground(String... params) {
			ArrayList<String> a = SearchApi.suggestAtUser(params[0], 5);
			return a.toArray(new String[a.size()]);
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			
			ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, result);
			
			mList.setAdapter(adapter);
			
			mStrs = result;
			mLoading = false;
		}
	}
}
