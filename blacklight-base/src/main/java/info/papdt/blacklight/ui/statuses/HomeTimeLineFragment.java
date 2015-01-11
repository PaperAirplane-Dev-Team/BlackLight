package info.papdt.blacklight.ui.statuses;

import info.papdt.blacklight.ui.main.MainActivity;

public class HomeTimeLineFragment extends TimeLineFragment{
	@Override
	protected void load(boolean param) {
		mCache.load(param, ((MainActivity) getActivity()).mCurrentGroupId);
		mCache.cache();
	}
}
