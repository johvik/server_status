package server.status.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import server.status.R;
import server.status.Server;

public class ServerAdapter extends BaseAdapter {
	private final Context context;
	private final ArrayList<Server> list;
	private static final SimpleDateFormat format = new SimpleDateFormat(
			"HH:mm", Locale.US);

	public ServerAdapter(Context context, ArrayList<Server> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public final TextView text1;
		public final TextView text2;

		public ViewHolder(TextView text1, TextView text2) {
			this.text1 = text1;
			this.text2 = text2;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text1;
		TextView text2;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_item_server, parent, false);
			text1 = (TextView) convertView.findViewById(R.id.text1);
			text2 = (TextView) convertView.findViewById(R.id.text2);
			convertView.setTag(new ViewHolder(text1, text2));
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			text1 = viewHolder.text1;
			text2 = viewHolder.text2;
		}

		Server e = list.get(position);
		text1.setText(e.getHost());
		long time = e.getOldestTime();
		String oldestTime;
		if (time > 0) {
			oldestTime = format.format(time);
		} else {
			oldestTime = context.getString(R.string.never);
		}
		text2.setText(context.getString(R.string.last_update, oldestTime));
		return convertView;
	}
}
