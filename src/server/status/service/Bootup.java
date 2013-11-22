package server.status.service;

import server.status.Settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Bootup extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Start the alarm if enabled
		Settings settings = new Settings();
		settings.loadSettings(context);
		if (settings.isEnabled()) {
			Starter.start(context, Settings.BOOTUP_DELAY);
		}
	}
}
