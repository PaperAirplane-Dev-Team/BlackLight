package us.shandian.blacklight.cache.database.tables;

public class CommentMentionsTimeLineTable
{
	public static final String NAME = "comment_mentions";

	public static final String ID = "id";

	public static final String JSON = "json";

	public static final String CREATE = "create table " + NAME
						+ "("
						+ ID + " integer primary key autoincrement,"
						+ JSON + " text"
						+ ");";
	
}
