package us.shandian.blacklight.api;

/* Constants for weibo api */

public class Constants
{
	public static final String SINA_BASE_URL = "https://api.weibo.com/2/";
	
	// App Key
	public static final String APP_KEY = "2323213750";
	public static final String APP_SECRET = "b8a12c7befd736bef58e3b4f084ca941";
	
	// Login
	public static final String OAUTH2_ACCESS_TOKEN = SINA_BASE_URL + "oauth2/access_token";
	public static final String OAUTH2_AUTHORIZE = SINA_BASE_URL + "oauth2/authorize";
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
	public static final String LOGIN_URL = OAUTH2_AUTHORIZE + "?client_id=" + APP_KEY + "&redirect_uri=" + REDIRECT_URL + "&display=mobile&response_type=code";
	
	// User / Account
	public static final String GET_UID = SINA_BASE_URL + "account/get_uid.json";
	public static final String USER_SHOW = SINA_BASE_URL + "users/show.json";
	
	// Statuses
	public static final String HOME_TIMELINE = SINA_BASE_URL + "statuses/home_timeline.json";
	public static final String USER_TIMELINE = SINA_BASE_URL + "statuses/user_timeline.json";
	public static final String MENTIONS = SINA_BASE_URL + "statuses/mentions.json";
	public static final String REPOST_TIMELINE = SINA_BASE_URL + "statuses/repost_timeline.json";
	public static final String UPDATE = SINA_BASE_URL + "statuses/update.json";
	public static final String UPLOAD = SINA_BASE_URL + "statuses/upload.json";
	public static final String REPOST = SINA_BASE_URL + "statuses/repost.json";
	
	// Comments
	public static final String COMMENTS_TIMELINE = SINA_BASE_URL + "comments/timeline.json";
	public static final String COMMENTS_MENTIONS = SINA_BASE_URL + "comments/mentions.json";
	public static final String COMMENTS_SHOW = SINA_BASE_URL + "comments/show.json";
	public static final String COMMENTS_CREATE = SINA_BASE_URL + "comments/create.json";
	public static final String COMMENTS_REPLY = SINA_BASE_URL + "comments/reply.json";
	
}
