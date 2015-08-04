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
import android.view.View;

import info.papdt.blacklight.api.comments.NewCommentApi;
import info.papdt.blacklight.model.CommentModel;
import info.papdt.blacklight.ui.statuses.NewPostActivity;

public class ReplyToActivity extends NewPostActivity
{
	private CommentModel mComment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPic.setVisibility(View.GONE);

		// Arguments
		mComment = getIntent().getParcelableExtra("comment");
	}

	@Override
	protected boolean post() {
		return NewCommentApi.replyTo(mComment.status.id, mComment.id, mText.getText().toString(), false);
	}

	@Override
	protected boolean needCache() {
		//不需要记忆为草稿
		return false;
	}
}
