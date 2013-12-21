package server.status.check;

import java.io.IOException;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import server.status.R;
import server.status.Settings;

public class Ping extends Checker {

	public Ping() {
		this(-1);
	}

	public Ping(long id) {
		super(id);
	}

	private Ping(Parcel in) {
		this(in.readLong());
	}

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

	@Override
	public Type getType() {
		return Type.PING;
	}

	@Override
	public String getArgs() {
		return "";
	}

	@Override
	public String getName(Context context) {
		return context.getString(R.string.checker_ping);
	}

	public static Ping parse(long id, String args) {
		return new Ping(id);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(getId());
	}

	public static final Parcelable.Creator<Ping> CREATOR = new Creator<Ping>() {
		@Override
		public Ping[] newArray(int size) {
			return new Ping[size];
		}

		@Override
		public Ping createFromParcel(Parcel source) {
			return new Ping(source);
		}
	};
}
