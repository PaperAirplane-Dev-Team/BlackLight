package us.shandian.blacklight.ui.statuses;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import us.shandian.blacklight.R;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class HomeTimeLineFragment extends TimeLineFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if (hasSmartBar()){
            Log.i("s", "onCreateOptionsMenu for SmartBar!");
            inflater.inflate(R.menu.home_timeline, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if (menuItem.getItemId() == R.id.post_new) {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.setClass(getActivity(), NewPostActivity.class);
            startActivity(i);
            return true;
        }

        return false;
    }

}
