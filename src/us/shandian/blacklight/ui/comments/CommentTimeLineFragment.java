package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.view.MenuInflater;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.comments.CommentTimeLineApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;

/* 
  Shows latest comments (mine and recieved)
  Similar with HomeTimeLine, so we just extend this from HomeTimeLine
  To avoid unnecessary extra work
 */
public class CommentTimeLineFragment extends HomeTimeLineFragment
{

	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new CommentTimeLineApiCache(getActivity());
	}

	@Override
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.comment);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}
	
}
