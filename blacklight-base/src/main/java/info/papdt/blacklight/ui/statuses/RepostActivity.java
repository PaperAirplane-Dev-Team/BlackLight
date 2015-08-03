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

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.statuses.PostApi;
import info.papdt.blacklight.model.MessageModel;

import static info.papdt.blacklight.support.Utility.hasSmartBar;

public class RepostActivity extends NewPostActivity
{
	private MessageModel mMsg;
	
	private MenuItem mComment;
	private MenuItem mCommentOrig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (hasSmartBar()) {
			getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		}

		super.onCreate(savedInstanceState);

		mPic.setVisibility(View.GONE);

		// Get the original msg
		mMsg = getIntent().getParcelableExtra("msg");
		
		// If the msg itself is a repost
		// We should repost the original one
		if (mMsg.retweeted_status != null) {
			mText.setText("//" + (mMsg.user != null ? "@" + mMsg.user.getNameNoRemark() + ":" : "") + mMsg.text);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		mComment = menu.add(R.string.repost_and_comment);
		mComment.setCheckable(true);
		mComment.setChecked(false);
		mComment.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		mCommentOrig = menu.add(R.string.repost_and_comment_the_original);
		mCommentOrig.setCheckable(true);
		mCommentOrig.setChecked(false);
		mCommentOrig.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mCommentOrig.setEnabled(mMsg.retweeted_status != null);
		
		return true;
	}

	@Override
	protected boolean post() {
		int extra = PostApi.EXTRA_NONE;
		
		if (mComment.isChecked() && mCommentOrig.isChecked()) {
			extra = PostApi.EXTRA_ALL;
		} else if (mComment.isChecked()) {
			extra = PostApi.EXTRA_COMMENT;
		} else if (mCommentOrig.isChecked()) {
			extra = PostApi.EXTRA_COMMENT_ORIG;
		}
		
		return PostApi.newRepost(mMsg.id, mText.getText().toString(), extra, mVersion);
	}

	@Override
	public void send() {
		if (TextUtils.isEmpty(mText.getText())) {
			mText.setText(R.string.repost);
		}
		
		super.send();
	}

	@Override
	protected boolean needCache() {
		//转发微博不需要记忆为草稿
		return false;
	}
}
