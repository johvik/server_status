package server.status.check;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import server.status.R;
import server.status.Settings;

public class Socket extends Checker {
	private int port;

	public Socket(int port) {
		this(-1, port);
	}

	public Socket(long id, int port) {
		super(id);
		this.port = port;
	}

	private Socket(Parcel in) {
		this(in.readLong(), in.readInt());
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public Status check(String host, Settings settings) {
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(host, port);
			java.net.Socket s = new java.net.Socket();
			s.connect(socketAddress, settings.getTimeoutMS());
			s.close();
			return Status.pass();
		} catch (SocketTimeoutException e) {
			return Status.fail("Timeout");
		} catch (ConnectException e) {
			return Status.fail("Connection failure");
		} catch (IOException e) {
			return Status.inconclusive(e);
		}
	}

	@Override
	public String toString() {
		return "Socket " + port;
	}

	@Override
	public Type getType() {
		return Type.SOCKET;
	}

	@Override
	public String getArgs() {
		return String.valueOf(port);
	}

	@Override
	public String getName(Context context) {
		return context.getString(R.string.checker_socket);
	}

	public static Socket parse(long id, String args) {
		String[] split = args.split(" ");
		int port;
		if (split.length == 1) {
			port = Integer.parseInt(split[0]);
		} else {
			port = 8000; // What to use here?
		}
		return new Socket(id, port);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(getId());
		dest.writeInt(port);
	}

	public static final Parcelable.Creator<Socket> CREATOR = new Creator<Socket>() {
		@Override
		public Socket[] newArray(int size) {
			return new Socket[size];
		}

		@Override
		public Socket createFromParcel(Parcel source) {
			return new Socket(source);
		}
	};
}
