package server.status.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import server.status.R;
import server.status.Server;
import server.status.Settings;
import server.status.check.Checker;
import server.status.db.ServerData;
import server.status.db.SortedList;
import server.status.ui.SelectCheckerDialog.SelectCheckerListener;
import server.status.ui.ServerHostDialog.ServerHostListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;

public class ServerListFragment extends Fragment implements ServerHostListener,
		SelectCheckerListener, Observer {
	private static final String BUNDLE_EXPANDED = "exp";

	private ExpandableListView listViewServers = null;
	private ServerAdapter serverAdapter;
	private final SortedList<Server> servers2;
	private ArrayList<Integer> expanded = new ArrayList<Integer>();
	private long expandServerId = -1;
	private Server editServer = null;

	// TODO Rework this fragment...

	public ServerListFragment() {
		servers2 = ServerData.getInstance().getServers();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load expanded
		if (savedInstanceState != null) {
			ArrayList<Integer> expanded = savedInstanceState
					.getIntegerArrayList(BUNDLE_EXPANDED);
			if (expanded != null) {
				this.expanded.addAll(expanded);
			}
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		ServerData serverData = ServerData.getInstance();
		serverData.addObserver(this);
		serverData.loadServersAsync(getActivity().getApplicationContext());
		super.onResume();
	}

	@Override
	public void onPause() {
		ServerData serverData = ServerData.getInstance();
		serverData.deleteObserver(this);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (listViewServers != null) {
			// Save the ones that are expanded
			ArrayList<Integer> expanded = new ArrayList<Integer>();
			for (int i = 0, j = listViewServers.getCount(); i < j; i++) {
				if (listViewServers.isGroupExpanded(i)) {
					expanded.add(i);
				}
			}
			outState.putIntegerArrayList(BUNDLE_EXPANDED, expanded);
		}
		super.onSaveInstanceState(outState);
	}

	/**
	 * Used after refreshAll
	 * 
	 * @param id
	 */
	public void expandServer(long id) {
		expandServerId = id;
	}

	public void addServer(Server server) {
		final Activity activity = getActivity();
		ServerData.getInstance().insertAsync(activity.getApplicationContext(),
				server, new Runnable() {
					@Override
					public void run() {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(activity,
										R.string.server_save_fail,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
	}

	/**
	 * Manually starts an update
	 * 
	 * @param index
	 *            Index of the server to start
	 */
	private void update(final Server server) {
		final Context context = getActivity().getApplicationContext();
		if (!server.isCheckRunning()) {
			// TODO Update should run in a service?
			new Thread(new Runnable() {
				@Override
				public void run() {
					Settings settings = new Settings();
					settings.loadSettings(context);
					server.check(settings, context);
				}
			}).start();
		} else {
			Toast.makeText(context, getString(R.string.update_running),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void changeHost(Server server) {
		// TODO Does not work when rotating the screen
		this.editServer = server;
		ServerHostDialog dialog = new ServerHostDialog();
		Bundle args = new Bundle();
		args.putString(ServerHostDialog.INTENT_HOST, server.getHost());
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "ServerHostDialog");
	}

	private void addChecker(Server server) {
		// TODO Does not work when rotating the screen
		this.editServer = server;
		SelectCheckerDialog dialog = new SelectCheckerDialog();
		dialog.show(getFragmentManager(), "SelectCheckerDialog");
	}

	private void remove(final Server server) {
		final Activity activity = getActivity();
		final Context context = activity.getApplicationContext();
		new AlertDialog.Builder(activity)
				.setMessage(
						getString(R.string.server_remove_confirm,
								server.getHost()))
				.setPositiveButton(getString(R.string.button_ok),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// OK clicked, lets remove
								ServerData.getInstance().deleteAsync(context,
										server, new Runnable() {
											@Override
											public void run() {
												activity.runOnUiThread(new Runnable() {
													@Override
													public void run() {
														Toast.makeText(
																context,
																context.getString(R.string.server_remove_fail),
																Toast.LENGTH_SHORT)
																.show();
													}
												});
											}
										});
							}
						})
				.setNegativeButton(getString(R.string.button_cancel), null)
				.show();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_server_list, container,
				false);
		listViewServers = (ExpandableListView) view
				.findViewById(R.id.listViewServers);
		listViewServers.setEmptyView(view.findViewById(R.id.textViewEmptyList));
		Context context = getActivity().getApplicationContext();
		serverAdapter = new ServerAdapter(context, servers2);
		listViewServers.setAdapter(serverAdapter);
		// Long click menu
		registerForContextMenu(listViewServers);
		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listViewServers) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
			int positionGroup = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int positionType = ExpandableListView
					.getPackedPositionType(info.packedPosition);
			Server server = servers2.get(positionGroup);
			Activity activity = getActivity();
			if (positionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				activity.getMenuInflater().inflate(R.menu.server, menu);
				// Set title of the menu
				menu.setHeaderTitle(server.getHost());
			} else if (positionType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int positionChild = ExpandableListView
						.getPackedPositionChild(info.packedPosition);
				activity.getMenuInflater().inflate(R.menu.server_checker, menu);
				menu.setHeaderTitle(server.getCheckers().get(positionChild)
						.getName(activity));
			}
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = item.getMenuInfo();
		if (menuInfo instanceof ExpandableListContextMenuInfo) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
			int positionGroup = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int positionChild = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			Server server = servers2.get(positionGroup);
			switch (item.getItemId()) {
			case R.id.action_server_update_now:
				update(server);
				return true;
			case R.id.action_server_change_host:
				changeHost(server);
				return true;
			case R.id.action_server_add_checker:
				addChecker(server);
				return true;
			case R.id.action_server_remove:
				remove(server);
				return true;
			case R.id.action_server_checker_update_now:
				update(server, positionChild);
				return true;
			case R.id.action_server_checker_edit:
				edit(server, positionChild);
				return true;
			case R.id.action_server_checker_remove:
				remove(server, positionChild);
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	private void update(Server server, int index) {
		// TODO Update checker
	}

	private void edit(Server server, int index) {
		// TODO Edit checker
	}

	private void remove(final Server server, int index) {
		server.removeChecker(index);
		final Activity activity = getActivity();
		ServerData.getInstance().updateAsync(activity.getApplicationContext(),
				server, new Runnable() {
					@Override
					public void run() {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(activity,
										R.string.server_save_fail,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
	}

	@Override
	public void onHostChange(String host) {
		if (editServer != null) {
			editServer.setHost(host);
			final Activity activity = getActivity();
			ServerData.getInstance().updateAsync(
					activity.getApplicationContext(), editServer,
					new Runnable() {
						@Override
						public void run() {
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(activity,
											R.string.server_save_fail,
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					});
			editServer = null;
		}
	}

	@Override
	public void onSelectChecker(Checker checker) {
		if (editServer != null) {
			editServer.addChecker(checker);
			final Activity activity = getActivity();
			ServerData.getInstance().updateAsync(
					activity.getApplicationContext(), editServer,
					new Runnable() {
						@Override
						public void run() {
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(activity,
											R.string.server_save_fail,
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					});
			editServer = null;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				serverAdapter.notifyDataSetChanged();
			}
		});
	}
}
