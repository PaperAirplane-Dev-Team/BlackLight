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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.friendships.GroupsApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.GroupListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Emoticons;
import us.shandian.blacklight.support.LogF;
import us.shandian.blacklight.support.Settings;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.comments.CommentTimeLineFragment;
import us.shandian.blacklight.ui.common.FloatingActionButton;
import us.shandian.blacklight.ui.common.ToolbarActivity;
import us.shandian.blacklight.ui.directmessage.DirectMessageUserFragment;
import us.shandian.blacklight.ui.favorites.FavListFragment;
import us.shandian.blacklight.ui.search.SearchFragment;
import us.shandian.blacklight.ui.settings.SettingsActivity;
import us.shandian.blacklight.ui.statuses.HomeTimeLineFragment;
import us.shandian.blacklight.ui.statuses.MentionsFragment;
import us.shandian.blacklight.ui.statuses.NewPostActivity;
import us.shandian.blacklight.ui.statuses.UserTimeLineActivity;

import us.shandian.blacklight.ui.statuses.TimeLineFragment;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

/* Main Container Activity */
public class MainActivity extends ToolbarActivity implements ActionBar.OnNavigationListener, View.OnClickListener, View.OnLongClickListener
{

	public static interface Refresher {
		void doRefresh();
	}

	public static final int HOME = 0,COMMENT = 1,FAV = 2,DM = 3, MENTION = 4, SEARCH = 5;

	private static final String BILATERAL = "bilateral";

	private DrawerLayout mDrawer;
	private int mDrawerGravity;
	private ActionBarDrawerToggle mToggle;

	// Drawer content
	private View mDrawerWrapper;
	private ScrollView mDrawerScroll;
	private TextView mName;
	private ImageView mAvatar;
	private ImageView mCover;
	private FloatingActionButton mFAB;
	
	private LoginApiCache mLoginCache;
	private UserApiCache mUserCache;
	private UserModel mUser;
	
	// Fragments
	private Fragment[] mFragments = new Fragment[6];
	private FragmentManager mManager;

	// Groups
	public GroupListModel mGroups;
	public String mCurrentGroupId = null;
	private MenuItem mGroupDestroy, mGroupCreate;
	
	// Temp fields
	private int mCurrent = 0;
	private int mNext = 0;
	private boolean mIgnore = false;
	private int mLang = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLang = Utility.getCurrentLanguage(this);
		if (mLang > -1) {
			Utility.changeLanguage(this, mLang);
		}

		Utility.initDarkMode(this);
		mLayout = R.layout.main;

		super.onCreate(savedInstanceState);

		// Inflate custom decor
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customDecor = inflater.inflate(R.layout.decor, null);

		// Replace the original layout
		// Learned from SlidingMenu
		ViewGroup decor = (ViewGroup) getWindow().getDecorView();
		ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
		decor.removeView(decorChild);
		decor.addView(customDecor);
		((ViewGroup) customDecor.findViewById(R.id.decor_container)).addView(decorChild);

		// Add custom view
		if (Build.VERSION.SDK_INT < 21) {
			// This fix is only for ICS/JB/KK
			ContextThemeWrapper customContext = new ContextThemeWrapper(this, R.style.Theme_AppCompat);
			LayoutInflater customInflater = (LayoutInflater) customContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View custom = customInflater.inflate(R.layout.action_custom, null);
			getSupportActionBar().setCustomView(custom);
		} else {
			getSupportActionBar().setCustomView(R.layout.action_custom);
		}
		
		getSupportActionBar().setDisplayShowCustomEnabled(false);

		// Initialize views
		mDrawer = Utility.findViewById(this, R.id.drawer);
		mDrawerWrapper = Utility.findViewById(this, R.id.drawer_wrapper);
		mDrawerScroll = Utility.findViewById(this, R.id.drawer_scroll);
		mName = Utility.findViewById(this, R.id.my_name);
		mAvatar = Utility.findViewById(this, R.id.my_avatar);
		mCover = Utility.findViewById(this, R.id.my_cover);
		
