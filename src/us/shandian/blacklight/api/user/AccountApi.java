package us.shandian.blacklight.api.user;

import org.json.JSONObject;

import com.sina.weibo.sdk.net.WeiboParameters;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;

/* Current Account Api of Sina Weibo */
public class AccountApi extends BaseApi
{
	public static String getUid() {
		try {
			JSONObject json = request(Constants.GET_UID, new WeiboParameters(), HTTP_GET);
			return json.optString("uid");
		} catch (Exception e) {
			return null;
		}
	}
}
