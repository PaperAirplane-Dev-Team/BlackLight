package us.shandian.blacklight.ui.statuses;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.os.AsyncTask;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.adapter.WeiboAdapter;

public class HomeTimeLineFragment extends Fragment
{
	private ListView mList;
	private WeiboAdapter mAdapter;
	private HomeTimeLineApiCache mCache;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getActivity().getActionBar().setTitle(R.string.timeline);
		
		View v = inflater.inflate(R.layout.home_timeline, null);
		mList = (ListView) v.findViewById(R.id.home_timeline);
		mCache = new HomeTimeLineApiCache(getActivity());
		mCache.loadFromCache();
		mAdapter = new WeiboAdapter(getActivity(), mCache.mMessages);
		mList.setAdapter(mAdapter);
		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute();
		}
		return v;
	}
	
	private class Refresher extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void[] params) {
			mCache.load(true);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mAdapter.notifyDataSetChanged();
		}

		
	}
}
