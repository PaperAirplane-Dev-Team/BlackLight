/*
 * Copyright (C) 2015 Peter Cai
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

package info.papdt.blacklight.support.adapter;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.attitudes.AttitudesApi;
import info.papdt.blacklight.api.comments.NewCommentApi;
import info.papdt.blacklight.api.statuses.PostApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.cache.user.UserApiCache;
import info.papdt.blacklight.model.CommentModel;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.HackyMovementMethod;
import info.papdt.blacklight.support.Settings;
import info.papdt.blacklight.support.SpannableStringUtils;
import info.papdt.blacklight.support.StatusTimeUtils;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.support.Binded;
import info.papdt.blacklight.ui.comments.CommentOnActivity;
import info.papdt.blacklight.ui.comments.ReplyToActivity;
import info.papdt.blacklight.ui.common.ImageActivity;
import info.papdt.blacklight.ui.statuses.RepostActivity;
import info.papdt.blacklight.ui.statuses.SingleActivity;
import info.papdt.blacklight.ui.statuses.UserTimeLineActivity;

import static info.papdt.blacklight.receiver.ConnectivityReceiver.isWIFI;

/*
  This is a common adapter for all kinds of weibo data (Statuses / Comments)
  Adapting them to ListViews.
  They share one common layout
*/
public class WeiboAdapter extends HeaderViewAdapter<WeiboAdapter.ViewHolder> {
	private static final String TAG = WeiboAdapter.class.getSimpleName();

	private static final int TAG_MSG = R.id.weibo_content;
	private static final int TAG_ID = R.id.card;

