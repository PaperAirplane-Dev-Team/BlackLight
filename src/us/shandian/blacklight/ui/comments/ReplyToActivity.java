package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.model.CommentModel;
import us.shandian.blacklight.ui.statuses.NewPostActivity;

public class ReplyToActivity extends NewPostActivity
{
	private CommentModel mComment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Arguments
		mComment = getIntent().getParcelableExtra("comment");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Disable picture uploading, because comments cannot contain pictures
		menu.findItem(R.id.post_pic).setVisible(false);

		return true;
	}

	@Override
	protected boolean post() {
		return NewCommentApi.replyTo(mComment.status.id, mComment.id, mText.getText().toString());
	}
}
