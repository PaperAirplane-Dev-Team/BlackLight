/* 
 * Copyright (C) 2015 Peter Cai
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

package info.papdt.blacklight.ui.main;

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
import android.widget.Toast;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v13.app.FragmentStatePagerAdapter;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.GroupsApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Emoticons;
import info.papdt.blacklight.support.LogF;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.comments.CommentTimeLineFragment;
import info.papdt.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import info.papdt.blacklight.ui.common.FloatingActionButton;
import info.papdt.blacklight.ui.common.SlidingTabLayout;
import info.papdt.blacklight.ui.common.SlidingTabStrip;
import info.papdt.blacklight.ui.common.ToolbarActivity;
import info.papdt.blacklight.ui.directmessage.DirectMessageUserFragment;
import info.papdt.blacklight.ui.favorites.FavListFragment;
import info.papdt.blacklight.ui.search.SearchFragment;
import info.papdt.blacklight.ui.settings.SettingsActivity;
import info.papdt.blacklight.ui.statuses.HomeTimeLineFragment;
import info.papdt.blacklight.ui.statuses.MentionsTimeLineFragment;
import info.papdt.blacklight.ui.statuses.NewPostActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

/* Main Container Activity */
public class MainActivity extends ToolbarActivity implements View.OnClickListener, View.OnLongClickListener
{

	public static interface Refresher {
		void doRefresh();
		void goToTop();
	}

	public static final int HOME = 0,COMMENT = 1,FAV = 2,DM = 3, MENTION = 4, SEARCH = 5;

	private static final String BILATERAL = "bilateral";

	private DrawerLayout mDrawer;
	private int mDrawerGravity;
	private ActionBarDrawerToggle mToggle;
	private ContextThemeWrapper mToolbarContext;

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
	private Fragment[] mFragments = new Fragment[]{
		new HomeTimeLineFragment(),
		new CommentTimeLineFragment(),
		new MentionsTimeLineFragment(),
		new CommentMentionsTimeLineFragment(),
		new DirectMessageUserFragment(),
		new FavListFragment()
	};
	private FragmentManager mManager;
	
	// Pager
	private ViewPager mPager;
	private SlidingTabLayout mTabs;
	private int mHeaderHeight = 0;

	// Groups
	public GroupListModel mGroups;
	public String mCurrentGroupId = null;
	private MenuItem mGroupDestroy, mGroupCreate, mSearch;
	
	// Temp fields
	private int mCurrent = 0;
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