		View me = Utility.findViewById(this, R.id.my_account);
		View home = Utility.findViewById(this, R.id.drawer_home);
		View at = Utility.findViewById(this, R.id.drawer_at);
		View cmt = Utility.findViewById(this, R.id.drawer_comment);
		View dm = Utility.findViewById(this, R.id.drawer_dm);
		View fav = Utility.findViewById(this, R.id.drawer_fav);
		View set = Utility.findViewById(this, R.id.drawer_settings);
		
		// bind events
		Utility.bindOnClick(this, me, "showMe");
		Utility.bindOnClick(this, home, "home");
		Utility.bindOnClick(this, at, "mentions");
		Utility.bindOnClick(this, cmt, "comments");
		Utility.bindOnClick(this, dm, "dm");
		Utility.bindOnClick(this, fav, "fav");
		Utility.bindOnClick(this, set, "settings");
		
		// Detect if the user chose to use right-handed mode
		boolean rightHanded = Settings.getInstance(this).getBoolean(Settings.RIGHT_HANDED, false);

		mDrawerGravity = rightHanded ? Gravity.RIGHT : Gravity.LEFT;

		// Set gravity
		View nav = findViewById(R.id.nav);
		DrawerLayout.LayoutParams p = (DrawerLayout.LayoutParams) nav.getLayoutParams();
		p.gravity = mDrawerGravity;
		nav.setLayoutParams(p);

