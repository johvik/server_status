package server.status.check;

import java.util.Date;

public class Status {
	public enum Result {
		PASS, FAIL, INCONCLUSIVE;

		private static final Result[] RESULT_VALUES = Result.values();

		public static Result parse(int i) {
			return RESULT_VALUES[i % RESULT_VALUES.length];
		}
	}

	public final Result result;
	public final String reason;
	public final long time;

	private Status(Result result, String reason, long time) {
		this.result = result;
		this.reason = reason;
		this.time = time;
	}

	public static Status parse(Result result, String reason, long time) {
		return new Status(result, reason, time);
	}

	public static Status pass() {
		return new Status(Result.PASS, "", System.currentTimeMillis());
	}

	public static Status fail(String reason) {
		return new Status(Result.FAIL, reason, System.currentTimeMillis());
	}

	public static Status inconclusive(Exception e) {
		e.printStackTrace();
		return new Status(Result.INCONCLUSIVE, e.getLocalizedMessage(),
				System.currentTimeMillis());
	}

	public static Status inconclusive(String reason) {
		return new Status(Result.INCONCLUSIVE, reason,
				System.currentTimeMillis());
	}

	public static Status initial() {
		return new Status(Result.INCONCLUSIVE, "Not tested", 0);
	}

	@Override
	public String toString() {
		return result + " " + reason + " " + new Date(time);
	}
}
