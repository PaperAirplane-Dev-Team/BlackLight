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

package us.shandian.blacklight.ui.statuses;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.friendships.FriendsApi;
import us.shandian.blacklight.api.friendships.GroupsApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.GroupModel;
import us.shandian.blacklight.model.GroupListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.common.AbsActivity;
import us.shandian.blacklight.ui.common.GenerousSlidingUpPanelLayout;
import us.shandian.blacklight.ui.directmessage.DirectMessageConversationActivity;
import us.shandian.blacklight.ui.friendships.FriendsActivity;

import static us.shandian.blacklight.support.Utility.hasSmartBar;

public class UserTimeLineActivity extends AbsActivity
{
	private UserTimeLineFragment mFragment;
	private UserModel mModel;
	
	@InjectView(R.id.user_name) TextView mName;
	@InjectView(R.id.user_follow_state) TextView mFollowState;
	@InjectView(R.id.user_des) TextView mDes;
	@InjectView(R.id.user_followers) TextView mFollowers;
	@InjectView(R.id.user_following) TextView mFollowing;
	@InjectView(R.id.user_msgs) TextView mMsgs;
	@InjectView(R.id.user_like) TextView mLikes;
	@InjectView(R.id.user_geo) TextView mGeo;
	@InjectView(R.id.user_avatar) ImageView mAvatar;
	@InjectView(R.id.user_cover) View mCover;
	@InjectView(R.id.user_following_container) View mFollowingContainer;
	@InjectView(R.id.iv_collapse) ImageView mCollapse;
	
	@InjectView(R.id.user_slide) GenerousSlidingUpPanelLayout mSlide;
	
	private MenuItem mMenuFollow;
	private MenuItem mMenuGroup;
	
	private UserApiCache mCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        if (hasSmartBar()) {
            getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        }

        super.onCreate(savedInstanceState);
		setContentView(R.layout.user_timeline_activity);

		mCache = new UserApiCache(this);

		// Arguments
		mModel = getIntent().getParcelableExtra("user");
		
		// Inject
		ButterKnife.inject(this);
		
		// Init PanelSlideListener
		mSlide.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener(){

			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				Utility.setActionBarTranslation(UserTimeLineActivity.this, mSlide.getCurrentParalaxOffset());
			}

			@Override
			public void onPanelCollapsed(View panel) {
				mCollapse.setRotation(180);
				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_180);
				mCollapse.startAnimation(animation);
			}

			@Override
			public void onPanelExpanded(View panel) {
				mCollapse.setRotation(0);
				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_180);
				mCollapse.startAnimation(animation);
			}

			@Override
			public void onPanelAnchored(View panel) {
				
			}
			
		});
		
		// View values
		mName.setText(mModel.getName());
		
		// Follower state (following/followed/each other)
		resetFollowState();
		
		// Also view values
		mDes.setText(mModel.description);
		mFollowers.setText(String.valueOf(mModel.followers_count));
		mFollowing.setText(String.valueOf(mModel.friends_count));
		mMsgs.setText(String.valueOf(mModel.statuses_count));
		mLikes.setText(String.valueOf(mModel.favourites_count));
		mGeo.setText(mModel.location);
		
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
				mSlide.setPanelHeight(slideHeight - containerHeight - 20);
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
		if (new LoginApiCache(this).getUid().equals(mModel.id)) {
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
			new Follower().execute();
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

	@OnClick(R.id.user_following_container)
	public void viewFriends() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.putExtra("uid", mModel.id);
		i.setClass(this, FriendsActivity.class);
		startActivity(i);
	}
	
	private void resetFollowState() {
		if (mModel.follow_me && mModel.following) {
			mFollowState.setText(R.string.following_each_other);
		} else if (mModel.follow_me) {
			mFollowState.setText(R.string.following_me);
		} else if (mModel.following) {
			mFollowState.setText(R.string.i_am_following);
		} else {
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
		protected Void doInBackground(Void... params) {
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
		protected Void doInBackground(Object... params) {
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
		protected Void doInBackground(Void... params) {
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
		protected Void doInBackground(Void... params) {
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
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			
			switch (Integer.parseInt(String.valueOf(values[0]))) {
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
