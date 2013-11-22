package server.status;

import java.util.ArrayList;

import server.status.check.Http;
import server.status.check.Https;
import server.status.check.Ping;
import server.status.check.Socket;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	public static final int BOOTUP_DELAY = 30000; // 30s
	public static final int ENABLE_DELAY = 100; // ms
	private boolean enabled = true;
	private long intervalMS = 600000; // 10min
	private int timeoutMS = 5000;
	private int timeout = 5;
	private ArrayList<Server> servers = new ArrayList<Server>();

	public void loadSettings(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		enabled = sharedPref.getBoolean(SettingsActivity.PREF_ENABLED, true);
		try {
			intervalMS = ((long) Integer.parseInt(sharedPref.getString(
					SettingsActivity.PREF_INTERVAL, "10"))) * 60000;
			setTimeoutMS(Integer.parseInt(sharedPref.getString(
					SettingsActivity.PREF_TIMEOUT, "5000")));

			// Set a minimum time to avoid high load
			if (intervalMS <= 100) {
				intervalMS = 100;
			}
		} catch (NumberFormatException e) {
			// Just to be sure
			intervalMS = 600000;
			setTimeoutMS(5000);
		}
	}

	public void loadServers(Context context) {
		// TODO Load servers
		servers.clear();
		Server test = new Server("192.168.1.1");
		test.addChecker(new Http(8000, 302));
		test.addChecker(new Https(8080, 200, true));
		test.addChecker(new Ping());
		test.addChecker(new Socket(50022));
		servers.add(test);
	}

	private void setTimeoutMS(int timeoutMS) {
		this.timeoutMS = timeoutMS;
		timeout = Math.max(1, timeoutMS / 1000);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public long getIntervalMS() {
		return intervalMS;
	}

	public int getTimeoutMS() {
		return timeoutMS;
	}

	public int getTimeout() {
		return timeout;
	}

	public ArrayList<Server> getServers() {
		return servers;
	}
}
