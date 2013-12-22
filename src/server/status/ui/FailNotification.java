package server.status.ui;

import java.util.ArrayList;

import server.status.MainActivity;
import server.status.R;
import server.status.Server;
import server.status.Settings;
import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class FailNotification {

	public static void show(Context context, Settings settings, Server server) {
		Intent intent = new Intent(context, MainActivity.class);
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		ArrayList<Status> results = server.getResults();
		ArrayList<Checker> checkers = server.getCheckers();
		int size = results.size();
		for (int i = 0; i < size; i++) {
			Status result = results.get(i);
			if (result.result != Result.PASS) {
				inboxStyle.addLine(checkers.get(i).getName(context) + " "
						+ result.reason);
			}
		}
		String summary = context.getString(R.string.notification_text_fail,
				(size - server.getPassCount()), size);
		inboxStyle.setSummaryText(summary);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				context)
				.setContentTitle(
						context.getString(R.string.notification_title_fail,
								server.getDisplayHost(context)))
				.setContentText(summary).setSmallIcon(R.drawable.ic_stat_fail)
				.setContentIntent(pendingIntent).setAutoCancel(true)
				.setStyle(inboxStyle);
		int defaults = Notification.DEFAULT_LIGHTS;
		if (settings.notificationSound()) {
			// Play sound and vibrate
			defaults |= Notification.DEFAULT_SOUND;
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		notificationBuilder.setDefaults(defaults);
		notificationManager.notify((int) server.getId(),
				notificationBuilder.build());
	}
}
