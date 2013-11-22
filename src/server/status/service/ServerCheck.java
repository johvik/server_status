package server.status.service;

import server.status.Server;
import server.status.Settings;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServerCheck extends IntentService {
	public ServerCheck() {
		super("ServerCheck");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("ServerCheck", "checking...");
		Context context = getApplicationContext();
		Settings settings = new Settings();
		settings.loadSettings(context);
		if (settings.isEnabled()) { // Extra check if something gets messed up
			settings.loadServers(context);
			for (Server server : settings.getServers()) {
				server.check(settings);
			}
		}
	}
}
