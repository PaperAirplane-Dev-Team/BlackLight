/* 
 * Copyright (C) 2014 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.shandian.blacklight.ui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Build;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import java.util.concurrent.TimeUnit;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.comments.CommentTimeLineFragment;
import us.shandian.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import us.shandian.blacklight.ui.directmessage.DirectMessageUserFragment;
import us.shandian.blacklight.ui.entry.EntryActivity;
import us.shandian.blacklight.ui.favorites.FavListFragment;
import us.shandian.blacklight.ui.login.LoginActivity;
import us.shandian.blacklight.ui.search.SearchFragment;
import us.shandian.blacklight.ui.settings.SettingsActivity;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;
import us.shandian.blacklight.ui.statuses.MentionsTimeLineFragment;
import us.shandian.blacklight.ui.statuses.NewPostActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

/* Main Container Activity */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener
{
	private DrawerLayout mDrawer;
	private int mDrawerGravity;
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
	private Fragment[] mFragments = new Fragment[7];
	private FragmentManager mManager;
	
	// Temp fields
	private TextView mLastChoice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (hasSmartBar()) {
			getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Tint
		Utility.enableTint(this);

		// Detect if the user chose to use right-handed mode
		boolean rightHanded = Settings.getInstance(this).getBoolean(Settings.RIGHT_HANDED, false);
		mDrawerGravity = rightHanded ? Gravity.END : Gravity.START;

		// Set gravity
		View nav = findViewById(R.id.nav);
		DrawerLayout.LayoutParams p = (DrawerLayout.LayoutParams) nav.getLayoutParams();
		p.gravity = mDrawerGravity;
		nav.setLayoutParams(p);

		// Initialize naviagtion drawer
		mDrawer = (DrawerLayout) findViewById(R.id.drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, 0, 0) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().show();
				invalidateOptionsMenu();
				if (mLastChoice == null) {
					mLastChoice = (TextView) mMy.getChildAt(0);
					mLastChoice.getPaint().setFakeBoldText(true);
					mLastChoice.invalidate();
				}
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				invalidateOptionsMenu();
			}
		};
		mDrawer.setDrawerListener(mToggle);

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

		if (Build.VERSION.SDK_INT >= 19) {
			getActionBar().setDisplayUseLogoEnabled(false);
			getActionBar().setDisplayShowHomeEnabled(false);
		} else {
			getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		}

		// Fragments
		mFragments[0] = new HomeTimeLineFragment();
		mFragments[1] = new CommentTimeLineFragment();
		mFragments[2] = new FavListFragment();
		mFragments[3] = new DirectMessageUserFragment();
		mFragments[4] = new MentionsTimeLineFragment();
		mFragments[5] = new CommentMentionsTimeLineFragment();
		mFragments[6] = new SearchFragment();
		mManager = getFragmentManager();
		
		FragmentTransaction ft = mManager.beginTransaction();
		for (Fragment f : mFragments) {
			ft.add(R.id.container, f);
			ft.hide(f);
		}
		ft.commit();
		switchTo(0);
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

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		return true;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawer.isDrawerOpen(mDrawerGravity)) {
				mDrawer.closeDrawer(mDrawerGravity);
			} else {
				mDrawer.openDrawer(mDrawerGravity);
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		if ((parent != mOther || position == 0) && mLastChoice != null) {
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
							switchTo(position);
						} catch (Exception e) {
							
						}
					}
				}, 400);
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
							switchTo(4 + position);
						} catch (Exception e) {
							
						}
					}
				}, 400);
			}
		} else if (parent == mOther) {
			switch (position) {
				case 0:{
					view.postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								switchTo(6);
							} catch (Exception e) {

							}
						}
					}, 400);
					break;
				}
				case 1:{
					Intent i = new Intent();
					i.setAction(Intent.ACTION_MAIN);
					i.setClass(this, SettingsActivity.class);
					startActivity(i);
					break;
				}
				case 2:{
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
		
		mDrawer.closeDrawer(mDrawerGravity);
	}
	
	private void initList() {
		mLastChoice = null;
		mMy.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_item, getResources().getStringArray(R.array.my_array)));
		mAtMe.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_item, getResources().getStringArray(R.array.at_me_array)));
		mOther.setAdapter(new ArrayAdapter(this, R.layout.main_drawer_other_item, getResources().getStringArray(R.array.other_array)));
	}
	
	private void switchTo(int id) {
		FragmentTransaction ft = mManager.beginTransaction();
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
		
		for (int i = 0; i < mFragments.length; i++) {
			Fragment f = mFragments[i];
			
			if (f != null) {
				if (i != id) {
					ft.hide(f);
				} else {
					ft.show(f);
				}
			}
		}
		
		ft.commit();
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
			int value = Integer.parseInt(String.valueOf(values[0]));
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
