package server.status.ui;

import java.util.ArrayList;

import server.status.R;
import server.status.Server;
import server.status.Settings;
import server.status.db.ServerDbHelper;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ServerListFragment extends Fragment {
	private ServerAdapter serverAdapter;
	private ArrayList<Server> servers;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Settings settings = new Settings();
		Context context = activity.getApplicationContext();
		settings.loadServers(context);
		servers = settings.getServers();
		serverAdapter = new ServerAdapter(context, servers);
	}

	public void addServer(Server server) {
		servers.add(server);
		serverAdapter.notifyDataSetChanged();
	}

	private void remove(int index) {
		Context context = getActivity().getApplicationContext();
		Server server = servers.get(index);
		boolean deleted = ServerDbHelper.getInstance(context).delete(server);
		if (deleted) {
			servers.remove(index);
			serverAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(context,
					context.getString(R.string.server_remove_fail),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_server_list, container,
				false);
		ListView listViewServers = (ListView) view
				.findViewById(R.id.listViewServers);
		listViewServers.setEmptyView(view.findViewById(R.id.textViewEmptyList));
		listViewServers.setAdapter(serverAdapter);
		listViewServers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO
			}
		});
		listViewServers
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO
						remove(position);
						return true;
					}
				});
		return view;
	}
}
