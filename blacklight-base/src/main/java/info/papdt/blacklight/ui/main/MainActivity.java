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

import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
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

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.List;
import java.util.Random;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.GroupsApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.search.SearchHistoryCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Emoticons;
import info.papdt.blacklight.support.LogF;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.comments.CommentTimeLineFragment;
import info.papdt.blacklight.ui.comments.CommentMentionsTimeLineFragment;
import info.papdt.blacklight.ui.common.FloatingActionButton;
import info.papdt.blacklight.ui.common.SlidingTabLayout;
import info.papdt.blacklight.ui.common.SlidingTabStrip;
import info.papdt.blacklight.ui.common.ToolbarActivity;
import info.papdt.blacklight.ui.directmessage.DirectMessageUserFragment;
import info.papdt.blacklight.ui.favorites.FavListFragment;
import info.papdt.blacklight.ui.login.LoginActivity;
import info.papdt.blacklight.ui.search.SearchActivity;
import info.papdt.blacklight.ui.settings.SettingsActivity;
import info.papdt.blacklight.ui.statuses.HomeTimeLineFragment;
import info.papdt.blacklight.ui.statuses.MentionsTimeLineFragment;
import info.papdt.blacklight.ui.statuses.NewPostActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;
import static info.papdt.blacklight.BuildConfig.DEBUG;

