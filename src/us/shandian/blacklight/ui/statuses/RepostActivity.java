package us.shandian.blacklight.ui.statuses;

import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.model.MessageModel;

public class RepostActivity extends NewPostActivity
{
	private MessageModel mMsg;
	
	private MenuItem mComment;
	private MenuItem mCommentOrig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the original msg
		mMsg = getIntent().getParcelableExtra("msg");
		
		// If the msg itself is a repost
		// We should repost the original one
		if (mMsg.retweeted_status != null) {
			mText.setText("//" + (mMsg.user != null ? "@" + mMsg.user.getName() + ":" : "") + mMsg.text);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.post_pic).setVisible(false);
		
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
		
		return PostApi.newRepost(mMsg.id, mText.getText().toString(), extra);
	}
}
