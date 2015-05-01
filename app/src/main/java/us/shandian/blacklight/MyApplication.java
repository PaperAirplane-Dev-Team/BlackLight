package us.shandian.blacklight;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

public class MyApplication extends Application implements SensorEventListener
{
	private static final String NAME_BLACK = "info.papdt.blacklight.ui.entry.EntryActivity-Black";
	private static final String NAME_WHITE = "info.papdt.blacklight.ui.entry.EntryActivity-White";

	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private PackageManager mPackageManager;
	private ActivityManager mActivityManager;
	private SharedPreferences mPref;
	private boolean mLastState = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mPackageManager = getPackageManager();
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mPref = getSharedPreferences("app_component", Context.MODE_WORLD_READABLE);
		mLastState = mPref.getBoolean("state", false);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mSensorManager.unregisterListener(this, mLightSensor);
	}
	
	@Override
	public void onSensorChanged(SensorEvent ev) {
		switchLauncherIcon(ev.values[0] >= 1233.0f); //233lux is too low.
	}

	@Override
	public void onAccuracyChanged(Sensor ev, int newAccuracy) {
		
	}
	
	private void switchLauncherIcon(boolean isWhite) {
		if (isWhite == mLastState) return;
		
		mLastState = isWhite;
		mPref.edit().putBoolean("state", isWhite).commit();
		
		mPackageManager.setComponentEnabledSetting(new ComponentName(this, NAME_BLACK), 
			isWhite ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			PackageManager.DONT_KILL_APP);
		mPackageManager.setComponentEnabledSetting(new ComponentName(this, NAME_WHITE), 
			isWhite ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP);
			
		if (isWhite) {
			Toast.makeText(this, R.string.not_black_enough, Toast.LENGTH_SHORT).show();
		}
		
		killLauncher();
	}
	
	private void killLauncher() {
		// Kill Launcher
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		List<ResolveInfo> info = mPackageManager.queryIntentActivities(i, 0);
		for (ResolveInfo ri : info) {
			mActivityManager.killBackgroundProcesses(ri.activityInfo.packageName);
		}
	}
}
