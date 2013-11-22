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
	public Result check(String host, Settings settings) {
		try {
			URL url = new URL("http", host, port, "");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(settings.getTimeoutMS());
			urlConnection.setReadTimeout(settings.getTimeoutMS());
			urlConnection.setRequestMethod("GET");
			int responseCode = urlConnection.getResponseCode();
			urlConnection.disconnect();
			return this.responseCode == responseCode ? Result.PASS
					: Result.FAIL;
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
		return "Http " + port + " " + responseCode;
	}
}
