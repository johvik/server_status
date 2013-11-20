package server.status.check;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import server.status.Settings;

public class Https implements Checker {
	static {
		// Do not follow redirects
		HttpsURLConnection.setFollowRedirects(false);
	}

	private static final TrustManager[] trustManagers = new TrustManager[] { new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	} };
	private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	private URL url;
	private int responseCode;
	private boolean allCertificates;

	public Https(String host, int port, int responseCode,
			boolean allCertificates) throws MalformedURLException {
		url = new URL("https", host, port, "");
		this.responseCode = responseCode;
		this.allCertificates = allCertificates;
	}

	@Override
	public Result check() {
		try {
			HttpsURLConnection urlConnection = (HttpsURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(Settings.getTimeoutMS());
			urlConnection.setReadTimeout(Settings.getTimeoutMS());
			urlConnection.setRequestMethod("GET");
			if (allCertificates) {
				// Set up a factory that allows all SSL certificates
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, trustManagers, null);
				urlConnection.setSSLSocketFactory(context.getSocketFactory());
				urlConnection.setHostnameVerifier(hostnameVerifier);
			}
			int responseCode = urlConnection.getResponseCode();
			urlConnection.disconnect();
			return this.responseCode == responseCode ? Result.PASS
					: Result.FAIL;
		} catch (SocketTimeoutException e) {
			return Result.FAIL;
		} catch (SSLHandshakeException e) {
			return Result.FAIL;
		} catch (IOException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return Result.INCONCLUSIVE;
		}
	}
}
