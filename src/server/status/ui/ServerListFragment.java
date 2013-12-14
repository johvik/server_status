package server.status.ui;

import java.util.ArrayList;
import java.util.Collections;

import server.status.R;
import server.status.Server;
import server.status.Settings;
import server.status.db.ServerDbHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;

public class ServerListFragment extends Fragment {
	private static final int ID_UPDATE = 1;
	private static final int ID_EDIT = 2;
	private static final int ID_REMOVE = 3;
	private static final String BUNDLE_EXPANDED = "exp";

	private ExpandableListView listViewServers = null;
	private ServerAdapter serverAdapter;
	private ArrayList<Server> servers = new ArrayList<Server>();
	private ArrayList<Integer> expanded = new ArrayList<Integer>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Context context = activity.getApplicationContext();
		serverAdapter = new ServerAdapter(context, servers);
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
		refreshAll();
		super.onCreate(savedInstanceState);
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
	 * Refresh server by id
	 * 
	 * @param serverId
	 * @param done
	 *            Indicates if it was the last update for the server
	 */
	public void refresh(final long serverId, final boolean done) {
		final Activity activity = getActivity();
		final Context context = activity.getApplicationContext();
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Server server = ServerDbHelper.getInstance(context).load(
						serverId);
				if (server != null) {
					// Set done flag
					server.setDone(done);
					Server found = null;
					// Find old by id
					for (Server s : servers) {
						if (s.getId() == serverId) {
							found = s;
							break;
						}
					}
					final Server old = found;
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// Remove old
							if (old != null) {
								servers.remove(old);
							}
							// Add new
							addServer(server);
						}
					});
				}
			}
		}).start();
	}

	/**
	 * Replaces the server list with a newly loaded one.
	 */
	private void refreshAll() {
		final Activity activity = getActivity();
		final Context context = activity.getApplicationContext();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Settings settings = new Settings();
				settings.loadServers(context);
				final ArrayList<Server> list = settings.getServers();
				Collections.sort(list);

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						servers.clear();
						servers.addAll(list);
						// Expand items
						if (listViewServers != null) {
							int size = servers.size();
							for (Integer i : expanded) {
								if (i < size) {
									listViewServers.expandGroup(i);
								}
							}
						}
						serverAdapter.notifyDataSetChanged();
					}
				});
			}
		}).start();
	}

	public void addServer(Server server) {
		// Add in order
		int index = Collections.binarySearch(servers, server);
		if (index < 0) {
			servers.add((-index) - 1, server);
		} else {
			servers.add(index, server);
		}
		serverAdapter.notifyDataSetChanged();
	}

	/**
	 * Manually starts an update
	 * 
	 * @param index
	 *            Index of the server to start
	 */
	private void update(final Server server) {
		final Context context = getActivity().getApplicationContext();
		if (server.isDone()) {
			// TODO Done flag is unstable if rotated for example. Save in db?
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

	private void edit(Server server) {
		// TODO Edit activity
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
								new Thread(new Runnable() {
									@Override
									public void run() {
										final boolean deleted = ServerDbHelper
												.getInstance(context).delete(
														server);
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (deleted) {
													if (servers.remove(server)) {
														serverAdapter
																.notifyDataSetChanged();
													}
												} else {
													Toast.makeText(
															context,
															context.getString(R.string.server_remove_fail),
															Toast.LENGTH_SHORT)
															.show();
												}
											}
										});
									}
								}).start();
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
			Server server = servers.get(ExpandableListView
					.getPackedPositionGroup(info.packedPosition));
			menu.setHeaderTitle(server.getHost());
			menu.add(Menu.NONE, ID_UPDATE, Menu.NONE,
					R.string.action_update_server);
			menu.add(Menu.NONE, ID_EDIT, Menu.NONE, R.string.action_edit_server);
			menu.add(Menu.NONE, ID_REMOVE, Menu.NONE,
					R.string.action_remove_server);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ID_UPDATE: {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
					.getMenuInfo();
			update(servers.get(ExpandableListView
					.getPackedPositionGroup(info.packedPosition)));
			return true;
		}
		case ID_EDIT: {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
					.getMenuInfo();
			edit(servers.get(ExpandableListView
					.getPackedPositionGroup(info.packedPosition)));
			return true;
		}
		case ID_REMOVE: {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
					.getMenuInfo();
			remove(servers.get(ExpandableListView
					.getPackedPositionGroup(info.packedPosition)));
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}
}