/* Main Container Activity */
public class MainActivity extends ToolbarActivity implements View.OnClickListener, View.OnLongClickListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	public static interface Refresher {
		void doRefresh();
		void goToTop();
	}

	public static interface HeaderProvider {
		float getHeaderFactor();
	}

	public static final int REQUEST_LOGIN = 2333;
	public static final int HOME = 0,
							COMMENT = 1,
							MENTION = 2,
							MENTION_CMT = 3,
							DM = 4,
							FAV = 5;

	private static final String BILATERAL = "bilateral";

	private DrawerLayout mDrawer;
	private int mDrawerGravity;
	private ActionBarDrawerToggle mToggle;
	private SearchBox mSearchBox;
	private SearchHistoryCache mSearchHistory;

	// Drawer content
	private View mDrawerWrapper;
	private TextView mName;
	private View mAccountSwitch, mAccountSwitchIcon;
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
	private GroupFragment mGroupFragment = new GroupFragment();
	private MultiUserFragment mMultiUserFragment = new MultiUserFragment();

	// Actions
	private View mSetting, mMultiUser;

	// Pager
	private ViewPager mPager;
	private SlidingTabLayout mTabs;
	private SlidingTabLayout mToolbarTabs;
	private View mTabsWrapper;
	private int mHeaderHeight = 0, mWrapperHeight = 0;

	private View mShadow;
	private View mTopWrapper, mToolbarWrapper;

	// Groups
	public GroupListModel mGroups;
	public String mCurrentGroupId = null;
	private MenuItem mGroupDestroy, mGroupCreate, mSearch;

	// Temp fields
	private int mCurrent = 0;
	private boolean mIgnore = false;
	private int mLang = -1;

	// false = Group, true = MultiUser
	private boolean mDrawerState = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLang = Utility.getCurrentLanguage(this);
		if (mLang > -1) {
			Utility.changeLanguage(this, mLang);
		}

		Utility.initDarkMode(this);
		mLayout = R.layout.main;

		super.onCreate(savedInstanceState);

		// Initialize views
		mDrawer = Utility.findViewById(this, R.id.drawer);
		mDrawerWrapper = Utility.findViewById(this, R.id.drawer_wrapper);
		mName = Utility.findViewById(this, R.id.account_name);
		mAvatar = Utility.findViewById(this, R.id.my_avatar);
		mCover = Utility.findViewById(this, R.id.my_cover);
		mPager = Utility.findViewById(this, R.id.main_pager);
		mTabs = Utility.findViewById(this, R.id.main_tabs);
		mTabsWrapper = Utility.findViewById(this, R.id.main_tab_wrapper);
		mToolbarTabs = Utility.findViewById(this, R.id.top_tab);
		mToolbarWrapper = Utility.findViewById(this, R.id.toolbar_wrapper);
		mTopWrapper = Utility.findViewById(this, R.id.top_wrapper);
		mShadow = Utility.findViewById(this, R.id.action_shadow);
		mSetting = Utility.findViewById(this, R.id.drawer_settings);
		mMultiUser = Utility.findViewById(this, R.id.drawer_multiuser);
		mAccountSwitch = Utility.findViewById(this, R.id.account_switch);
		mAccountSwitchIcon = Utility.findViewById(this, R.id.account_switch_icon);
		mSearchBox = Utility.findViewById(this, R.id.main_search);

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

		// Search Box
		mSearchHistory = new SearchHistoryCache(this);
		mSearchBox.setLogoText(getString(R.string.search));
		mSearchBox.setSearchListener(new SearchBox.SearchListener() {
			@Override
			public void onSearchOpened() {
			}

			@Override
			public void onSearchCleared() {

			}

			@Override
			public void onSearchClosed() {
				mSearchBox.hideCircularly(MainActivity.this);
			}

			@Override
			public void onSearchTermChanged() {

			}

			@Override
			public void onSearch(String result) {
				mSearchHistory.addHistory(result);
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.setClass(MainActivity.this, SearchActivity.class);
				i.putExtra("keyword", result);
				startActivity(i);
			}
		});

		// Initialize toolbar custom view
		mTopWrapper.setAlpha(0f);
		final Drawable[] pageIcons = new Drawable[] {
			getResources().getDrawable(R.drawable.ic_drawer_home),
			getResources().getDrawable(R.drawable.ic_drawer_comment),
			getResources().getDrawable(R.drawable.ic_drawer_at),
			getResources().getDrawable(R.drawable.ic_drawer_at),
			getResources().getDrawable(R.drawable.ic_drawer_pm),
			getResources().getDrawable(R.drawable.ic_drawer_fav)
		};

		mToolbarTabs.setIconAdapter(new SlidingTabLayout.TabIconAdapter() {
			@Override
			public Drawable getIcon(int position) {
				return pageIcons[position];
			}
		});
		mToolbarTabs.setViewPager(mPager, mTabs);

		// Prepare listener to be set later
		final ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (position + 1 >= mFragments.length)
					return;

				Fragment cur = mFragments[position];
				Fragment next = mFragments[position + 1];

				float factorCur = 0, factorNext = 0;

				if (cur instanceof HeaderProvider) {
					factorCur = ((HeaderProvider) cur).getHeaderFactor();
				}

				if (next instanceof HeaderProvider) {
					factorNext = ((HeaderProvider) next).getHeaderFactor();
				}

				float factor = factorCur + positionOffset * (factorNext - factorCur);
				updateHeaderTranslation(factor);
			}

			@Override
			public void onPageSelected(int pos) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		};

		final int color = getResources().getColor(R.color.white);
		SlidingTabStrip.SimpleTabColorizer colorizer = new SlidingTabStrip.SimpleTabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return color;
			}

			@Override
			public int getSelectedTitleColor(int position) {
				return color;
			}
		};
		mTabs.setCustomTabColorizer(colorizer);
		mTabs.notifyIndicatorColorChanged();
		mToolbarTabs.setCustomTabColorizer(colorizer);
		mToolbarTabs.notifyIndicatorColorChanged();

		if (Build.VERSION.SDK_INT >= 21) {
			mToolbar.setElevation(0);
			//findViewById(R.id.main_tab_wrapper).setElevation(getToolbarElevation());
		} else {
			mShadow.setAlpha(0);
		}

		// Detect if the user chose to use right-handed mode
		boolean rightHanded = Settings.getInstance(this).getBoolean(Settings.RIGHT_HANDED, false);

		mDrawerGravity = rightHanded ? Gravity.RIGHT : Gravity.LEFT;

		//set GroupFragmentCallBack
		GroupFragment.setGfCallBack(new GroupFragment.GFCallBack() {
			@Override
			public void onItemClick() {
				openOrCloseDrawer();
			}
		});

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
		initUserAccount();
		//new GroupsTask().execute();

		// Initialize FAB
		mFAB = new FloatingActionButton.Builder(this)
			.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
			.withPaddings(0, 0, 16, 16)
			.withDrawable(Utility.getFABNewIcon(this))
			.withButtonColor(Utility.getFABBackground(this))
			.withButtonSize(56 + 16)
			.create();
		mFAB.setOnClickListener(this);
		mFAB.setOnLongClickListener(this);

		// Bind
		Utility.bindOnClick(this, mSetting, "settings");
		Utility.bindOnClick(this, mAccountSwitch, "drawerSwitch");
		Utility.bindOnClick(this, mMultiUser, "muser");
		Utility.bindOnClick(this, mCover, "showMe");

		// Initialize ActionBar Style
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		// Drawer Groups
		getFragmentManager().beginTransaction()
			.add(R.id.drawer_group, mGroupFragment)
			.add(R.id.drawer_group, mMultiUserFragment)
			.show(mGroupFragment)
			.hide(mMultiUserFragment)
			.commit();

		updateSplashes();

		// Ignore first spinner event
		mIgnore = true;

		mToolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateSplashes();
			}
		});

		// Adjust drawer layout params
		mDrawerWrapper.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {

				mHeaderHeight = mTabs.getHeight() + 10;
				mWrapperHeight = mTabsWrapper.getHeight();

				if (DEBUG) {
					Log.d(TAG, "Global layout. Wrapper height: " + mWrapperHeight);
				}

				mToolbarTabs.setOnPageChangeListener(pageListener);
				mToolbarTabs.setTabIconSize(mToolbar.getHeight());

				if (mWrapperHeight > 0)
					mDrawerWrapper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		MultiUserFragment.setMuCallBack(new MultiUserFragment.MuCallBack() {
			@Override
			public void syncAccount() {
				initUserAccount();
				((HomeTimeLineFragment) mFragments[0]).doRefresh();
				drawerSwitch();
				mMultiUserFragment.reload();
			}

			@Override
			public void closeDrawer() {
				openOrCloseDrawer();
			}
		});
	}

	private void initUserAccount() {
		mLoginCache = new LoginApiCache(this);
		mUserCache = new UserApiCache(this);
		new InitializerTask().execute();
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

		int page = getIntent().getIntExtra(Intent.EXTRA_INTENT, 0);
		mPager.setCurrentItem(page);

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

		if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
			mMultiUserFragment.reload();
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

		mGroupDestroy.setVisible(true);
		mGroupCreate.setVisible(true);

		mGroupDestroy.setEnabled(mCurrentGroupId != null && !mCurrentGroupId.equals(BILATERAL));

		return true;
	}

	// Can't be more annoying. WHY DOES SEARCHBOX NEED ANOTHER METHOD?
	// WHY NOT A LISTENER??? WHY!!!???
	public void mic(View view) {
		// Is causing crashes. Abandon it.
		//mSearchBox.micClick(this);
		mSearchBox.setSearchString("");
	}

	public void updateHeaderTranslation(float factor) {

		if (mWrapperHeight == 0) return; // DO NOT allow call to this if the height is not measured yet.

		mTopWrapper.setAlpha(factor);
		mToolbar.setAlpha(1 - factor);
		mTabs.setAlpha(1 - factor);

		if (factor >= 0.5f) {
			mTopWrapper.bringToFront();
		} else {
			mToolbar.bringToFront();
		}

		ViewGroup.LayoutParams params = mTabsWrapper.getLayoutParams();
		params.height = (int) (mWrapperHeight * (1 - factor));

		if (params.height < 0)
			params.height = 0;

		if (DEBUG) {
			Log.d(TAG, "Wrapper height: " + mWrapperHeight);
			Log.d(TAG, "Header factor: " + factor);
			Log.d(TAG, "Header height: " + params.height);
		}

		mTabsWrapper.setLayoutParams(params);

		if (Build.VERSION.SDK_INT >= 21) {
			mToolbarWrapper.setElevation(factor * getToolbarElevation());
		} else {
			mShadow.setAlpha(factor);
		}
	}

	public void updateSplashes() {
		// 梗
		String[] splashes = getResources().getStringArray(R.array.title_splashes);
		getSupportActionBar().setTitle(splashes[new Random().nextInt(splashes.length)]);

		// 梗 in search box
		if (Math.random() > 0.6) { // Make this a matter of possibility
			splashes = getResources().getStringArray(R.array.splashes);
			mSearchBox.setLogoText(splashes[new Random().nextInt(splashes.length)]);
		} else {
			mSearchBox.setLogoText("");
		}
	}

	// For fragments to pass events
	public View getTabsView() {
		return mTabs;
	}

	public ViewPager getViewPager() {
		return mPager;
	}

	@Binded
	public void showMe() {
		openOrCloseDrawer();
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
			mSearchBox.clearSearchable();
			List<String> history = mSearchHistory.getHistory();
			for (String keyword : history) {
				mSearchBox.addSearchable(new SearchResult(keyword, getResources().getDrawable(R.drawable.ic_history)));
			}
			updateSplashes();
			mSearchBox.revealFromMenuItem(R.id.search, this);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if(mDrawer.isDrawerOpen(mDrawerGravity)){
			mDrawer.closeDrawer(mDrawerGravity);
		} else {
			super.onBackPressed();
		}
	}

	public int getHeaderHeight() {
		return mHeaderHeight;
	}

	@Binded
	public void settings() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, SettingsActivity.class);
		startActivity(i);
		openOrCloseDrawer();
	}

	@Binded
	public void drawerSwitch() {
		mAccountSwitchIcon.startAnimation(AnimationUtils.loadAnimation(this, !mDrawerState ? R.anim.rotate_180 : R.anim.rotate_180_reverse));

		if (!mDrawerState) {
			mSetting.setVisibility(View.GONE);
			mMultiUser.setVisibility(View.VISIBLE);
			getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
				.hide(mGroupFragment).show(mMultiUserFragment).commit();
		} else {
			mSetting.setVisibility(View.VISIBLE);
			mMultiUser.setVisibility(View.GONE);
			getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
				.hide(mMultiUserFragment).show(mGroupFragment).commit();
		}

		mDrawerState = !mDrawerState;
	}

	@Binded
	public void muser() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, LoginActivity.class);
		i.putExtra("multi", true);
		startActivityForResult(i, REQUEST_LOGIN);
	}

	public void setCurrentGroup(String group, boolean refresh) {
		mCurrentGroupId = group;

		if (refresh)
			((HomeTimeLineFragment) mFragments[0]).doRefresh();
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

	public void setShowSpinner(boolean show) {
		getSupportActionBar().setNavigationMode(show ? ActionBar.NAVIGATION_MODE_LIST : ActionBar.NAVIGATION_MODE_STANDARD);
	}

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
			UserApiCache.setAmIMale(mUser.isMale());

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
			mGroupFragment.reload();
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
			prog.dismiss();
			mGroupFragment.reload();
		}
	}
}
