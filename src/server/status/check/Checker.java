package server.status.check;

import server.status.Settings;

public abstract class Checker {
	public enum Type {
		HTTP, HTTPS, PING, SOCKET;

		private static final Type[] TYPE_VALUES = Type.values();

		public static Type parse(int i) {
			return TYPE_VALUES[i % TYPE_VALUES.length];
		}
	}

	private long id;

	public Checker(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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
	public abstract Status check(String host, Settings settings);

	public abstract Type getType();

	public abstract String getArgs();

	public static Checker parse(long id, Type type, String args) {
		switch (type) {
		case HTTP:
			return Http.parse(id, args);
		case HTTPS:
			return Https.parse(id, args);
		case PING:
			return Ping.parse(id, args);
		case SOCKET:
			return Socket.parse(id, args);
		default:
			return null;
		}
	}
}
