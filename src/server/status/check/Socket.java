package server.status.check;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import server.status.Settings;

public class Socket implements Checker {
	private int port;

	public Socket(int port) {
		this.port = port;
	}

	@Override
	public Result check(String host, Settings settings) {
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(host, port);
			java.net.Socket s = new java.net.Socket();
			s.connect(socketAddress, settings.getTimeoutMS());
			s.close();
			return Result.PASS;
		} catch (SocketTimeoutException e) {
			return Result.FAIL;
		} catch (ConnectException e) {
			return Result.FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		}
	}

	@Override
	public String toString() {
		return "Socket " + port;
	}
}
