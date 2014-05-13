package us.shandian.blacklight.ui.statuses;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.statuses.UserTimeLineApiCache;

/* Little modification from HomeTimeLineFragment to UserTimeLineFragment */
public class UserTimeLineFragment extends HomeTimeLineFragment
{
	private String mUid;
	
	public UserTimeLineFragment(String uid) {
		mUid = uid;
	}
	
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new UserTimeLineApiCache(getActivity(), mUid);
	}

	@Override
	protected void initTitle() {
		// Don't change my title
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
}
