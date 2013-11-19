package server.status.check;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Http implements Checker {
	static {
		// Do not follow redirects
		HttpURLConnection.setFollowRedirects(false);
	}

	private URL url;

	public Http(String host, int port) throws MalformedURLException {
		url = new URL("http", host, port, "");
	}

	@Override
	public boolean check() {
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setReadTimeout(5000);
			urlConnection.setRequestMethod("GET");
			int responseCode = urlConnection.getResponseCode();
			return responseCode != -1;
		} catch (IOException e) {
			// Ignore errors
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return false;
	}
}
