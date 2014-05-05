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
	
}
