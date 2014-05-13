package us.shandian.blacklight.api;

/* Constants for weibo api */

public class Constants
{
	public static final String SINA_BASE_URL = "https://api.weibo.com/2/";
	
	// Login
	public static final String OAUTH2_ACCESS_TOKEN = SINA_BASE_URL + "oauth2/access_token";
	
	// User / Account
	public static final String GET_UID = SINA_BASE_URL + "account/get_uid.json";
	public static final String USER_SHOW = SINA_BASE_URL + "users/show.json";
	
	// Statuses
	public static final String HOME_TIMELINE = SINA_BASE_URL + "statuses/home_timeline.json";
	public static final String USER_TIMELINE = SINA_BASE_URL + "statuses/user_timeline.json";
	public static final String MENTIONS = SINA_BASE_URL + "statuses/mentions.json";
	public static final String REPOST_TIMELINE = SINA_BASE_URL + "statuses/repost_timeline.json";
	public static final String UPDATE = SINA_BASE_URL + "statuses/update.json";
	
	// Comments
	public static final String COMMENTS_TIMELINE = SINA_BASE_URL + "comments/timeline.json";
	public static final String COMMENTS_MENTIONS = SINA_BASE_URL + "comments/mentions.json";
	public static final String COMMENTS_SHOW = SINA_BASE_URL + "comments/show.json";
	
}
