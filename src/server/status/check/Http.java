package server.status.check;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import server.status.Settings;

public class Http implements Checker {
	static {
		// Do not follow redirects
		HttpURLConnection.setFollowRedirects(false);
	}

	private URL url;
	private int responseCode;

	public Http(String host, int port, int responseCode)
			throws MalformedURLException {
		url = new URL("http", host, port, "");
		this.responseCode = responseCode;
	}

	@Override
	public Result check() {
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(Settings.getTimeoutMS());
			urlConnection.setReadTimeout(Settings.getTimeoutMS());
			urlConnection.setRequestMethod("GET");
			int responseCode = urlConnection.getResponseCode();
			urlConnection.disconnect();
			return this.responseCode == responseCode ? Result.PASS
					: Result.FAIL;
		} catch (SocketTimeoutException e) {
			return Result.FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		}
	}
}
