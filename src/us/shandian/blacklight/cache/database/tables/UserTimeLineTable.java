package us.shandian.blacklight.cache.database.tables;

public class UserTimeLineTable
{
	public static final String NAME = "user_timeline";

	public static final String UID = "uid";

	public static final String JSON = "json";

	public static final String CREATE = "create table " + NAME
	+ "("
	+ UID + " integer primary key autoincrement,"
	+ JSON + " text"
	+ ");";
}
