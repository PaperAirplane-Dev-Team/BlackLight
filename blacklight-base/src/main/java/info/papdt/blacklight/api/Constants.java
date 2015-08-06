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

package info.papdt.blacklight.api;

/* Constants for weibo api */

public class Constants
{
	public static final String SINA_BASE_URL = "https://api.weibo.com/2/";

	// Login
	public static final String OAUTH2_ACCESS_AUTHORIZE = "https://open.weibo.cn/oauth2/authorize";
	public static final String OAUTH2_ACCESS_TOKEN = SINA_BASE_URL + "oauth2/access_token";

	// User / Account
	public static final String GET_UID = SINA_BASE_URL + "account/get_uid.json";
	public static final String USER_SHOW = SINA_BASE_URL + "users/show.json";

	// Statuses
	public static final String HOME_TIMELINE = SINA_BASE_URL + "statuses/home_timeline.json";
	public static final String USER_TIMELINE = SINA_BASE_URL + "statuses/user_timeline.json";
	public static final String BILATERAL_TIMELINE = SINA_BASE_URL + "statuses/bilateral_timeline.json";
	public static final String MENTIONS = SINA_BASE_URL + "statuses/mentions.json";
	public static final String REPOST_TIMELINE = SINA_BASE_URL + "statuses/repost_timeline.json";
	public static final String UPDATE = SINA_BASE_URL + "statuses/update.json";
	public static final String UPLOAD = SINA_BASE_URL + "statuses/upload.json";
	public static final String REPOST = SINA_BASE_URL + "statuses/repost.json";
	public static final String DESTROY = SINA_BASE_URL + "statuses/destroy.json";
	public static final String UPLOAD_PIC = SINA_BASE_URL + "statuses/upload_pic.json";
	public static final String UPLOAD_URL_TEXT = SINA_BASE_URL + "statuses/upload_url_text.json";

	// Comments
	public static final String COMMENTS_TIMELINE = SINA_BASE_URL + "comments/timeline.json";
	public static final String COMMENTS_MENTIONS = SINA_BASE_URL + "comments/mentions.json";
	public static final String COMMENTS_SHOW = SINA_BASE_URL + "comments/show.json";
	public static final String COMMENTS_CREATE = SINA_BASE_URL + "comments/create.json";
	public static final String COMMENTS_REPLY = SINA_BASE_URL + "comments/reply.json";
	public static final String COMMENTS_DESTROY = SINA_BASE_URL + "comments/destroy.json";

	// Favorites
	public static final String FAVORITES_CREATE = SINA_BASE_URL + "favorites/create.json";
	public static final String FAVORITES_DESTROY = SINA_BASE_URL + "favorites/destroy.json";
	public static final String FAVORITES_LIST = SINA_BASE_URL + "favorites.json";

	// Search
	public static final String SEARCH_TOPICS = SINA_BASE_URL + "search/topics.json";
	public static final String SEARCH_STATUSES = SINA_BASE_URL + "search/statuses.json";
	public static final String SEARCH_USERS = SINA_BASE_URL + "search/users.json";
	public static final String SEARCH_SUGGESTIONS_AT_USERS = SINA_BASE_URL + "search/suggestions/at_users.json";

	// Friendships
	public static final String FRIENDSHIPS_FRIENDS = SINA_BASE_URL + "friendships/friends.json";
	public static final String FRIENDSHIPS_FOLLOWERS = SINA_BASE_URL + "friendships/followers.json";
	public static final String FRIENDSHIPS_CREATE = SINA_BASE_URL + "friendships/create.json";
	public static final String FRIENDSHIPS_DESTROY = SINA_BASE_URL + "friendships/destroy.json";
	public static final String FRIENDSHIPS_GROUPS = SINA_BASE_URL + "friendships/groups.json";
	public static final String FRIENDSHIPS_GROUPS_IS_MEMBER = SINA_BASE_URL + "friendships/groups/is_member.json";
	public static final String FRIENDSHIPS_GROUPS_TIMELINE = SINA_BASE_URL + "friendships/groups/timeline.json";
	public static final String FRIENDSHIPS_GROUPS_CREATE = SINA_BASE_URL + "friendships/groups/create.json";
	public static final String FRIENDSHIPS_GROUPS_DESTROY = SINA_BASE_URL + "friendships/groups/destroy.json";
	public static final String FRIENDSHIPS_GROUPS_MEMBERS_ADD = SINA_BASE_URL + "friendships/groups/members/add.json";
	public static final String FRIENDSHIPS_GROUPS_MEMBERS_DESTROY = SINA_BASE_URL + "friendships/groups/members/destroy.json";

	// Direct Message
	public static final String DIRECT_MESSAGES_USER_LIST = SINA_BASE_URL + "direct_messages/user_list.json";
	public static final String DIRECT_MESSAGES_CONVERSATION = SINA_BASE_URL + "direct_messages/conversation.json";
	public static final String DIRECT_MESSAGES_SEND = SINA_BASE_URL + "direct_messages/new.json";
	public static final String DIRECT_MESSAGES_THUMB_PIC = "https://upload.api.weibo.com/2/mss/msget_thumbnail?fid=%d&access_token=%s&high=%d&width=%d";
	public static final String DIRECT_MESSAGES_ORIG_PIC = "http://upload.api.weibo.com/2/mss/msget?fid=%d&access_token=%s";

	// Remind
	public static final String REMIND_UNREAD_COUNT = "https://rm.api.weibo.com/2/remind/unread_count.json";
	public static final String REMIND_UNREAD_SET_COUNT = "https://rm.api.weibo.com/2/remind/set_count.json";

	// Attitude
	public static final String ATTITUDE_CREATE = SINA_BASE_URL + "attitudes/create.json";
	public static final String ATTITUDE_DESTROY = SINA_BASE_URL + "attitudes/destroy.json";
}
