package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Status;
import server.status.check.Status.Result;
import android.util.Log;

public class Server {
	private String host;
	private ArrayList<Checker> checkers = new ArrayList<Checker>();
	private ArrayList<Status> results = new ArrayList<Status>();

	public Server(String host) {
		this.host = host;
	}

	public void addChecker(Checker checker) {
		checkers.add(checker);
		results.add(Status.inconclusive("Not tested"));
	}

	public void check(Settings settings) {
		int ok = 0;
		int size = checkers.size();
		for (int i = 0; i < size; i++) {
			Log.d("Sever", "Checking..." + i);
			Checker checker = checkers.get(i);
			Status status = checker.check(host, settings);
			results.set(i, status);
			// TODO How to report results?
			if (Result.PASS == status.result) {
				++ok;
			}
			Log.d("Server", checker.toString() + " " + host + " " + status);
		}
		Log.d("Server", host + " " + (ok == size));
	}
}
