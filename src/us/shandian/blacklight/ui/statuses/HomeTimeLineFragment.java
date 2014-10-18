package us.shandian.blacklight.ui.statuses;

import us.shandian.blacklight.ui.main.MainActivity;

public class HomeTimeLineFragment extends TimeLineFragment {
	@Override
	protected void load(boolean param) {
		mCache.load(param, ((MainActivity) getActivity()).mCurrentGroupId);
		mCache.cache();
	}
}
