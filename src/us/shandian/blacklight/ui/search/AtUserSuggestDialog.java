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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.search.SearchApi;
import us.shandian.blacklight.support.AsyncTask;
import android.widget.*;

public class AtUserSuggestDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener
{
	public static interface AtUserListener {
		public void onChooseUser(String name);
	}
	
	private Context mContext;
	private EditText mText;
	private View mSearch;
	private ListView mList;
	
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
		
		mText = (EditText) findViewById(R.id.at_user_text);
		mSearch = findViewById(R.id.at_user_search);
		mList = (ListView) findViewById(R.id.at_user_list);
		
		mSearch.setOnClickListener(this);
		mList.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mLoading) return;
		
		new SearchTask().execute(mText.getText().toString());
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
