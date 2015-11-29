package info.papdt.blacklight.ui.statuses;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import info.papdt.blacklight.R;
import info.papdt.blacklight.model.UserModel;
import info.papdt.blacklight.support.StatusTimeUtils;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.common.AbsActivity;

public class UserProfileActivity extends AbsActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.user_profile;
		super.onCreate(savedInstanceState);

		StatusTimeUtils utils = StatusTimeUtils.instance(this);

		UserModel mModel = getIntent().getParcelableExtra("user");
		ListView listView = Utility.findViewById(this, R.id.user_profile_list);
		ArrayList<Item> itemList = new ArrayList<>();

		itemList.add(new Item(R.string.uid, mModel.id));
		itemList.add(new Item(R.string.gender, mModel.gender));
		itemList.add(new Item(R.string.location, mModel.location));
		itemList.add(new Item(R.string.created_at, utils.buildTimeString(mModel.created_at)));
		if (mModel.verified) {
			itemList.add(new Item(R.string.verified_reason, mModel.verified_reason));
		}

		SimpleAdapter adapter = new SimpleAdapter(this,
				itemList,
				R.layout.user_profile_item,
				new String[] {"user_profile_item_title", "user_profile_item_text"},
				new int[] {R.id.user_profile_item_title, R.id.user_profile_item_text});
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private class Item extends HashMap<String, String> {
		public Item(@StringRes int title, String text) {
			super();
			put("user_profile_item_title", getString(title));
			put("user_profile_item_text", text);
		}
	}
}
