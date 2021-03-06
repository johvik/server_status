package server.status.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import server.status.R;
import server.status.Server;
import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import server.status.db.SortedList;

public class ServerAdapter extends BaseExpandableListAdapter {
	private static final int COLOR_FAIL = Color.parseColor("#DC143C");
	private static final int COLOR_INCONCLUSIVE = Color.parseColor("#FFA500");
	private static final int COLOR_PASS = Color.parseColor("#32CD32");

	private final Context context;
	private final SortedList<Server> list;
	private static final SimpleDateFormat format = new SimpleDateFormat(
			"MMM d, HH:mm", Locale.US);

	public ServerAdapter(Context context, SortedList<Server> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (list.get(groupPosition).getSize() == 0) {
			return null;
		}
		Server server = list.get(groupPosition);
		return server.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		if (list.get(groupPosition).getSize() == 0) {
			return -1;
		}
		Server server = list.get(groupPosition);
		ArrayList<Checker> checkers = server.getCheckers();
		return checkers.get(childPosition).getId();
	}

	private static class ChildViewHolder {
		public final TextView text1;
		public final TextView text2;
		public final TextView text3;
		public final boolean empty;

		public ChildViewHolder(TextView text1, TextView text2, TextView text3) {
			this.text1 = text1;
			this.text2 = text2;
			this.text3 = text3;
			empty = false;
		}

		public ChildViewHolder() {
			text1 = null;
			text2 = null;
			text3 = null;
			empty = true;
		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (childPosition == 0 && list.get(groupPosition).getSize() == 0) {
			// Show hint when no children
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_item_server_empty, parent, false);
				convertView.setTag(new ChildViewHolder());
			} else {
				ChildViewHolder viewHolder = (ChildViewHolder) convertView
						.getTag();
				if (!viewHolder.empty) {
					// Previous wasn't a empty view
					convertView = LayoutInflater.from(context).inflate(
							R.layout.list_item_server_empty, parent, false);
					convertView.setTag(new ChildViewHolder());
				}
			}
			return convertView;
		}
		TextView text1;
		TextView text2;
		TextView text3;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_item_server_details, parent, false);
			text1 = (TextView) convertView.findViewById(R.id.text1);
			text2 = (TextView) convertView.findViewById(R.id.text2);
			text3 = (TextView) convertView.findViewById(R.id.text3);
			convertView.setTag(new ChildViewHolder(text1, text2, text3));
		} else {
			ChildViewHolder viewHolder = (ChildViewHolder) convertView.getTag();
			if (viewHolder.empty) {
				// Previous view was empty
				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_item_server_details, parent, false);
				text1 = (TextView) convertView.findViewById(R.id.text1);
				text2 = (TextView) convertView.findViewById(R.id.text2);
				text3 = (TextView) convertView.findViewById(R.id.text3);
				convertView.setTag(new ChildViewHolder(text1, text2, text3));
			} else {
				text1 = viewHolder.text1;
				text2 = viewHolder.text2;
				text3 = viewHolder.text3;
			}
		}

		@SuppressWarnings("unchecked")
		Pair<Checker, Status> p = (Pair<Checker, Status>) getChild(
				groupPosition, childPosition);
		Checker checker = p.first;
		Status result = p.second;
		int color;
		if (result.result == Result.FAIL) {
			color = COLOR_FAIL;
		} else if (result.result == Result.INCONCLUSIVE) {
			color = COLOR_INCONCLUSIVE;
		} else {
			color = COLOR_PASS;
		}
		text1.setText(checker.getName(context));
		long time = result.time;
		String oldestTime;
		if (time > 0) {
			oldestTime = format.format(time);
		} else {
			oldestTime = context.getString(R.string.never);
		}
		text2.setText(oldestTime);
		text3.setText(result.reason);
		if (result.result == Result.PASS || result.reason.length() == 0) {
			text3.setVisibility(View.GONE);
		} else {
			text3.setVisibility(View.VISIBLE);
		}

		text1.setTextColor(color);
		text2.setTextColor(color);
		text3.setTextColor(color);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int size = list.get(groupPosition).getSize();
		if (size == 0) {
			return 1;
		}
		return size;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return list.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return list.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return list.get(groupPosition).getId();
	}

	private static class GroupViewHolder {
		public final TextView text1;
		public final ProgressBar progressBar1;

		public GroupViewHolder(TextView text1, ProgressBar progressBar1) {
			this.text1 = text1;
			this.progressBar1 = progressBar1;
		}
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView text1;
		ProgressBar progressBar1;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_item_server, parent, false);
			text1 = (TextView) convertView.findViewById(R.id.text1);
			progressBar1 = (ProgressBar) convertView
					.findViewById(R.id.progressBar1);
			convertView.setTag(new GroupViewHolder(text1, progressBar1));
		} else {
			GroupViewHolder viewHolder = (GroupViewHolder) convertView.getTag();
			text1 = viewHolder.text1;
			progressBar1 = viewHolder.progressBar1;
		}

		Server server = list.get(groupPosition);
		text1.setText(server.getDisplayHost(context));
		if (server.hasFail()) {
			text1.setTextColor(COLOR_FAIL);
		} else if (server.hasInconclusive()) {
			text1.setTextColor(COLOR_INCONCLUSIVE);
		} else {
			text1.setTextColor(COLOR_PASS);
		}
		// Hide progress bar when done
		if (server.isCheckRunning()) {
			progressBar1.setVisibility(View.VISIBLE);
		} else {
			progressBar1.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		if (list.get(groupPosition).getSize() == 0) {
			return false;
		}
		return true;
	}
}
