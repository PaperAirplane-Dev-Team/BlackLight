package us.shandian.blacklight.ui.search;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Bundle;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.search.TopicsApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;

/*
  Shows the topics
*/
public class TopicsActivity extends SwipeBackActivity
{
	private String mTopic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_frame);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Argument
		mTopic = getIntent().getStringExtra("topic");
		
		// Fragment
		getFragmentManager().beginTransaction().replace(R.id.frame, new HackyFragment()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class HackyApiCache extends HomeTimeLineApiCache {
		public HackyApiCache(Context context) {
			super(context);
		}

		@Override
		public void loadFromCache() {
			// We don't need to cache
			mMessages = new MessageListModel();
		}

		@Override
		protected MessageListModel load() {
			return TopicsApi.searchTopic(mTopic, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
		}

		@Override
		public void cache() {
			// We don't need to cache
		}
	}
	
	private class HackyFragment extends  HomeTimeLineFragment {
		@Override
		protected HomeTimeLineApiCache bindApiCache() {
			return new HackyApiCache(getActivity());
		}

		@Override
		protected void initTitle() {

		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		}
	}
}
