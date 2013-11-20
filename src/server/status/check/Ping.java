package server.status.check;

import java.io.IOException;

import server.status.Settings;

public class Ping implements Checker {
	private String host;

	public Ping(String host) {
		this.host = host;
	}

	@Override
	public Result check() {
		try {
			Process p = Runtime.getRuntime().exec(
					"ping -c 1 -w " + Settings.getTimeout() + " " + host);
			int exitCode = p.waitFor();
			// Exit code 0 means OK
			return exitCode == 0 ? Result.PASS : Result.FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		}
	}
}
