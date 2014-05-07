package us.shandian.blacklight.support.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.text.Html;

import us.shandian.blacklight.R;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.StatusTimeUtils;

public class WeiboAdapter extends BaseAdapter
{
	private MessageListModel mList;
	private LayoutInflater mInflater;
	private StatusTimeUtils mTimeUtils;
	
	public WeiboAdapter(Context context, MessageListModel list) {
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTimeUtils = StatusTimeUtils.instance(context);
	}
	
	@Override
	public int getCount() {
		return mList.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mList.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			MessageModel msg = mList.get(position);
			View v = mInflater.inflate(R.layout.weibo, null);
			v.setTag(msg);
			TextView name = (TextView) v.findViewById(R.id.weibo_name);
			TextView date = (TextView) v.findViewById(R.id.weibo_date);
			TextView from = (TextView) v.findViewById(R.id.weibo_from);
			TextView retweet = (TextView) v.findViewById(R.id.weibo_retweet);
			TextView comments = (TextView) v.findViewById(R.id.weibo_comments);
			TextView content = (TextView) v.findViewById(R.id.weibo_content);
			name.setText(msg.user.screen_name);
			date.setText(mTimeUtils.buildTimeString(msg.created_at));
			from.setText(Html.fromHtml(msg.source).toString());
			retweet.setText(String.valueOf(msg.reposts_count));
			comments.setText(String.valueOf(msg.comments_count));
			content.setText(msg.text); // TODO Spannable String , Emoticons
			
			// TODO Async Task to download pictures and avatars
			return v;
		}
	}

}
