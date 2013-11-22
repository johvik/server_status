package server.status;

import java.util.ArrayList;

import server.status.check.Checker;
import server.status.check.Checker.Result;
import android.util.Log;

public class Server {
	private String host;
	private ArrayList<Checker> checkers = new ArrayList<Checker>();
	private ArrayList<Result> results = new ArrayList<Result>();

	public Server(String host) {
		this.host = host;
	}

	public void addChecker(Checker checker) {
		checkers.add(checker);
		results.add(Result.INCONCLUSIVE);
	}

	public void check(Settings settings) {
		int ok = 0;
		int size = checkers.size();
		for (int i = 0; i < size; i++) {
			Log.d("Sever", "Checking..." + i);
			Checker checker = checkers.get(i);
			Result result = checker.check(host, settings);
			results.set(i, result);
			// TODO How to report results?
			if (Result.PASS == result) {
				++ok;
			}
			Log.d("Server", checker.toString() + " " + host + " " + result);
		}
		Log.d("Server", host + " " + (ok == size));
	}
}
