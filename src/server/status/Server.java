package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import server.status.db.ServerDbHelper;
import android.content.Context;
import android.content.Intent;

public class Server implements Comparable<Server> {
	public static final String INTENT_ID = "sid";
	public static final String BROADCAST_UPDATE = "server.status.UPDATE";

	private long id;
	private String host;
	private ArrayList<Checker> checkers = new ArrayList<Checker>();
	private ArrayList<Status> results = new ArrayList<Status>();

	public Server(long id, String host) {
		this.id = id;
		this.host = host;
	}

	public Server(String host) {
		this(-1, host);
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

	public void addChecker(Checker checker) {
		add(checker, Status.initial());
	}

	public void add(Checker checker, Status status) {
		checkers.add(checker);
		results.add(status);
	}

	public void clear() {
		checkers.clear();
		results.clear();
	}

	public void check(Settings settings, Context context) {
		int size = checkers.size();
		for (int i = 0; i < size; i++) {
			Checker checker = checkers.get(i);
			Status status = checker.check(host, settings);
			boolean saved = ServerDbHelper.getInstance(context).save(this,
					checker, status);
			if (saved) {
				results.set(i, status);
				// Send update
				Intent intent = new Intent(BROADCAST_UPDATE);
				intent.putExtra(INTENT_ID, id);
				context.sendBroadcast(intent);
			}
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
