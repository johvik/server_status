package server.status.service;

import server.status.Settings;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Starter {
	private static int REQUEST_CODE = 92834;

	/**
	 * Sets the alarm which will start the server checker.
	 * 
	 * @param context
	 *            Context to start the alarm on, should be gotten from
	 *            getApplicationContext() or similar.
	 * @param firstDelay
	 *            Number of milliseconds to wait for the first run.
	 */
	public static void start(Context context, int firstDelay) {
		Settings settings = new Settings();
		settings.loadSettings(context);
		if (settings.isEnabled()) {
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, Schedule.class);
			boolean alarmUp = PendingIntent.getBroadcast(context, REQUEST_CODE,
					intent, PendingIntent.FLAG_NO_CREATE) != null;
			Log.d("Starter", "start " + alarmUp);
			if (!alarmUp) {
				// Start if not running
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, REQUEST_CODE, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis() + firstDelay,
						settings.getIntervalMS(), pendingIntent);
			}
		}
	}

	public static void update(Context context) {
		// TODO Update alarm instead
		stop(context);
		start(context, Settings.ENABLE_DELAY);
	}

	/**
	 * Cancels the alarm which runs the server checker.
	 * 
	 * @param context
	 *            Context to start the alarm on, should be gotten from
	 *            getApplicationContext() or similar.
	 */
	public static void stop(Context context) {
		Log.d("Starter", "stop");
		Intent intent = new Intent(context, Schedule.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntent.cancel();
	}
}
