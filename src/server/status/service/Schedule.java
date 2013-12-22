package server.status.service;

import server.status.Server;
import server.status.db.ServerData;
import server.status.db.SortedList;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Schedule extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Start the service, if not running
		if (!isServiceRunning(context)) {
			ServerData serverData = ServerData.getInstance();
			// Load servers in this thread
			serverData.loadServersSync(context);
			SortedList<Server> servers = serverData.getServers();
			// Start one service for each server
			for (Server server : servers) {
				Intent service = new Intent(context, ServerCheck.class);
				service.putExtra(ServerCheck.INTENT_SERVER_ID, server.getId());
				context.startService(service);
			}
			// Run cleanup
			serverData.cleanupSync(context);
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
