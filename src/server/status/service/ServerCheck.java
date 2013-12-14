package server.status.service;

import java.util.ArrayList;

import server.status.Server;
import server.status.Settings;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ServerCheck extends IntentService {
	public ServerCheck() {
		super("ServerCheck");
		// Restart if it is killed while running
		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Handle each server in individual intents?
		Context context = getApplicationContext();
		Settings settings = new Settings();
		settings.loadSettings(context);
		if (settings.isEnabled()) { // Extra check if something gets messed up
			settings.loadServers(context);
			ArrayList<Server> servers = settings.getServers();
			for (Server server : servers) {
				server.check(settings, context);
			}
		}
	}
}
