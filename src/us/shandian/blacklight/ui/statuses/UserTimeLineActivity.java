package us.shandian.blacklight.ui.statuses;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;

public class UserTimeLineActivity extends SwipeBackActivity
{
	private UserTimeLineFragment mFragment;
	private UserModel mModel;
	
	private TextView mName;
	private TextView mFollowState;
	private TextView mDes;
	private TextView mFollowers;
	private TextView mFollowing;
	private TextView mMsgs;
	private TextView mLikes;
	private TextView mGeo;
	private ImageView mAvatar;
	private View mCover;
	
	private UserApiCache mCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_timeline_activity);
		
		mCache = new UserApiCache(this);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Arguments
		mModel = getIntent().getParcelableExtra("user");
		
		// Views
		mName = (TextView) findViewById(R.id.user_name);
		mFollowState = (TextView) findViewById(R.id.user_follow_state);
		mDes = (TextView) findViewById(R.id.user_des);
		mFollowers = (TextView) findViewById(R.id.user_followers);
		mFollowing = (TextView) findViewById(R.id.user_following);
		mMsgs = (TextView) findViewById(R.id.user_msgs);
		mLikes = (TextView) findViewById(R.id.user_like);
		mGeo = (TextView) findViewById(R.id.user_geo);
		mAvatar = (ImageView) findViewById(R.id.user_avatar);
		mCover = findViewById(R.id.user_cover);
		
		// View values
		mName.setText(mModel.getName());
		
		// Follower state (following/followed/each other)
		if (mModel.follow_me && mModel.following) {
			mFollowState.setText(R.string.following_each_other);
		} else if (mModel.follow_me) {
			mFollowState.setText(R.string.following_me);
		} else if (mModel.following) {
			mFollowState.setText(R.string.i_am_following);
		} else {
			mFollowState.setText(R.string.no_following);
		}
		
		// Also view values
		mDes.setText(mModel.description);
		mFollowers.setText(String.valueOf(mModel.followers_count));
		mFollowing.setText(String.valueOf(mModel.friends_count));
		mMsgs.setText(String.valueOf(mModel.statuses_count));
		mLikes.setText(String.valueOf(mModel.favourites_count));
		mGeo.setText(mModel.location);
		
		new Downloader().execute();
		
		// Init
		if (BaseApi.hasBlackMagic()) {
			mFragment = new UserTimeLineFragment(mModel.id);
			getFragmentManager().beginTransaction().replace(R.id.user_timeline_container, mFragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class Downloader extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void[] params) {
			// Avatar
			Bitmap avatar = mCache.getLargeAvatar(mModel);
			publishProgress(new Object[]{0, avatar});
			
			// Cover
			if (!mModel.cover_image.trim().equals("")) {
				Bitmap cover = mCache.getCover(mModel);
				if (cover != null) {
					publishProgress(new Object[]{1, cover});
				}
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			
			switch ((int) values[0]) {
				case 0:
					if (mAvatar != null) {
						mAvatar.setImageBitmap((Bitmap) values[1]);
					}
					break;
				case 1:
					if (mCover != null) {
						mCover.setBackground(new BitmapDrawable((Bitmap) values[1]));
					}
					break;
			}
		}
	}
}
