package server.status.check;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import server.status.Settings;

public class Http extends Checker {
	static {
		// Do not follow redirects
		HttpURLConnection.setFollowRedirects(false);
	}

	private int port;
	private int responseCode;

	public Http(int port, int responseCode) {
		this(-1, port, responseCode);
	}

	public Http(long id, int port, int responseCode) {
		super(id);
		this.port = port;
		this.responseCode = responseCode;
	}

	@Override
	public Status check(String host, Settings settings) {
		try {
			URL url = new URL("http", host, port, "");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(settings.getTimeoutMS());
			urlConnection.setReadTimeout(settings.getTimeoutMS());
			urlConnection.setRequestMethod("GET");
			int responseCode = urlConnection.getResponseCode();
			urlConnection.disconnect();
			if (this.responseCode == responseCode) {
				return Status.pass();
			}
			return Status.fail("Response code " + responseCode);
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
		return "Http " + port + " " + responseCode;
	}

	@Override
	public Type getType() {
		return Type.HTTP;
	}

	@Override
	public String getArgs() {
		return port + " " + responseCode;
	}

	public static Http parse(long id, String args) {
		String[] split = args.split(" ");
		int port;
		int responseCode;
		if (split.length == 2) {
			port = Integer.parseInt(split[0]);
			responseCode = Integer.parseInt(split[1]);
		} else {
			port = 80;
			responseCode = 200;
		}
		return new Http(id, port, responseCode);
	}
}
