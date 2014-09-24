package us.shandian.blacklight.ui.statuses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;

import us.shandian.blacklight.R;

import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class HomeTimeLineFragment extends TimeLineFragment{
	@Override
	protected void load(boolean param) {
		mCache.load(param, ((MainActivity) getActivity()).mCurrentGroupId);
		mCache.cache();
	}
}