		// Adjust Padding for statusbar and navigation bar
		if (!Utility.isChrome()) {
			nav.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);
		}

		// Initialize naviagtion drawer
		//mDrawer = (DrawerLayout) findViewById(R.id.drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, 0, 0) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
				hideFAB();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();

				if (mCurrent != DM) {
					showFAB();
				}
			}
		};
		mToggle.setDrawerIndicatorEnabled(true);
		mDrawer.setDrawerListener(mToggle);

		if (mDrawerGravity == Gravity.LEFT) {
			mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
		}

		// My account
		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new InitializerTask().execute();
		new GroupsTask().execute();

		// Initialize FAB
		mFAB = new FloatingActionButton.Builder(this)
			.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
			.withPaddings(10, 10, 10, 10)
			.withDrawable(Utility.getFABNewIcon(this))
			.withButtonColor(Utility.getFABBackground(this))
			.withButtonSize(90)
			.create();
		mFAB.setOnClickListener(this);
		mFAB.setOnLongClickListener(this);
		
		// Initialize ActionBar Style
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		
		//mTitle = Utility.addActionViewToCustom(this, Utility.action_bar_title, mAction);

		//getActionBar().setDisplayShowTitleEnabled(false);

		// Ignore first spinner event
		mIgnore = true;

		// Fragments
		mFragments[HOME] = new HomeTimeLineFragment();
		mFragments[COMMENT] = new CommentTimeLineFragment();
		mFragments[FAV] = new FavListFragment();
		mFragments[DM] = new DirectMessageUserFragment();
		mFragments[MENTION] = new MentionsFragment();
		mFragments[SEARCH] = new SearchFragment();
		mManager = getFragmentManager();
		
		FragmentTransaction ft = mManager.beginTransaction();
		for (Fragment f : mFragments) {
			ft.add(R.id.container, f);
			ft.hide(f);
		}
		ft.commit();

		// Adjust drawer layout params
		mDrawerWrapper.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mDrawerScroll.getMeasuredHeight() > mDrawerWrapper.getMeasuredHeight()) {
					// On poor screens, we add a scroll over the drawer content
					ViewGroup.LayoutParams lp = mDrawerScroll.getLayoutParams();
					lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
					mDrawerScroll.setLayoutParams(lp);
				}
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		mToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		mToggle.syncState();
	}

	@Override
	protected void onResume(){
		super.onResume();
		
		// Dirty fix strange focus
		findViewById(R.id.main_root).requestFocus();

		int lang = Utility.getCurrentLanguage(this);
		if (lang != mLang) {
			recreate();
		}

		Intent i = getIntent();

		if (i == null) return;

		int page = getIntent().getIntExtra(Intent.EXTRA_INTENT,HOME);
		if (page == HOME){
			switchTo(HOME);
		}else{
			setShowTitle(true);
			setShowSpinner(false);
			switchAndRefresh(page);
		}

		setIntent(null);
	}

	@Override
	protected void onNewIntent(Intent i){
		setIntent(i);
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		// This is a dummy method
		// To fix duplicate menu and fragments
		// After a restart
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			mLoginCache = new LoginApiCache(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		// Save some needed items
		mGroupDestroy = menu.findItem(R.id.group_destroy);
		mGroupCreate = menu.findItem(R.id.group_create);

		mGroupDestroy.setEnabled(mCurrentGroupId != null);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);

		if (mCurrent == 0) {
			mGroupDestroy.setVisible(true);
			mGroupCreate.setVisible(true);

			mGroupDestroy.setEnabled(mCurrentGroupId != null);
		} else {
			mGroupDestroy.setVisible(false);
			mGroupCreate.setVisible(false);
		}

		return true;
	}

	public void showMe() {
		if (mUser != null) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, UserTimeLineActivity.class);
			i.putExtra("user", mUser);
			startActivity(i);
		}
	}

	public void openOrCloseDrawer() {
		if (mDrawer.isDrawerOpen(mDrawerGravity)) {
			mDrawer.closeDrawer(mDrawerGravity);
		} else {
			mDrawer.openDrawer(mDrawerGravity);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.switch_theme) {
			Utility.switchTheme(this);

			// This will re-create the whole activity
			recreate();
			
			return true;
		} else if (item.getItemId() == R.id.group_destroy) {
			new AlertDialog.Builder(this)
				.setMessage(R.string.confirm_delete)
				.setCancelable(true)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new GroupDeleteTask().execute();
					}
				})
				.show();
			return true;
		} else if (item.getItemId() == R.id.group_create) {
			final EditText text = new EditText(this);
			new AlertDialog.Builder(this)
				.setTitle(R.string.group_create)
				.setCancelable(true)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new GroupCreateTask().execute(text.getText().toString().trim());
					}
				})
				.setView(text)
				.show();
			return true;
		} else if (item.getItemId() == R.id.search) {
			setShowTitle(false);
			setShowSpinner(false);
			switchTo(SEARCH);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (mCurrent != HOME) {
			home();
		} else {
			super.onBackPressed();
		}
	}

	public void home() {
		setShowTitle(false);
		setShowSpinner(true);
		switchTo(HOME);
	}

	public void comments() {
		switchTo(COMMENT);
		setShowTitle(true);
		setShowSpinner(false);
	}

	public void dm() {
		switchTo(DM);
		setShowTitle(true);
		setShowSpinner(false);
	}

	public void fav() {
		switchTo(FAV);
		setShowTitle(true);
		setShowSpinner(false);
	}

	public void settings() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, SettingsActivity.class);
		startActivity(i);
	}

	public void mentions() {
		switchTo(MENTION);
		setShowTitle(true);
		setShowSpinner(false);
	}

	@Override
	public boolean onNavigationItemSelected(int id, long itemId) {
		if (mIgnore) {
			mIgnore = false;
			return false;
		}

		if (id == 0) {
			mCurrentGroupId = null;
		} else if (id == 1){
			mCurrentGroupId = BILATERAL;
		} else {
			mCurrentGroupId = mGroups.get(id - 2).idstr;
		}

		Settings.getInstance(this).putString(Settings.CURRENT_GROUP, mCurrentGroupId);
		
		((HomeTimeLineFragment) mFragments[0]).doRefresh();

		return true;
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, NewPostActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onLongClick(View v) {
		Fragment f = mFragments[mCurrent];

		if (f instanceof Refresher) {
			((Refresher) f).doRefresh();
		}

		return true;
	}

	public void hideFAB() {
		mFAB.hideFloatingActionButton();
	}

	public void showFAB() {
		mFAB.showFloatingActionButton();
	}

	private void switchAndRefresh(int id){
		if (id != 0){
			SwipeRefreshLayout.OnRefreshListener l = (SwipeRefreshLayout.OnRefreshListener)mFragments[id];
			l.onRefresh();
		}
		switchTo(id);
	}

	private void setShowTitle(boolean show) {
		getSupportActionBar().setDisplayShowTitleEnabled(show);
	}

	public void setShowSpinner(boolean show) {
		getSupportActionBar().setNavigationMode(show ? ActionBar.NAVIGATION_MODE_LIST : ActionBar.NAVIGATION_MODE_STANDARD);
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

		mCurrent = id;
		mNext = id;

		mDrawer.closeDrawer(mDrawerGravity);
	}

	private void updateActionSpinner() {
		// Current Group
		mCurrentGroupId = Settings.getInstance(MainActivity.this).getString(Settings.CURRENT_GROUP, null);
		LogF.d(this.getLocalClassName(), "current group id:%s",mCurrentGroupId);
		int curId = 0;

		if (mCurrentGroupId != null) {
			if (mCurrentGroupId.equals(BILATERAL)){
				curId = 1;
			} else {
				for (int i = 0; i < mGroups.getSize(); i++) {
					if (mGroups.get(i).idstr.equals(mCurrentGroupId)) {
						curId = i + 2;
					}
				}
			}
		}

		if (curId == 0) {
			mCurrentGroupId = null;
		}

		getSupportActionBar().setSelectedNavigationItem(curId);

	}
	
	private class InitializerTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void[] params) {
			// Username first
			mUser = mUserCache.getUser(mLoginCache.getUid());
			publishProgress(new Object[]{0});
			
			// My avatar
			Bitmap avatar = mUserCache.getLargeAvatar(mUser);
			if (avatar != null) {
				publishProgress(new Object[]{1, avatar});
			}

			// My Cover
			Bitmap cover = mUserCache.getCover(mUser);
			if (cover != null) {
				publishProgress(new Object[]{2, cover});
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			int value = Integer.parseInt(String.valueOf(values[0]));
			switch (value) {
				case 0:
					// Show user name
					mName.setText(mUser != null ? mUser.getName() : "");
					break;
				case 1:
					// Show avatar
					mAvatar.setImageBitmap((Bitmap) values[1]);
					break;
				case 2:
					// Show cover
					mCover.setImageBitmap((Bitmap) values[1]);
					break;
			}
			super.onProgressUpdate(values);
		}
	}

	private class GroupDeleteTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(MainActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			GroupsApi.destroyGroup(mCurrentGroupId);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			new GroupsTask().execute();
			prog.dismiss();
			onNavigationItemSelected(0, 0);
		}
	}

	private class GroupCreateTask extends AsyncTask<String, Void, Void> {
		ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(MainActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			GroupsApi.createGroup(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			new GroupsTask().execute();
			prog.dismiss();
		}
	}

	private class GroupsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			mGroups = GroupsApi.getGroups();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mGroups != null && mGroups.getSize() > 0) {
				// Get the name list
				String[] names = new String[mGroups.getSize() + 2];

				names[0] = getResources().getString(R.string.group_all);
				names[1] = getString(R.string.group_bilateral);
				for (int i = 0; i < mGroups.getSize(); i++) {
					names[i + 2] = mGroups.get(i).name;
				}

				// Navigation
				getSupportActionBar().setListNavigationCallbacks(new ArrayAdapter<String>(MainActivity.this, 
							R.layout.action_spinner_item, names), MainActivity.this);

				getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

				if (mCurrent == 0) {
					mIgnore = true;
					setShowTitle(false);
				}

				updateActionSpinner();
				
				if (mCurrent != 0) {
					Log.d("Spinner", "Will now hide the spinner");
					setShowSpinner(false);
				}
			}
		}

	}
}
