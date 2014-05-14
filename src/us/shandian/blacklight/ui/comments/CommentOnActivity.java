package us.shandian.blacklight.ui.comments;

import android.view.Menu;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.comments.NewCommentApi;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.ui.statuses.NewPostActivity;

public class CommentOnActivity extends NewPostActivity
{
	private MessageModel mMsg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Arguments
		mMsg = getIntent().getParcelableExtra("msg");
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
		return NewCommentApi.commentOn(mMsg.id, mText.getText().toString());
	}
}
