package server.status.check;

import java.io.IOException;

import server.status.Settings;

public class Ping implements Checker {

	@Override
	public Status check(String host, Settings settings) {
		try {
			Process p = Runtime.getRuntime().exec(
					"ping -c 1 -w " + settings.getTimeout() + " " + host);
			int exitCode = p.waitFor();
			// Exit code 0 means OK
			if (0 == exitCode) {
				return Status.pass();
			}
			return Status.fail("Timeout");
		} catch (IOException e) {
			return Status.inconclusive(e);
		} catch (InterruptedException e) {
			return Status.inconclusive("Interrupted");
		}
	}

	@Override
	public String toString() {
		return "Ping";
	}
}
