package server.status.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import server.status.R;
import server.status.Server;
import server.status.db.ServerData;
import server.status.db.SortedList;
import android.app.Activity;
import android.content.Context;
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

public class ServerListFragment extends Fragment implements Observer {
	public interface ServerListFragmentListener {
		public void onUpdateNow(Server server);

		public void onChangeHost(Server server);

		public void onAddChecker(Server server);

		public void onRemove(Server server);

		public void onUpdateChecker(Server server, int index);

		public void onEditChecker(Server server, int index);

		public void onRemoveChecker(Server server, int index);
	}

	private static final String BUNDLE_EXPANDED = "exp";

	private ExpandableListView listViewServers = null;
	private ServerAdapter serverAdapter;
	private final SortedList<Server> servers;
	private boolean expanded = false;
	private ArrayList<Integer> expandedIndexes = new ArrayList<Integer>();

	private ServerListFragmentListener serverListFragmentListener;

	public ServerListFragment() {
		servers = ServerData.getInstance().getServers();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Attach listener
		if (activity instanceof ServerListFragmentListener) {
			serverListFragmentListener = (ServerListFragmentListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement ServerListFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Load expanded
		if (savedInstanceState != null) {
			ArrayList<Integer> expanded = savedInstanceState
					.getIntegerArrayList(BUNDLE_EXPANDED);
			if (expanded != null) {
				expandedIndexes.addAll(expanded);
			}
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		// Listen for changes in data
		ServerData serverData = ServerData.getInstance();
		serverData.addObserver(this);
		serverData.loadServersAsync(getActivity().getApplicationContext());
		super.onResume();
	}

	@Override
	public void onPause() {
		// Stop listening to data
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_server_list, container,
				false);
		listViewServers = (ExpandableListView) view
				.findViewById(R.id.listViewServers);
		listViewServers.setEmptyView(view.findViewById(R.id.textViewEmptyList));
		Context context = getActivity().getApplicationContext();
		serverAdapter = new ServerAdapter(context, servers);
		listViewServers.setAdapter(serverAdapter);
		// Long click menu
		registerForContextMenu(listViewServers);
		updateExpanded();
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
			Server server = servers.get(positionGroup);
			Activity activity = getActivity();
			if (positionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				activity.getMenuInflater().inflate(R.menu.server, menu);
				// Set title of the menu
				menu.setHeaderTitle(server.getDisplayHost(activity
						.getApplicationContext()));
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
			Server server = servers.get(positionGroup);
			switch (item.getItemId()) {
			case R.id.action_server_update_now:
				serverListFragmentListener.onUpdateNow(server);
				return true;
			case R.id.action_server_change_host:
				serverListFragmentListener.onChangeHost(server);
				return true;
			case R.id.action_server_add_checker:
				serverListFragmentListener.onAddChecker(server);
				return true;
			case R.id.action_server_remove:
				serverListFragmentListener.onRemove(server);
				return true;
			case R.id.action_server_checker_update_now:
				serverListFragmentListener.onUpdateChecker(server,
						positionChild);
				return true;
			case R.id.action_server_checker_edit:
				serverListFragmentListener.onEditChecker(server, positionChild);
				return true;
			case R.id.action_server_checker_remove:
				serverListFragmentListener.onRemoveChecker(server,
						positionChild);
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	private void updateExpanded() {
		if (!expanded && listViewServers != null) {
			int serverCount = servers.size();
			if (!expandedIndexes.isEmpty()) {
				int lastIndex = expandedIndexes.get(expandedIndexes.size() - 1);
				// Check that to avoid index of out bounds
				if (lastIndex < serverCount) {
					for (int index : expandedIndexes) {
						// Expand
						listViewServers.expandGroup(index);
					}
					expanded = true;
				}
			} else {
				expanded = true;
			}
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		// Make sure it runs on the UI thread
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateExpanded();
				serverAdapter.notifyDataSetChanged();
			}
		});
	}
}
