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

package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.ui.statuses.NewPostActivity;

public class CommentOnActivity extends NewPostActivity
{
	private MessageModel mMsg;
	
	private MenuItem mCommentOrig;
	private MenuItem mRepost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Arguments
		mMsg = getIntent().getParcelableExtra("msg");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		// Disable picture uploading, because comments cannot contain pictures
		menu.findItem(R.id.post_pic).setVisible(false);
		
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
			PostApi.newRepost(mMsg.id, mText.getText().toString(), PostApi.EXTRA_NONE);
		}
		return NewCommentApi.commentOn(mMsg.id, mText.getText().toString(), mCommentOrig.isChecked());
	}
}
