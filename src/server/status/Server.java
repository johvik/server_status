package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import server.status.db.ServerDbHelper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

public class Server implements Comparable<Server> {
	public static final String INTENT_ID = "sid";
	public static final String BROADCAST_UPDATE = "server.status.UPDATE";

	private long id;
	private String host;
	private boolean checkRunning;
	private ArrayList<Checker> checkers = new ArrayList<Checker>();
	private ArrayList<Status> results = new ArrayList<Status>();

	public Server(long id, String host, boolean checkRunning) {
		this.id = id;
		this.host = host;
		this.checkRunning = checkRunning;
	}

	public Server(String host) {
		this(-1, host, false);
	}

	public String getHost() {
		return host;
	}

	public long getId() {
		return id;
	}

	public ArrayList<Checker> getCheckers() {
		return checkers;
	}

	public ArrayList<Status> getResults() {
		return results;
	}

	public int getPassCount() {
		int count = 0;
		for (Status s : results) {
			if (s.result == Result.PASS) {
				count++;
			}
		}
		return count;
	}

	public int getServerCount() {
		return results.size();
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isCheckRunning() {
		return checkRunning;
	}

	public void addChecker(Checker checker) {
		add(checker, Status.initial());
	}

	public void add(Checker checker, Status status) {
		checkers.add(checker);
		results.add(status);
	}

	public void removeChecker(int index) {
		if (index >= 0 && index < checkers.size()) {
			checkers.remove(index);
			results.remove(index);
		}
	}

	public Pair<Checker, Status> get(int index) {
		return Pair.create(checkers.get(index), results.get(index));
	}

	public void clear() {
		checkers.clear();
		results.clear();
	}

	public void check(Settings settings, Context context) {
		int count = 0;
		int size = checkers.size();
		if (size > 0) {
			checkRunning = true;
		} else {
			// No checks to run
			checkRunning = false;
		}
		// Send initial update to indicate that it started (or not)
		boolean updated = ServerDbHelper.getInstance(context).update(this);
		if (updated) {
			Intent intent = new Intent(BROADCAST_UPDATE);
			intent.putExtra(INTENT_ID, id);
			context.sendBroadcast(intent);
		}
		// Run all checkers and update results
		for (int i = 0; i < size; i++) {
			Checker checker = checkers.get(i);
			Status status = checker.check(host, settings);
			// Done when last has been checked
			if (i + 1 == size) {
				checkRunning = false;
			}
			boolean saved = ServerDbHelper.getInstance(context).save(this,
					checker, status);
			if (saved) {
				results.set(i, status);
				// Send update
				Intent intent = new Intent(BROADCAST_UPDATE);
				intent.putExtra(INTENT_ID, id);
				context.sendBroadcast(intent);
				if (status.result == Result.PASS) {
					count++;
				}
			}
		}
		if (count < size) {
			// Something failed, show notification
			Intent intent = new Intent(context, MainActivity.class);
			intent.putExtra(INTENT_ID, id);
			String host = this.host;
			if (host.trim().length() == 0) {
				host = context.getString(R.string.empty_host);
			}
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			for (int i = 0; i < size; i++) {
				Status result = results.get(i);
				if (result.result != Result.PASS) {
					inboxStyle.addLine(checkers.get(i).getName(context) + " "
							+ result.reason);
				}
			}
			String summary = context.getString(R.string.notification_text_fail,
					count, size);
			inboxStyle.setSummaryText(summary);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
					context)
					.setContentTitle(
							context.getString(R.string.notification_title_fail,
									host)).setContentText(summary)
					.setSmallIcon(R.drawable.ic_stat_fail)
					.setContentIntent(pendingIntent).setAutoCancel(true)
					.setStyle(inboxStyle);
			if (settings.notificationSound()) {
				// Play sound
				notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
			}
			notificationManager.notify((int) id, notificationBuilder.build());
		}
	}

	@Override
	public String toString() {
		return id + " " + host + " " + checkers + " " + results;
	}

	public long getOldestTime() {
		long oldestTime = Long.MAX_VALUE;
		for (Status status : results) {
			if (status.time < oldestTime) {
				oldestTime = status.time;
			}
		}
		if (oldestTime != Long.MAX_VALUE) {
			return oldestTime;
		}
		return 0;
	}

	public boolean hasFail() {
		for (Status s : results) {
			if (s.result == Result.FAIL) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInconclusive() {
		for (Status s : results) {
			if (s.result == Result.INCONCLUSIVE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Server another) {
		return Long.valueOf(id).compareTo(another.id);
	}
}
