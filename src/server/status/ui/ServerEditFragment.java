package server.status.ui;

import server.status.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ServerEditFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_server_edit, container,
				false);
		ListView listViewCheckers = (ListView) view
				.findViewById(R.id.listViewCheckers);
		listViewCheckers
				.setEmptyView(view.findViewById(R.id.textViewEmptyList));
		// listViewCheckers.setAdapter(adapter);
		// Long click menu
		// registerForContextMenu();
		return view;
	}
}
