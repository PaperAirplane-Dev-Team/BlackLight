package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.comments.CommentMentionsTimeLineApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;

public class CommentMentionsTimeLineFragment extends HomeTimeLineFragment
{

	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new CommentMentionsTimeLineApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.comment_mention_me);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
	
}
