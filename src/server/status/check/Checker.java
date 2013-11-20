package server.status.check;

public interface Checker {
	public enum Result {
		PASS, FAIL, INCONCLUSIVE
	}

	/**
	 * Run a check.
	 * 
	 * @return PASS if the check is OK, FAIL if it wasn't OK and INCONCLUSIVE if
	 *         it is worth to try again.
	 */
	public Result check();
}
