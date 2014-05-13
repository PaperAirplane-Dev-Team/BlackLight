package us.shandian.blacklight.ui.statuses;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.cache.statuses.MentionsTimeLineApiCache;

public class MentionsTimeLineFragment extends HomeTimeLineFragment
{
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new MentionsTimeLineApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.status_mention_me);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
	}
}
