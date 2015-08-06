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

package info.papdt.blacklight.ui.statuses;

import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.friendships.FriendsApi;
import info.papdt.blacklight.api.friendships.GroupsApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.GroupListModel;
import info.papdt.blacklight.model.GroupModel;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.GenerousSlidingUpPanelLayout;
import info.papdt.blacklight.ui.directmessage.DirectMessageConversationActivity;
import info.papdt.blacklight.ui.friendships.FriendsActivity;

public class UserTimeLineActivity extends AbsActivity
{
	private UserTimeLineFragment mFragment;
	private UserModel mModel;

	private TextView mFollowState;
	private ImageView mFollowImg;
	private TextView mDes;
	private ScrollView mDesScroll;
	private TextView mFollowers;
	private TextView mFollowing;
	private TextView mMsgs;
	private ImageView mAvatar;
	private ImageView mCover;
	private View mFollowingContainer;
	private View mFollowersContainer;
	private LinearLayout mLayoutFollowState;
	private GenerousSlidingUpPanelLayout mSlide;

	private MenuItem mMenuFollow;
	private MenuItem mMenuGroup;

	private UserApiCache mCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Arguments
		mModel = getIntent().getParcelableExtra("user");

		if (!Utility.isDarkMode(this) && mModel.isMale() != UserApiCache.amIMale()) {
			setTheme(R.style.BL_Theme_Purple);
		}

		mLayout = R.layout.user_timeline_activity;
		super.onCreate(savedInstanceState);

		mCache = new UserApiCache(this);

		// Initialize views
		mFollowState = Utility.findViewById(this, R.id.user_follow_state);
		mFollowImg = Utility.findViewById(this, R.id.user_follow_img);
		mDes = Utility.findViewById(this, R.id.user_des);
		mDesScroll = Utility.findViewById(this, R.id.user_des_scroll);
		mFollowers = Utility.findViewById(this, R.id.user_followers);
		mFollowing = Utility.findViewById(this, R.id.user_following);
		mMsgs = Utility.findViewById(this, R.id.user_msgs);
		mAvatar = Utility.findViewById(this, R.id.user_avatar);
		mCover = Utility.findViewById(this, R.id.user_cover);
		mFollowingContainer = Utility.findViewById(this, R.id.user_following_container);
		mFollowersContainer = Utility.findViewById(this, R.id.user_followers_container);
		mLayoutFollowState = Utility.findViewById(this, R.id.user_follow);
		mSlide = Utility.findViewById(this, R.id.user_slide);

		View info = Utility.findViewById(this, R.id.user_info_button);
		View dim = Utility.findViewById(this, R.id.user_dim);

		// Bind onClick events
		Utility.bindOnClick(this, mLayoutFollowState, "follow");
		Utility.bindOnClick(this, mFollowingContainer, "viewFriends");
		Utility.bindOnClick(this, mFollowersContainer, "viewFollowers");
		Utility.bindOnClick(this, info, dim, "showOrHideInfo");

		getSupportActionBar().setTitle(mModel.name);

		// Follower state (following/followed/each other)
		resetFollowState();
		if (mModel != null && mModel.id.equals((new UserApiCache(this).getUser( (new LoginApiCache(this).getUid()) ).id))) {
			mLayoutFollowState.setVisibility(View.GONE);
		}

		// Also view values
		mDes.setText(mModel.description);
		mFollowers.setText(Utility.addUnitToInt(this, mModel.followers_count));
		mFollowing.setText(Utility.addUnitToInt(this, mModel.friends_count));
		mMsgs.setText(Utility.addUnitToInt(this, mModel.statuses_count));

		// This way can support API 15.
		Typeface mTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Condensed.ttf");
		mFollowers.setTypeface(mTypeface);
		mFollowing.setTypeface(mTypeface);
		mMsgs.setTypeface(mTypeface);
		mFollowState.setTypeface(mTypeface);

		new Downloader().execute();

		mFragment = new UserTimeLineFragment(mModel.id);
		getFragmentManager().beginTransaction().replace(R.id.user_timeline_container, mFragment).commit();

