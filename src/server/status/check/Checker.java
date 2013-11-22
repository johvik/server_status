package server.status.check;

import server.status.Settings;

public interface Checker {
	public enum Result {
		PASS, FAIL, INCONCLUSIVE
	}

	/**
	 * Run a check on host.
	 * 
	 * @param host
	 *            The host to check.
	 * @param settings
	 *            The global settings.
	 * @return PASS if the check is OK, FAIL if it wasn't OK and INCONCLUSIVE if
	 *         it is worth to try again.
	 */
	public Result check(String host, Settings settings);
}
