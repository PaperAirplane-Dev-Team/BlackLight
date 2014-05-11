package us.shandian.blacklight.ui.statuses;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.os.AsyncTask;
import android.os.Bundle;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.adapter.WeiboAdapter;
import static us.shandian.blacklight.cache.Constants.HOME_TIMELINE_PAGE_SIZE;

public class HomeTimeLineFragment extends Fragment implements AbsListView.OnScrollListener, OnRefreshListener
{
	private ListView mList;
	private WeiboAdapter mAdapter;
	private HomeTimeLineApiCache mCache;
	
	// Pull To Refresh
	private PullToRefreshLayout mPullToRefresh;
	
	private boolean mRefreshing = false;
	
	protected boolean mBindOrig = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		initTitle();
		
		View v = inflater.inflate(R.layout.home_timeline, null);
		mList = (ListView) v.findViewById(R.id.home_timeline);
		mCache = bindApiCache();
		mCache.loadFromCache();
		mAdapter = new WeiboAdapter(getActivity(), mCache.mMessages, mBindOrig);
		mList.setAdapter(mAdapter);
		mList.setOnScrollListener(this);
		bindFooterView(inflater);
		
		// Pull To Refresh
		bindPullToRefresh(v);
		
		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute(new Boolean[]{true});
		}
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mCache.cache();
	}

	@Override
	public void onResume() {
		super.onResume();
		initTitle();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Refresh when scroll nearly to bottom
		if (!mRefreshing && totalItemCount >= HOME_TIMELINE_PAGE_SIZE &&  firstVisibleItem >= totalItemCount - 2 * visibleItemCount) {
			new Refresher().execute(new Boolean[]{false});
		}
	}
	
	@Override
	public void onRefreshStarted(View view) {
		if (!mRefreshing) {
			new Refresher().execute(new Boolean[]{true});
		} else {
			mPullToRefresh.setRefreshComplete();
		}
	}
	
	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}
	
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.timeline);
	}
	
	protected void bindFooterView(LayoutInflater inflater) {
		mList.addFooterView(inflater.inflate(R.layout.timeline_footer, null));
	}
	
	protected void bindPullToRefresh(View v) {
		mPullToRefresh = new PullToRefreshLayout(getActivity());
		
		ActionBarPullToRefresh.from(getActivity())
							  .insertLayoutInto((ViewGroup) v)
							  .theseChildrenArePullable(new View[]{mList})
							  .listener(this)
							  .setup(mPullToRefresh);
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Void>
	{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mRefreshing = true;
		}
		
		@Override
		protected Void doInBackground(Boolean[] params) {
			mCache.load(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mAdapter.notifyDataSetChanged();
			mRefreshing = false;
			if (mPullToRefresh != null) {
				mPullToRefresh.setRefreshComplete();
			}
		}

		
	}
}
