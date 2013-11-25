package server.status;

import server.status.check.Http;
import server.status.check.Https;
import server.status.check.Ping;
import server.status.check.Socket;
import server.status.db.ServerDbHelper;
import server.status.service.Starter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// Make sure it is started if enabled
		Starter.start(getApplicationContext(), Settings.ENABLE_DELAY);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_server:
			// TODO Add server in new thread
			Server server = new Server("192.168.1.1");
			server.addChecker(new Http(8000, 302));
			server.addChecker(new Https(8080, 200, true));
			server.addChecker(new Ping());
			server.addChecker(new Socket(50022));
			boolean saved = ServerDbHelper.getInstance(getApplicationContext())
					.save(server);
			return true;
		case R.id.action_settings:
			showSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}
