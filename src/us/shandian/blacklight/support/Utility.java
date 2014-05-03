package us.shandian.blacklight.support;

import java.util.concurrent.TimeUnit;

/* Helper functions */
public class Utility
{
	public static int expireTimeInDays(long time) {
		return (int) TimeUnit.MILLISECONDS.toDays(time - System.currentTimeMillis());
	}
	
	public static boolean isTokenExpired(long time) {
		return time <= System.currentTimeMillis();
	}
}
