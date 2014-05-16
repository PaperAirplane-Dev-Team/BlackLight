package us.shandian.blacklight.api.statuses;

import com.google.gson.Gson;
import com.sina.weibo.sdk.net.WeiboParameters;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageListModel;

/* Fetches messages published by an exact user */
public class UserTimeLineApi extends BaseApi
{
	public static MessageListModel fetchUserTimeLine(String uid, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("uid", uid);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = BMRequest(Constants.USER_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			return null;
		}
	}
}
