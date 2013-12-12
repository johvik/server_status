package server.status;

import server.status.check.Http;
import server.status.check.Https;
import server.status.check.Ping;
import server.status.check.Socket;
import server.status.db.ServerDbHelper;
import server.status.service.Starter;
import server.status.ui.ServerListFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	private ServerListFragment serverListFragment;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			long id = intent.getLongExtra(Server.INTENT_ID, -1);
			if (id != -1) {
				serverListFragment.refresh(id);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		serverListFragment = (ServerListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragmentServerList);
		// Make sure it is started if enabled
		Starter.start(getApplicationContext(), Settings.ENABLE_DELAY);
	}

	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(Server.BROADCAST_UPDATE));
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
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
			Context context = getApplicationContext();
			Server server = new Server("192.168.1.200");
			server.addChecker(new Http(8000, 302));
			server.addChecker(new Https(8080, 200, true));
			server.addChecker(new Ping());
			server.addChecker(new Socket(50022));
			boolean saved = ServerDbHelper.getInstance(context).save(server);
			if (saved) {
				serverListFragment.addServer(server);
			} else {
				Toast.makeText(context,
						context.getString(R.string.server_add_fail),
						Toast.LENGTH_SHORT).show();
			}
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