		// Add custom view
		mToolbarContext = new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Dark_ActionBar);
		LayoutInflater customInflater = (LayoutInflater) mToolbarContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View custom = customInflater.inflate(R.layout.action_custom, null);
		getSupportActionBar().setCustomView(custom);
		
		getSupportActionBar().setDisplayShowCustomEnabled(false);

		// Initialize views
		mDrawer = Utility.findViewById(this, R.id.drawer);
		mDrawerWrapper = Utility.findViewById(this, R.id.drawer_wrapper);
		mDrawerScroll = Utility.findViewById(this, R.id.drawer_scroll);
		mName = Utility.findViewById(this, R.id.my_name);
		mAvatar = Utility.findViewById(this, R.id.my_avatar);
		mCover = Utility.findViewById(this, R.id.my_cover);
		mPager = Utility.findViewById(this, R.id.main_pager);
		mTabs = Utility.findViewById(this, R.id.main_tabs);
		
		final String[] pages = getResources().getStringArray(R.array.main_tabs);
		mPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
			@Override
			public int getCount() {
				return pages.length;
			}

			@Override
			public Fragment getItem(int position) {
				
				return mFragments[position];
			}
			
			@Override
			public CharSequence getPageTitle(int position) {
				return pages[position];
			}
		});
		mPager.setOffscreenPageLimit(pages.length);
		mTabs.setViewPager(mPager);
		
		final int color = getResources().getColor(R.color.white);
		mTabs.setCustomTabColorizer(new SlidingTabStrip.SimpleTabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return color;
			}
			
			@Override
			public int getSelectedTitleColor(int position) {
				return color;
			}
		});
		mTabs.notifyIndicatorColorChanged();
		
		if (Build.VERSION.SDK_INT > 0) {
			mToolbar.setElevation(0);
			//findViewById(R.id.main_tab_wrapper).setElevation(getToolbarElevation());
		}
		
		// Detect if the user chose to use right-handed mode
		boolean rightHanded = Settings.getInstance(this).getBoolean(Settings.RIGHT_HANDED, false);

		mDrawerGravity = rightHanded ? Gravity.RIGHT : Gravity.LEFT;
		
		// Set gravity
		View nav = findViewById(R.id.nav);
		DrawerLayout.LayoutParams p = (DrawerLayout.LayoutParams) nav.getLayoutParams();
		p.gravity = mDrawerGravity;
		nav.setLayoutParams(p);
		
		// Semi-transparent statusbar over drawer
		if (Build.VERSION.SDK_INT >= 21) {
			mDrawer.setStatusBarBackgroundColor(Utility.getColorPrimaryDark(this));
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

		// Use system shadow for Lollipop but fall back for pre-L
		if (Build.VERSION.SDK_INT >= 21) {
			nav.setElevation(10.0f);
		} else if (mDrawerGravity == Gravity.LEFT) {
			mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
		}

		// My account
		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new InitializerTask().execute();
		//new GroupsTask().execute();

		// Initialize FAB
		mFAB = new FloatingActionButton.Builder(this)
			.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
			.withPaddings(16, 16, 16, 16)
			.withDrawable(Utility.getFABNewIcon(this))
			.withButtonColor(Utility.getFABBackground(this))
			.withButtonSize(100)
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
		/*mFragments[HOME] = new HomeTimeLineFragment();
		mFragments[COMMENT] = new CommentTimeLineFragment();
		mFragments[FAV] = new FavListFragment();
		mFragments[DM] = new DirectMessageUserFragment();
		mFragments[MENTION] = new MentionsFragment();
		mFragments[SEARCH] = new SearchFragment();*/
		/*mManager = getFragmentManager();
		
		FragmentTransaction ft = mManager.beginTransaction();
		for (Fragment f : mFragments) {
			ft.add(R.id.container, f);
			ft.hide(f);
		}
		ft.commit();*/
		
		mToolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment f = mFragments[mCurrent];
				
				if (f instanceof Refresher) {
					((Refresher) f).goToTop();
				}
			}
		});

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
				
				mHeaderHeight = mTabs.getHeight() + 10;
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
		
		// Override the click event of ActionBarDrawerToggle to avoid crash in right handed mode
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openOrCloseDrawer();
			}
		});
	}

	@Override
	protected void onResume(){
		super.onResume();
		
		// Dirty fix strange focus
		//findViewById(R.id.container).requestFocus();

		int lang = Utility.getCurrentLanguage(this);
		if (lang != mLang) {
			recreate();
		}

		Intent i = getIntent();

		if (i == null) return;

		int page = getIntent().getIntExtra(Intent.EXTRA_INTENT,HOME);
		if (page == HOME){
			//switchTo(HOME);
		}else{
			setShowTitle(true);
			setShowSpinner(false);
			//switchAndRefresh(page);
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
		mSearch = menu.findItem(R.id.search);
		mSearch.setVisible(true);
		
		mGroupDestroy.setEnabled(mCurrentGroupId != null);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);

		if (mCurrent == HOME) {
			mGroupDestroy.setVisible(true);
			mGroupCreate.setVisible(true);

			mGroupDestroy.setEnabled(mCurrentGroupId != null);
		} else {
			mGroupDestroy.setVisible(false);
			mGroupCreate.setVisible(false);
		}
		
		if (mCurrent == SEARCH) {
			mSearch.setVisible(false);
		} else {
			mSearch.setVisible(true);
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
			mSearch = item;
			setShowTitle(false);
			setShowSpinner(false);
			//switchTo(SEARCH);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
        if(mDrawer.isDrawerOpen(mDrawerGravity)){
            mDrawer.closeDrawer(mDrawerGravity);
        }
		else if (mCurrent != HOME) {
			//home();
		} else {
			super.onBackPressed();
		}
	}
	
	public int getHeaderHeight() {
		return mHeaderHeight;
	}

	/*public void home() {
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
	}*/

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

	/*private void switchAndRefresh(int id){
		if (id != 0){
			SwipeRefreshLayout.OnRefreshListener l = (SwipeRefreshLayout.OnRefreshListener)mFragments[id];
			l.onRefresh();
		}
		switchTo(id);
	}*/

	private void setShowTitle(boolean show) {
		getSupportActionBar().setDisplayShowTitleEnabled(show);
	}

	public void setShowSpinner(boolean show) {
		getSupportActionBar().setNavigationMode(show ? ActionBar.NAVIGATION_MODE_LIST : ActionBar.NAVIGATION_MODE_STANDARD);
	}
	
	/*private void switchTo(int id) {
		
		if (mSearch != null) {
			mSearch.setVisible(id != SEARCH);
		}
		
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
		int mNext = id;

		mDrawer.closeDrawer(mDrawerGravity);
	}*/

	/*private void updateActionSpinner() {
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

	}*/
	
	private class InitializerTask extends AsyncTask<Void, Object, Void> {

		@Override
		protected void onPreExecute() {
			if (Utility.isUidBanned(MainActivity.this, mLoginCache.getUid())) {
				// Sorry for doing this
				// But we have no idea how to stop this user from spamming
				Toast.makeText(MainActivity.this.getApplicationContext(), R.string.enough, Toast.LENGTH_LONG).show();
				getWindow().getDecorView().postDelayed(new Runnable() {
					@Override
					public void run() {
						throw new RuntimeException("WTF");
					}
				}, Toast.LENGTH_LONG);
			}
		}
		
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
			//new GroupsTask().execute();
			prog.dismiss();
			//onNavigationItemSelected(0, 0);
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
			//new GroupsTask().execute();
			prog.dismiss();
		}
	}

	/*private class GroupsTask extends AsyncTask<Void, Void, Void> {
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
				getSupportActionBar().setListNavigationCallbacks(new ArrayAdapter<String>(mToolbarContext, 
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

	}*/
}
