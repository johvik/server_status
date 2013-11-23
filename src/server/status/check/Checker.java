package server.status.check;

import server.status.Settings;

public interface Checker {

	/**
	 * Run a check on host.
	 * 
	 * @param host
	 *            The host to check.
	 * @param settings
	 *            The global settings.
	 * @return Result is PASS if the check is OK, FAIL if it wasn't OK and
	 *         INCONCLUSIVE if it is worth to try again and the exception is
	 *         saved as the reason.
	 */
	public Status check(String host, Settings settings);
}
