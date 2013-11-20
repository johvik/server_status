package server.status;

public class Settings {
	private static int timeoutMS;
	private static int timeout;
	static {
		// Default to 5000ms
		setTimeoutMS(5000);
	}

	public static void setTimeoutMS(int timeoutMS) {
		Settings.timeoutMS = timeoutMS;
		timeout = Math.max(1, timeoutMS / 1000);
	}

	public static int getTimeoutMS() {
		return timeoutMS;
	}

	public static int getTimeout() {
		return timeout;
	}
}
