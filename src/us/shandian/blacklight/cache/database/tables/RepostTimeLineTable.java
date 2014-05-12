package us.shandian.blacklight.cache.database.tables;

public class RepostTimeLineTable
{
	public static final String NAME = "repost_timeline";

	public static final String MSGID = "msgId";

	public static final String JSON = "json";

	public static final String CREATE = "create table " + NAME
						+ "("
						+ MSGID + " integer primary key autoincrement,"
						+ JSON + " text"
						+ ");";
}
