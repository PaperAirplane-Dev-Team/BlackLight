package us.shandian.blacklight.support;

import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;

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
	
	public static int getSupportedMaxPictureSize() {
		int[] array = new int[1];
		GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, array, 0);
		
		if (array[0] == 0) {
			GLES11.glGetIntegerv(GLES11.GL_MAX_TEXTURE_SIZE, array, 0);
			
			if (array[0] == 0) {
				GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, array, 0);
				
				if (array[0] == 0) {
					GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, array, 0);
				}
			}
		}
		
		return array[0] != 0 ? array[0] : 2048;
	}
	
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}
	
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
		Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
