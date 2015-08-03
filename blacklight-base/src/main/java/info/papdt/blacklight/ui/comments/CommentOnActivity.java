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

package info.papdt.blacklight.ui.comments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.comments.NewCommentApi;
import info.papdt.blacklight.api.statuses.PostApi;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.ui.statuses.NewPostActivity;

public class CommentOnActivity extends NewPostActivity
{
	private MessageModel mMsg;
	
	private MenuItem mCommentOrig;
	private MenuItem mRepost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPic.setVisibility(View.GONE);

		// Arguments
		mMsg = getIntent().getParcelableExtra("msg");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		// Other items
		mCommentOrig = menu.add(R.string.comment_orig);
		mCommentOrig.setCheckable(true);
		mCommentOrig.setChecked(false);
		mCommentOrig.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mCommentOrig.setEnabled(mMsg.retweeted_status != null);
		
		mRepost = menu.add(R.string.comment_and_repost);
		mRepost.setCheckable(true);
		mRepost.setChecked(false);
		mRepost.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		return true;
	}

	@Override
	protected boolean post() {
		if (mRepost.isChecked()) {
			PostApi.newRepost(mMsg.id, mText.getText().toString(), PostApi.EXTRA_NONE, mVersion);
		}
		return NewCommentApi.commentOn(mMsg.id, mText.getText().toString(), mCommentOrig.isChecked());
	}

	@Override
	protected boolean needCache() {
		//不需要记忆为草稿
		return false;
	}
}
