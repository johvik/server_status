package server.status.check;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import server.status.Settings;

public class Socket implements Checker {
	private InetSocketAddress socketAddress;

	public Socket(String host, int port) {
		socketAddress = new InetSocketAddress(host, port);
	}

	@Override
	public Result check() {
		try {
			java.net.Socket s = new java.net.Socket();
			s.connect(socketAddress, Settings.getTimeoutMS());
			s.close();
			return Result.PASS;
		} catch (SocketTimeoutException e) {
			return Result.FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		}
	}
}
