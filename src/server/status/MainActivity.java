package server.status;

import server.status.check.Checker;
import server.status.check.Http;
import server.status.check.Https;
import server.status.check.Ping;
import server.status.check.Socket;
import server.status.db.ServerData;
import server.status.service.Starter;
import server.status.ui.ConfirmDialog;
import server.status.ui.SelectCheckerDialog;
import server.status.ui.ServerHostDialog;
import server.status.ui.ConfirmDialog.ConfirmDialogListener;
import server.status.ui.SelectCheckerDialog.SelectCheckerListener;
import server.status.ui.ServerHostDialog.ServerHostListener;
import server.status.ui.ServerListFragment.ServerListFragmentListener;
import server.status.ui.checker.CheckerEditDialog;
import server.status.ui.checker.CheckerEditDialog.CheckerEditDialogListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ServerHostListener, SelectCheckerListener, ServerListFragmentListener,
		ConfirmDialogListener, CheckerEditDialogListener {
	private static final String BUNDLE_EDIT_SERVER_ID = "esid";
	private static final String BUNDLE_EDIT_CHECKER_INDEX = "ecind";
	private long editServerId = -1;
	private int editCheckerIndex = -1;
	private ServerData serverData;
	private Runnable serverSaveFail = new Runnable() {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							R.string.server_save_fail, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	};
	private Runnable serverRemoveFail = new Runnable() {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							R.string.server_remove_fail, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// Make sure it is started if enabled
		Starter.start(getApplicationContext(), Settings.ENABLE_DELAY);

		// Restore edit id
		if (savedInstanceState != null) {
			editServerId = savedInstanceState
					.getLong(BUNDLE_EDIT_SERVER_ID, -1);
			editCheckerIndex = savedInstanceState.getInt(
					BUNDLE_EDIT_CHECKER_INDEX, -1);
		}

		serverData = ServerData.getInstance();
		// Make sure servers are loaded
		serverData.loadServersAsync(getApplicationContext());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(BUNDLE_EDIT_SERVER_ID, editServerId);
		outState.putInt(BUNDLE_EDIT_CHECKER_INDEX, editCheckerIndex);
		super.onSaveInstanceState(outState);
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
			Server server = new Server("192.168.1.200");
			server.addChecker(new Http(8000, 302));
			server.addChecker(new Https(8080, 200, true));
			server.addChecker(new Ping());
			server.addChecker(new Socket(50022));
			serverData.insertAsync(getApplicationContext(), server,
					serverSaveFail);
			return true;
		case R.id.action_settings:
			// Start settings activity
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onHostChange(String host) {
		Server server = serverData.getServers().find(
				Server.fromId(editServerId));
		if (server != null) {
			server.setHost(host);
			serverData.updateAsync(getApplicationContext(), server,
					serverSaveFail);
		}
		editServerId = -1;
	}

	@Override
	public void onSelectChecker(Checker checker) {
		Server server = serverData.getServers().find(
				Server.fromId(editServerId));
		if (server != null) {
			server.addChecker(checker);
			serverData.updateAsync(getApplicationContext(), server,
					serverSaveFail);
		}
		editServerId = -1;
	}

	@Override
	public void onConfirm() {
		Server server = serverData.getServers().find(
				Server.fromId(editServerId));
		if (server != null) {
			serverData.deleteAsync(getApplicationContext(), server,
					serverRemoveFail);
		}
		editServerId = -1;
	}

	@Override
	public void onCheckerEditDone(Checker checker) {
		Server server = serverData.getServers().find(
				Server.fromId(editServerId));
		if (server != null) {
			server.setChecker(editCheckerIndex, checker);
			server.addChecker(checker);
			serverData.updateAsync(getApplicationContext(), server,
					serverSaveFail);
		}
		editServerId = -1;
		editCheckerIndex = -1;
	}

	@Override
	public void onUpdateNow(final Server server) {
		// Force an update
		new Thread(new Runnable() {
			@Override
			public void run() {
				Context context = getApplicationContext();
				Settings settings = new Settings();
				settings.loadSettings(context);
				server.check(settings, context);
			}
		}).start();
	}

	@Override
	public void onChangeHost(Server server) {
		editServerId = server.getId();
		ServerHostDialog dialog = new ServerHostDialog();
		Bundle args = new Bundle();
		args.putString(ServerHostDialog.INTENT_HOST, server.getHost());
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "ServerHostDialog");
	}

	@Override
	public void onAddChecker(Server server) {
		editServerId = server.getId();
		SelectCheckerDialog dialog = new SelectCheckerDialog();
		dialog.show(getSupportFragmentManager(), "SelectCheckerDialog");
	}

	@Override
	public void onRemove(Server server) {
		editServerId = server.getId();
		ConfirmDialog dialog = new ConfirmDialog();
		Bundle args = new Bundle();
		args.putString(ConfirmDialog.INTENT_MESSAGE,
				getString(R.string.server_remove_confirm, server.getHost()));
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "ConfirmDialog");
	}

	@Override
	public void onUpdateChecker(final Server server, final int index) {
		// Force an update
		new Thread(new Runnable() {
			@Override
			public void run() {
				Context context = getApplicationContext();
				Settings settings = new Settings();
				settings.loadSettings(context);
				server.checkSingle(settings, context, index);
			}
		}).start();
	}

	@Override
	public void onEditChecker(Server server, int index) {
		editServerId = server.getId();
		editCheckerIndex = index;
		Checker checker = server.getCheckers().get(index);
		CheckerEditDialog dialog = CheckerEditDialog.getEditDialog(checker);
		Bundle args = new Bundle();
		args.putParcelable(CheckerEditDialog.INTENT_CHECKER, checker);
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "CheckerEditDialog");
	}

	@Override
	public void onRemoveChecker(Server server, int index) {
		server.removeChecker(index);
		serverData.updateAsync(getApplicationContext(), server, serverSaveFail);
	}
}
