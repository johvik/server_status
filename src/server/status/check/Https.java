package server.status.check;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
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

	private int port;
	private int responseCode;
	private boolean allCertificates;

	public Https(int port, int responseCode, boolean allCertificates) {
		this.port = port;
		this.responseCode = responseCode;
		this.allCertificates = allCertificates;
	}

	@Override
	public Status check(String host, Settings settings) {
		try {
			URL url = new URL("https", host, port, "");
			HttpsURLConnection urlConnection = (HttpsURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(settings.getTimeoutMS());
			urlConnection.setReadTimeout(settings.getTimeoutMS());
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
			if (this.responseCode == responseCode) {
				return Status.pass();
			}
			return Status.fail("Response code " + responseCode);
		} catch (SocketTimeoutException e) {
			return Status.fail("Timeout");
		} catch (SSLHandshakeException e) {
			return Status.fail("SSL handshake exception");
		} catch (ConnectException e) {
			return Status.fail("Connection failure");
		} catch (SSLException e) {
			return Status.fail("SSL exception");
		} catch (IOException e) {
			return Status.inconclusive(e);
		} catch (GeneralSecurityException e) {
			return Status.inconclusive(e);
		}
	}

	@Override
	public String toString() {
		return "Https " + port + " " + responseCode + " " + allCertificates;
	}
}
