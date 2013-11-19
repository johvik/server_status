package server.status.check;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Socket implements Checker {
	private InetSocketAddress socketAddress;

	public Socket(String host, int port) {
		socketAddress = new InetSocketAddress(host, port);
	}

	@Override
	public boolean check() {
		try {
			java.net.Socket s = new java.net.Socket();
			s.connect(socketAddress, 5000);
			s.close();
			return true;
		} catch (IOException e) {
			// Ignore errors
			e.printStackTrace();
		}
		return false;
	}
}