	private static final View.OnClickListener sImageListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ActivityOptionsCompat o = ActivityOptionsCompat
				.makeSceneTransitionAnimation((Activity) v.getContext(), v, "model");
			MessageModel msg = (MessageModel) v.getTag(TAG_MSG);
			int id = Integer.parseInt(v.getTag(TAG_ID).toString());
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), ImageActivity.class);
			i.putExtra("model", msg);
			i.putExtra("defaultId", id);
			ActivityCompat.startActivity((Activity) v.getContext(), i, o.toBundle());
		}
	};

	private MessageListModel mList;
	private MessageListModel mClone;
	private LayoutInflater mInflater;
	private StatusTimeUtils mTimeUtils;
	private UserApiCache mUserApi;
	private HomeTimeLineApiCache mHomeApi;
	private LoginApiCache mLogin;

	private String mUid;

	private int mGray;

	private Context mContext;

	private boolean mBindOrig;
	private boolean mShowCommentStatus;
	private boolean mScrolling = false;
	private boolean mAutoNoPic = false;
	private String mAppName;

	private RecyclerView mRecycler;

	public WeiboAdapter(Context context, RecyclerView listView, MessageListModel list, boolean bindOrig, boolean showCommentStatus) {
		super(listView);
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTimeUtils = StatusTimeUtils.instance(context);
		mUserApi = new UserApiCache(context);
		mHomeApi = new HomeTimeLineApiCache(context);
		mLogin = new LoginApiCache(context);
		mGray = context.getResources().getColor(R.color.light_gray);
		mUid = mLogin.getUid();
		mContext = context;
		mBindOrig = bindOrig;
		mShowCommentStatus = showCommentStatus;
		mAutoNoPic = Settings.getInstance(context).getBoolean(Settings.AUTO_NOPIC, true);
		mAppName = context.getString(R.string.app_name);

		mRecycler = listView;

		notifyDataSetChangedAndClone();
	}

	public void notifyDataSetLoaded() {
		mListener.onScrollStateChanged(mRecycler, RecyclerView.SCROLL_STATE_IDLE);
	}

	@Override
	public int getCount() {
		return mClone.getSize();
	}

	@Override
	public int getViewType(int position) {
		MessageModel msg = mClone.get(position);

		if (msg instanceof CommentModel) {
			return mBindOrig ? 10 : 0;
		} else {
			int ret = 0;

			if (msg.retweeted_status != null && mBindOrig) {
				ret = 10;
				msg = msg.retweeted_status;
			}

			if (willLoadPic(msg)) {
				ret += msg.hasMultiplePictures() ? msg.pic_urls.size() : 1;
			}

			return ret;
		}
	}

	@Override
	public long getItemViewId(int position) {
		return mClone.get(position).id;
	}

	@Override
	public void doRecycleView(ViewHolder h) {
		h.avatar.setImageResource(R.color.gray);
		h.avatar.setTag(true);
		h.comment_and_retweet.setVisibility(View.VISIBLE);
		h.msg = null;
	}

	@Override
	public WeiboAdapter.ViewHolder doCreateViewHolder(ViewGroup parent, int viewType) {
		View v = mInflater.inflate(R.layout.weibo, parent, false);
		ViewHolder h = new ViewHolder(this, v);

		h.content.setMovementMethod(HackyMovementMethod.getInstance());
		h.orig_content.setMovementMethod(HackyMovementMethod.getInstance());

		if (viewType >= 10) {
			h.origin_parent.setVisibility(View.VISIBLE);
		}

		int picCount = viewType % 10;

		if (picCount > 0) {
			h.scroll.setVisibility(View.VISIBLE);
			h.pics.setVisibility(View.VISIBLE);
			for (int i = 0; i < 9; i++) {
				View view = h.pics.getChildAt(i);
				if (i < picCount) {
					view.setTag(TAG_ID, i);
					view.setOnClickListener(sImageListener);
				} else {
					view.setVisibility(View.GONE);
				}
			}
		}

		return h;
	}

	@Override
	public ViewHolder doCreateHeaderHolder(View header) {
		return new ViewHolder(header);
	}

	@Override
	public void doBindViewHolder(ViewHolder h, int position) {
		/*if (DEBUG) {
			Debug.startMethodTracing("TraceLog");
		}*/

		View v = h.v;
		final MessageModel msg = mClone.get(position);
		h.msg = msg;

		TextView name = h.name;
		TextView from = h.from;
		TextView content = h.content;
		TextView date = h.date;
		TextView attitudes = h.attitudes;
		TextView retweet = h.retweets;
		TextView comments = h.comments;

		name.setText(msg.user != null ? msg.user.getName() : "");

		String ver = "";
		if (msg.annotations.size() > 0 && !(ver = msg.annotations.get(0).bl_version).trim().equals("")) {
			// Show a fake tail for BL :)
			from.setText(mAppName);
		} else {
			from.setText(TextUtils.isEmpty(msg.source) ? "" : Utility.truncateSourceString(msg.source));
		}

		content.setText(SpannableStringUtils.getSpan(mContext, msg));

		date.setText(mTimeUtils.buildTimeString(msg.millis));

		if (!mShowCommentStatus || msg instanceof CommentModel) {
			h.comment_and_retweet.setVisibility(View.GONE);
		} else {
			attitudes.setText(Utility.addUnitToInt(mContext, msg.attitudes_count));
			retweet.setText(Utility.addUnitToInt(mContext, msg.reposts_count));
			comments.setText(Utility.addUnitToInt(mContext, msg.comments_count));
		}

		bindMultiPicLayout(h, msg, true);

		// If this retweets/replies to others, show the original
		if (mBindOrig) {
			if (!(msg instanceof CommentModel) && msg.retweeted_status != null) {
				bindOrig(h, msg.retweeted_status, true);
			} else if (msg instanceof CommentModel) {
				CommentModel comment = (CommentModel) msg;
				if (comment.reply_comment != null) {
					bindOrig(h, comment.reply_comment, false);
				} else if (comment.status != null) {
					bindOrig(h, comment.status, false);
				}
			}

		}

		if (msg.user != null) {
			// Load avatar
			Picasso.with(v.getContext())
				.load(msg.user.profile_image_url)
				.fit()
				.centerCrop()
				.into(h.avatar);
		}

		//new ImageDownloader().execute(v);

		/*if (DEBUG) {
			Debug.stopMethodTracing();
		}*/
	}

	private boolean willLoadPic(MessageModel msg){
		boolean hasPic = (msg.thumbnail_pic != null || msg.pic_urls.size() > 0);
		boolean preferToShow = !(mAutoNoPic && !isWIFI) || msg.inSingleActivity;
		return  hasPic && preferToShow;
	}

	private void bindOrig(ViewHolder h, MessageModel msg, boolean showPic) {
		h.orig_content.setText(SpannableStringUtils.getOrigSpan(mContext, msg));

		bindMultiPicLayout(h, msg, showPic);

		if (!(msg instanceof CommentModel)) {
			h.origin_parent.setTag(msg);
		} else {
			h.origin_parent.setTag(null);
		}
	}

	private void bindMultiPicLayout(ViewHolder h, MessageModel msg, boolean showPic) {
		if (showPic && h.getItemViewType() % 10 > 0) {
			LinearLayout container = h.pics;

			int numChilds = h.getItemViewType() % 10;

			for (int i = 0; i < numChilds; i++) {
				ImageView iv = (ImageView) container.getChildAt(i);

				String url = null;
				if (msg.hasMultiplePictures()) {
					url = msg.pic_urls.get(i).getThumbnail();
				} else if (i == 0) {
					url = msg.thumbnail_pic;
				}

				Picasso.with(iv.getContext())
					.load(url)
					.fit()
					.centerCrop()
					.into(iv);

				iv.setTag(TAG_MSG, msg);
			}
		}
	}

	void buildPopup(final ViewHolder h) {
		PopupMenu p = new PopupMenu(mContext, h.popup);
		p.inflate(R.menu.popup);
		final Menu m = p.getMenu();

		// Show needed items
		m.findItem(R.id.popup_copy).setVisible(true);
		if (h.msg instanceof CommentModel) {
			m.findItem(R.id.popup_reply).setVisible(true);

			CommentModel cmt = (CommentModel) h.msg;
			if (cmt.user.id.equals(mUid) || (cmt.status != null && cmt.status.user != null && cmt.status.user.id.equals(mUid))) {
				m.findItem(R.id.popup_delete).setVisible(true);
			}
		} else {
			m.findItem(R.id.popup_repost).setVisible(true);
			m.findItem(R.id.popup_comment).setVisible(true);

			if (h.msg.liked) {
				m.findItem(R.id.popup_unlike).setVisible(true);
			} else {
				m.findItem(R.id.popup_like).setVisible(true);
			}

			if (h.msg.user != null && h.msg.user.id.equals(mUid)) {
				m.findItem(R.id.popup_delete).setVisible(true);
			}
		}

		p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				if (id == R.id.popup_delete) {
					h.delete();
				} else if (id == R.id.popup_copy) {
					h.copy();
				} else if (id == R.id.popup_comment || id == R.id.popup_reply) {
					h.reply();
				} else if (id == R.id.popup_repost) {
					h.repost();
				} else if (id == R.id.popup_like || id == R.id.popup_unlike) {
					new LikeTask().execute(h.msg, h, m);
				}

				return true;
			}
		});

		// Pop up!
		p.show();
	}

	@Override
	public void notifyDataSetChangedAndClone() {
		mClone = mList.clone();
		super.notifyDataSetChanged();

		try {
			mListener.onScrollStateChanged(mRecycler, RecyclerView.SCROLL_STATE_IDLE);
		} catch (Exception e) {

		}
	}

	private boolean waitUntilNotScrolling(ViewHolder h, MessageModel msg) {
		while (mScrolling) {
			if (h.msg != msg) {
				return false;
			}

			try {
				Thread.sleep(200);
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	private static class DeleteTask extends AsyncTask<MessageModel, Void, Void> {
		private ProgressDialog prog;
		private Context context;

		public DeleteTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(context);
			prog.setMessage(context.getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}

		@Override
		protected Void doInBackground(MessageModel[] params) {
			if (params[0] instanceof CommentModel) {
				NewCommentApi.deleteComment(params[0].id);
			} else {
				PostApi.deletePost(params[0].id);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();
		}
	}

	private class LikeTask extends AsyncTask<Object, Void, Boolean>{
		private MessageModel mm;
		private ViewHolder h;
		private Menu m;

		@Override
		protected Boolean doInBackground(Object... params) {
			mm = (MessageModel) params[0];
			h = (ViewHolder) params[1];
			m = (Menu) params[2];
			if (mm.liked){
				return AttitudesApi.cancelLike(mm.id);
			}else{
				return AttitudesApi.like(mm.id);
			}
		}

		@Override
		protected void onPostExecute(Boolean result){
			if (result && mm == h.msg){
				if(mm.liked){
					-- mm.attitudes_count;
				}else{
					++ mm.attitudes_count;
				}
				mm.liked = !mm.liked;

				h.attitudes.setText(String.valueOf(mm.attitudes_count));

				m.findItem(R.id.popup_unlike).setVisible(mm.liked);
				m.findItem(R.id.popup_like).setVisible(!mm.liked);
			}
		}
	}

	public static class ViewHolder extends HeaderViewAdapter.ViewHolder {
		public boolean sub = false;

		public TextView date;
		public TextView retweets;
		public TextView comments;
		public TextView name;
		public TextView from;
		public TextView content;
		public TextView attitudes;
		public TextView orig_content;
		public ImageView avatar;
		public ImageView popup;
		public HorizontalScrollView scroll;
		public LinearLayout pics;
		public View card;
		public View origin_parent;
		public View comment_and_retweet;

		public View v;
		public MessageModel msg = null;
		public Context context;
		public WeiboAdapter adapter;

		public ViewHolder(View v) {
			super(v);
			isHeader = true;
		}

		public ViewHolder(WeiboAdapter adapter, View v) {
			super(v);
			this.v = v;
			this.context = v.getContext();
			this.adapter = adapter;

			v.setTag(this);

			init();
		}

		private void init() {
			// Views
			date = Utility.findViewById(v, R.id.weibo_date);
			retweets = Utility.findViewById(v, R.id.weibo_retweet);
			comments = Utility.findViewById(v, R.id.weibo_comments);
			name = Utility.findViewById(v, R.id.weibo_name);
			from = Utility.findViewById(v, R.id.weibo_from);
			content = Utility.findViewById(v, R.id.weibo_content);
			attitudes = Utility.findViewById(v, R.id.weibo_attitudes);
			orig_content = Utility.findViewById(v, R.id.weibo_orig_content);
			avatar = Utility.findViewById(v, R.id.weibo_avatar);
			popup = Utility.findViewById(v, R.id.weibo_popup);
			scroll = Utility.findViewById(v, R.id.weibo_pics_scroll);
			pics = Utility.findViewById(v, R.id.weibo_pics);
			card = Utility.findViewById(v, R.id.card);
			origin_parent = Utility.findViewById(v, R.id.weibo_origin);
			comment_and_retweet = Utility.findViewById(v, R.id.weibo_comment_and_retweet);

			// Events
			Utility.bindOnClick(this, popup, "popup");
			Utility.bindOnClick(this, avatar, "showUser");
			Utility.bindOnClick(this, card, "show");
			Utility.bindOnClick(this, origin_parent, "showOrig");
		}

		@Binded
		void popup() {
			adapter.buildPopup(this);
		}

		@Binded
		void showUser() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(v.getContext(), UserTimeLineActivity.class);
			i.putExtra("user", msg.user);
			context.startActivity(i);
		}

		@Binded
		void show() {
			if (!(msg instanceof CommentModel)){
				if(msg.inSingleActivity){
					return;
				}
			}
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);

			if (msg instanceof CommentModel) {
				i.setClass(context, ReplyToActivity.class);
				i.putExtra("comment", (CommentModel) msg);
			} else {
				i.setClass(context, SingleActivity.class);
				i.putExtra("msg", msg);
			}

			ActivityOptionsCompat o =
				ActivityOptionsCompat.makeSceneTransitionAnimation(
					(Activity) context, v, "msg");

			ActivityCompat.startActivity((Activity) context, i, o.toBundle());
		}

		@Binded
		void showOrig() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setClass(context, SingleActivity.class);

			if (!(msg instanceof CommentModel)) {
				if (msg.retweeted_status != null) {
					i.putExtra("msg", msg.retweeted_status);
				}
			} else {
				i.putExtra("msg", ((CommentModel) msg).status);
			}

			ActivityOptionsCompat o =
				ActivityOptionsCompat.makeSceneTransitionAnimation(
				(Activity) context, origin_parent, "msg");

			ActivityCompat.startActivity((Activity) context, i, o.toBundle());
		}

		void repost() {
			if(!(msg instanceof CommentModel)) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.setClass(context, RepostActivity.class);
				i.putExtra("msg", msg);
				context.startActivity(i);
			}
		}

		void copy() {
			Utility.copyToClipboard(context, msg.text);
		}

		void reply() {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);

			if (msg instanceof CommentModel) {
				i.setClass(context, ReplyToActivity.class);
				i.putExtra("comment", (CommentModel) msg);
			} else {
				i.setClass(context, CommentOnActivity.class);
				i.putExtra("msg", msg);
			}

			context.startActivity(i);
		}

		void delete() {
			new AlertDialog.Builder(context)
				.setMessage(R.string.confirm_delete)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						new DeleteTask(context).execute(msg);
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				})
				.show();
		}

	}

}
