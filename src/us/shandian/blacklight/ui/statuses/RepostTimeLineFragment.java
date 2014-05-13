package us.shandian.blacklight.ui.statuses;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.statuses.RepostTimeLineApiCache;

public class RepostTimeLineFragment extends HomeTimeLineFragment
{
	private long mId;
	
	public RepostTimeLineFragment(long id) {
		mId = id;
		mBindOrig = false;
	}
	
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new RepostTimeLineApiCache(getActivity(), mId);
	}

	@Override
	protected void initTitle() {
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
}
