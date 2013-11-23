package server.status.check;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import server.status.Settings;

public class Http implements Checker {
	static {
		// Do not follow redirects
		HttpURLConnection.setFollowRedirects(false);
	}

	private int port;
	private int responseCode;

	public Http(int port, int responseCode) {
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
}
