package us.shandian.blacklight.ui.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import java.util.concurrent.TimeUnit;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.ui.comments.CommentTimeLineFragment;
import us.shandian.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import us.shandian.blacklight.ui.entry.EntryActivity;
import us.shandian.blacklight.ui.favorites.FavListFragment;
import us.shandian.blacklight.ui.login.LoginActivity;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;
import us.shandian.blacklight.ui.statuses.MentionsTimeLineFragment;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

/* Main Container Activity */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener
{
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mToggle;
	
	// Drawer content
	private TextView mName;
	private ImageView mAvatar;
	private ListView mMy;
	private ListView mAtMe;
	private ListView mOther;
	
	private LoginApiCache mLoginCache;
	private UserApiCache mUserCache;
	private UserModel mUser;
	
	// Fragments
	private Fragment[] mFragments = new Fragment[6];
	private FragmentManager mManager;
	
	// Temp fields
	private TextView mLastChoice;
	private int mCount = 0;
	private long mLast = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Initialize naviagtion drawer
		mDrawer = (DrawerLayout) findViewById(R.id.drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, 0, 0) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				
				if (mLastChoice == null) {
					mLastChoice = (TextView) mMy.getChildAt(0);
					mLastChoice.getPaint().setFakeBoldText(true);
					mLastChoice.invalidate();
				}
			}
		};
		mDrawer.setDrawerListener(mToggle);
		mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		
		mMy = (ListView) findViewById(R.id.list_my);
		mAtMe = (ListView) findViewById(R.id.list_at_me);
		mOther = (ListView) findViewById(R.id.list_other);
		mMy.setVerticalScrollBarEnabled(false);
		mMy.setChoiceMode(ListView.CHOICE_MODE_NONE);
		mAtMe.setVerticalScrollBarEnabled(false);
		mAtMe.setChoiceMode(ListView.CHOICE_MODE_NONE);
		mOther.setVerticalScrollBarEnabled(false);
		mOther.setChoiceMode(ListView.CHOICE_MODE_NONE);
		
		mMy.setOnItemClickListener(this);
		mAtMe.setOnItemClickListener(this);
		mOther.setOnItemClickListener(this);
		
		// My account
		mName = (TextView) findViewById(R.id.my_name);
		mName.getPaint().setFakeBoldText(true);
		mAvatar = (ImageView) findViewById(R.id.my_avatar);
		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		initList();
		new InitializerTask().execute();
		
		findViewById(R.id.my_account).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mUser != null) {
						Intent i = new Intent();
						i.setAction(Intent.ACTION_MAIN);
						i.setClass(MainActivity.this, UserTimeLineActivity.class);
						i.putExtra("user", mUser);
						startActivity(i);
					}
				}

		});
		
		// Initialize ActionBar Style
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Fragments
		mFragments[0] = new HomeTimeLineFragment();
		mFragments[1] = new CommentTimeLineFragment();
		mFragments[2] = new FavListFragment();
		mFragments[4] = new MentionsTimeLineFragment();
		mFragments[5] = new CommentMentionsTimeLineFragment();
		mManager = getFragmentManager();
		mManager.beginTransaction().replace(R.id.container, mFragments[0]).commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mToggle.syncState();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			mLoginCache = new LoginApiCache(this);
			initList();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			long now = System.currentTimeMillis();
			if (mCount == 0) {
				mCount++;
				mLast = System.currentTimeMillis();
			} else if (mCount == 40 && !mLoginCache.hasBlackMagic()) {
				mCount = 0;
				mLast = 0;
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(this, LoginActivity.class);
				startActivityForResult(i, 0);
			} else if (mCount > 0 && mCount < 40) {
				if (now - mLast <= TimeUnit.SECONDS.toMillis(1)) {
					mLast = now;
					mCount++;
				} else {
					mCount = 0;
					mLast = 0;
				}
			}
			return mToggle.onOptionsItemSelected(item);
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		if (parent != mOther && mLastChoice != null) {
			mLastChoice.getPaint().setFakeBoldText(false);
			mLastChoice.invalidate();
		}
		
		if (parent == mMy) {
			TextView tv = (TextView) view;
			tv.getPaint().setFakeBoldText(true);
			tv.invalidate();
			mLastChoice = tv;
			if (mFragments[position] != null) {
				tv.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							mManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
									.replace(R.id.container, mFragments[position]).commit();
						} catch (Exception e) {
							
						}
					}
				}, 800);
			}
		} else if (parent == mAtMe) {
			TextView tv = (TextView) view;
			tv.getPaint().setFakeBoldText(true);
			tv.invalidate();
			mLastChoice = tv;
			if (mFragments[4 + position] != null) {
				tv.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							mManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
									.replace(R.id.container, mFragments[4 + position]).commit();
						} catch (Exception e) {
							
						}
					}
				}, 800);
			}
		} else if (parent == mOther) {
			switch (position) {
				case 0:{
					mLoginCache.logout();
					Intent i = new Intent();
					i.setAction(Intent.ACTION_MAIN);
					i.setClass(this, EntryActivity.class);
					startActivity(i);
					finish();
					break;
				}
			}
		}
		
		mDrawer.closeDrawer(Gravity.START);
	}
	
	private void initList() {
		mLastChoice = null;
		mMy.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_item, getResources().getStringArray(mLoginCache.hasBlackMagic() ? R.array.my_array : R.array.my_array_no_bm)));
		mAtMe.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_item, getResources().getStringArray(R.array.at_me_array)));
		mOther.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_other_item, getResources().getStringArray(R.array.other_array)));
	}
	
	private class InitializerTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void[] params) {
			// Username first
			mUser = mUserCache.getUser(mLoginCache.getUid());
			publishProgress(new Object[]{0});
			
			// My avatar
			Bitmap avatar = mUserCache.getSmallAvatar(mUser);
			if (avatar != null) {
				publishProgress(new Object[]{1, avatar});
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			int value = (int) values[0];
			switch (value) {
				case 0:
					// Show user name
					mName.setText(mUser.getName());
					break;
				case 1:
					// Show avatar
					mAvatar.setImageBitmap((Bitmap) values[1]);
					break;
			}
			super.onProgressUpdate(values);
		}
		
	}
}