		// Change panel height when measured
		final View container = findViewById(R.id.user_container);
		ViewTreeObserver vto = container.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				int containerHeight = container.getMeasuredHeight();
				int slideHeight = mSlide.getMeasuredHeight();
				mSlide.setPanelHeight((int) (slideHeight - containerHeight + Utility.dp2px(UserTimeLineActivity.this, 20.0f)));
				mSlide.setChildListView(mFragment.getList());
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user, menu);
		mMenuFollow = menu.findItem(R.id.follow);
		mMenuGroup = menu.findItem(R.id.group);
		if (mModel != null && new LoginApiCache(this).getUid().equals(mModel.id)) {
			mMenuFollow.setVisible(false);
			mMenuGroup.setVisible(false);
			menu.findItem(R.id.send_dm).setVisible(false);
		} else {
			resetFollowState();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.follow) {
			follow();
			return true;
		} else if (id == R.id.send_dm) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(this, DirectMessageConversationActivity.class);
			i.putExtra("user", mModel);
			startActivity(i);
			return true;
		} else if (id == R.id.group) {
			new GroupLister().execute();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Binded
	public void viewFriends() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.putExtra("uid", mModel.id);
		i.putExtra("isFriends", true);
		i.setClass(this, FriendsActivity.class);
		startActivity(i);
	}
	@Binded
	public void viewFollowers() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.putExtra("uid", mModel.id);
		i.putExtra("isFriends", false);
		i.setClass(this, FriendsActivity.class);
		startActivity(i);
	}

	@Binded
	public void follow() {
		new Follower().execute();
	}

	@Binded
	public void showOrHideInfo() {
		mDesScroll.clearAnimation();

		AlphaAnimation anim = null;

		final int start = mDesScroll.getVisibility();

		if (start == View.VISIBLE) {
			mDesScroll.setAlpha(0.5f);
			anim = new AlphaAnimation(0.5f, 0.0f);
		} else {
			mDesScroll.setAlpha(0.5f);
			mDesScroll.setVisibility(View.VISIBLE);
			anim = new AlphaAnimation(0.0f, 0.5f);
		}

		anim.setDuration(400);
		anim.setFillAfter(true);
		anim.setFillBefore(false);

		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation anim) {
				mDesScroll.clearAnimation();
				if (start == View.VISIBLE) {
					mDesScroll.setVisibility(View.GONE);
				} else {
					mDesScroll.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationStart(Animation anim) {
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
			}
		});

		mDesScroll.setAnimation(anim);
		anim.start();
	}

	private void resetFollowState() {
		if (mModel == null) return;

		if (mModel.follow_me && mModel.following) {
			mFollowImg.setImageResource(R.drawable.ic_arrow);
			mFollowState.setText(R.string.following_each_other);
		} else if (mModel.follow_me) {
			mFollowImg.setImageResource(R.drawable.ic_action_new);
			mFollowState.setText(R.string.following_me);
		} else if (mModel.following) {
			mFollowImg.setImageResource(R.drawable.ic_checkmark);
			mFollowState.setText(R.string.i_am_following);
		} else {
			mFollowImg.setImageResource(R.drawable.ic_action_new);
			mFollowState.setText(R.string.no_following);
		}

		if (mMenuFollow != null) {
			mMenuFollow.setIcon(mModel.following ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
			mMenuFollow.setTitle(getString(mModel.following ? R.string.unfollow : R.string.follow));
			mMenuGroup.setEnabled(mModel.following);
		}
	}

	private class GroupLister extends AsyncTask<Void, Void, Void> {
		private ProgressDialog prog;
		private GroupModel[] groups;
		private String[] titles;
		private boolean[] checked;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(UserTimeLineActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(Void ... params) {
			GroupListModel groupList = GroupsApi.getGroups();
			groups = new GroupModel[groupList.getSize()];
			titles = new String[groupList.getSize()];
			checked = new boolean[groupList.getSize()];

			for (int i = 0; i < groupList.getSize(); i++) {
				GroupModel group = groupList.get(i);
				groups[i] = group;
				titles[i] = group.name;
				checked[i] = GroupsApi.isMember(mModel.id, group.idstr);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();

			// New dialog
			new AlertDialog.Builder(UserTimeLineActivity.this)
			.setTitle(getResources().getString(R.string.change_group))
			.setMultiChoiceItems(titles, checked, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					new GroupChanger().execute(groups, checked);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
		}
	}

	private class GroupChanger extends AsyncTask<Object, Void, Void> {
		private ProgressDialog prog;

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(UserTimeLineActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(Object ... params) {
			GroupModel[] groups = (GroupModel[]) params[0];
			boolean[] checked = (boolean[]) params[1];

			for (int i = 0; i < groups.length; i++) {
				if (checked[i]) {
					GroupsApi.addMemberToGroup(mModel.id, groups[i].idstr);
				} else {
					GroupsApi.removeMemberFromGroup(mModel.id, groups[i].idstr);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();
		}

	}

	private class Follower extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void ... params) {
			if (mModel.following) {
				FriendsApi.unfollow(mModel.id);
			} else {
				FriendsApi.follow(mModel.id);
			}

			mModel.following = !mModel.following;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			resetFollowState();
		}
	}

	private class Downloader extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void ... params) {
			// Avatar
			Bitmap avatar = mCache.getLargeAvatar(mModel);
			publishProgress(new Object[] {0, avatar});

			// Cover
			if (!mModel.getCover().trim().equals("")) {
				Bitmap cover = mCache.getCover(mModel);
				if (cover != null) {
					publishProgress(new Object[] {1, cover});
				}
			}

			// Refresh state
			mModel = mCache.getUser(mModel.id);
			publishProgress(2);

			return null;
		}

		@Override
		protected void onProgressUpdate(Object ... values) {
			super.onProgressUpdate(values);

			switch (Integer.parseInt(String.valueOf(values[0]))) {
			case 0:
				if (mAvatar != null) {
					mAvatar.setImageBitmap((Bitmap) values[1]);
				}
				break;
			case 1:
				if (mCover != null) {
					mCover.setImageBitmap((Bitmap) values[1]);
				}
				break;
			case 2:
				resetFollowState();
				break;
			}
		}
	}
}
