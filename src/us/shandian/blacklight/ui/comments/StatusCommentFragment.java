package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.cache.comments.StatusCommentApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;

public class StatusCommentFragment extends HomeTimeLineFragment
{
	private long mId;
	
	public StatusCommentFragment(long id) {
		mBindOrig = false;
		mId = id;
	}

	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new StatusCommentApiCache(getActivity(), mId);
	}

	@Override
	protected void initTitle() {
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
	
}
