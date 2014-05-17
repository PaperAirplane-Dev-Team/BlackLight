package us.shandian.blacklight.ui.favorites;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.favorites.FavListApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;

public class FavListFragment extends HomeTimeLineFragment
{
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new FavListApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.like);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
}
