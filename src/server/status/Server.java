package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import server.status.db.ServerData;
import server.status.ui.FailNotification;
import android.content.Context;
import android.util.Pair;

public class Server implements Comparable<Server> {
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

	public static Server fromId(long id) {
		return new Server(id, "", false);
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

	public void setChecker(int index, Checker checker) {
		int size = checkers.size();
		if (index >= 0 && index < size) {
			checkers.set(index, checker);
		}
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

	public void checkSingle(Settings settings, Context context, int index) {
		int size = checkers.size();
		if (index >= 0 && index < size) {
			Checker checker = checkers.get(index);
			Status status = checker.check(host, settings);
			boolean saved = ServerData.getInstance().saveSync(context, this,
					checker, status);
			if (saved) {
				results.set(index, status);
			}
		}
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
		ServerData serverData = ServerData.getInstance();
		serverData.updateSync(context, this);

		// Run all checkers and update results
		for (int i = 0; i < size; i++) {
			Checker checker = checkers.get(i);
			Status status = checker.check(host, settings);
			// Done when last has been checked
			if (i + 1 == size) {
				checkRunning = false;
			}
			boolean saved = serverData.saveSync(context, this, checker, status);
			if (saved) {
				results.set(i, status);
				if (status.result == Result.PASS) {
					count++;
				}
			}
		}
		if (count < size) {
			// Something failed, show notification
			FailNotification.show(context, settings, this);
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

	@Override
	public boolean equals(Object o) {
		if (o instanceof Server) {
			Server other = (Server) o;
			return id == other.id;
		}
		return false;
	}
}
