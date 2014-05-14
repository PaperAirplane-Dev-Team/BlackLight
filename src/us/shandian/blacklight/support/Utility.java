package us.shandian.blacklight.support;

import java.io.UnsupportedEncodingException;
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
	
	public static boolean isCacheAvailable(long createTime, int availableDays) {
		return System.currentTimeMillis() <= createTime + TimeUnit.DAYS.toMillis(availableDays);
	}
	
	public static int lengthOfString(String str) throws UnsupportedEncodingException {
		// Considers 1 Chinese character as 2 English characters
		return (str.getBytes("GB2312").length + 1) / 2;
	}
}
