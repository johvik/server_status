package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import server.status.db.ServerDbHelper;
import android.content.Context;
import android.util.Log;

public class Server {
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

	public void setId(long id) {
		this.id = id;
	}

	public void addChecker(Checker checker) {
		add(checker, Status.inconclusive("Not tested"));
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
		int ok = 0;
		int size = checkers.size();
		for (int i = 0; i < size; i++) {
			Log.d("Sever", "Checking..." + i);
			Checker checker = checkers.get(i);
			Status status = checker.check(host, settings);
			boolean saved = ServerDbHelper.getInstance(context).save(this,
					checker, status);
			if (saved) {
				results.set(i, status);
				// TODO How to report results?
			}
			if (Result.PASS == status.result) {
				++ok;
			}
			Log.d("Server", checker.toString() + " " + host + " " + status);
		}
		Log.d("Server", host + " " + (ok == size));
	}

	@Override
	public String toString() {
		return id + " " + host + " " + checkers + " " + results;
	}
}
