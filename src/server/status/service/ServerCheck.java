package server.status.service;

import server.status.Server;
import server.status.Settings;
import server.status.db.ServerData;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ServerCheck extends IntentService {
	public static final String INTENT_SERVER_ID = "isid";

	public ServerCheck() {
		super("ServerCheck");
		// Restart if it is killed while running
		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
		Settings settings = new Settings();
		settings.loadSettings(context);
		if (settings.isEnabled()) { // Extra check if something gets messed up
			ServerData serverData = ServerData.getInstance();
			// Load servers in this thread
			serverData.loadServersSync(context);
			long id = intent.getLongExtra(INTENT_SERVER_ID, -1);
			Server server = serverData.getServers().find(Server.fromId(id));
			if (server != null) {
				server.check(settings, context);
			}
		}
	}
}
