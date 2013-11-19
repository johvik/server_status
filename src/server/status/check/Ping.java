package server.status.check;

import java.io.IOException;

public class Ping implements Checker {
	private String host;

	public Ping(String host) {
		this.host = host;
	}

	@Override
	public boolean check() {
		try {
			Process p = Runtime.getRuntime().exec("ping -c 1 -w 5 " + host);
			int exitCode = p.waitFor();
			return exitCode == 0; // Exit code 0 means OK
		} catch (IOException e) {
			// Ignore errors
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Ignore errors
			e.printStackTrace();
		}
		return false;
	}
}
