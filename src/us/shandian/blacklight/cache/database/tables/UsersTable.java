package us.shandian.blacklight.cache.database.tables;

public class UsersTable
{
	public static final String NAME = "users";
	
	public static final String UID = "uid";
	
	public static final String USERNAME = "username";
	
	public static final String TIMESTAMP = "timestamp";
	
	public static final String JSON = "json";
	
	public static final String CREATE = "create table " + NAME
						+ "("
						+ UID + " integer primary key autoincrement,"
						+ TIMESTAMP + " integer,"
						+ USERNAME + " text,"
						+ JSON + " text"
						+ ");";
}
