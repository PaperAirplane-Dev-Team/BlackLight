package us.shandian.blacklight.ui.statuses;

import android.view.Menu;
import android.os.Bundle;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.model.MessageModel;

public class RepostActivity extends NewPostActivity
{
	private MessageModel mMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the original msg
		mMsg = getIntent().getParcelableExtra("msg");
		
		// If the msg itself is a repost
		// We should repost the original one
		if (mMsg.retweeted_status != null) {
			mText.setText("//" + (mMsg.user != null ? "@" + mMsg.user.getName() + ":" : "") + mMsg.text);
			mMsg = mMsg.retweeted_status;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.post_pic).setVisible(false);
		return true;
	}

	@Override
	protected boolean post() {
		return PostApi.newRepost(mMsg.id, mText.getText().toString());
	}
}
