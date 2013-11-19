package server.status.check;

public interface Checker {
	/**
	 * Run a check.
	 * 
	 * @return True if the check is OK otherwise false.
	 */
	public boolean check();
}
