package server.status.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Schedule extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!isServiceRunning(context)) {
			// Start the service, if not running
			Intent service = new Intent(context, ServerCheck.class);
			context.startService(service);
		}
	}

	private static boolean isServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ServerCheck.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
