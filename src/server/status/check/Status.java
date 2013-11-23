package server.status.check;

public class Status {
	public enum Result {
		PASS, FAIL, INCONCLUSIVE
	}

	public final Result result;
	public final String reason;

	private Status(Result result, String reason) {
		this.result = result;
		this.reason = reason;
	}

	public static Status pass() {
		return new Status(Result.PASS, "");
	}

	public static Status fail(String reason) {
		return new Status(Result.FAIL, reason);
	}

	public static Status inconclusive(Exception e) {
		e.printStackTrace();
		return new Status(Result.INCONCLUSIVE, e.getLocalizedMessage());
	}

	public static Status inconclusive(String reason) {
		return new Status(Result.INCONCLUSIVE, reason);
	}

	@Override
	public String toString() {
		return result + " " + reason;
	}
}
