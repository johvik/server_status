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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import server.status.R;
import server.status.Settings;

public class Https extends Checker {
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
		this(-1, port, responseCode, allCertificates);
	}

	public Https(long id, int port, int responseCode, boolean allCertificates) {
		super(id);
		this.port = port;
		this.responseCode = responseCode;
		this.allCertificates = allCertificates;
	}

	private Https(Parcel in) {
		this(in.readLong(), in.readInt(), in.readInt(), in.readInt() == 1);
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

	@Override
	public Type getType() {
		return Type.HTTPS;
	}

	@Override
	public String getArgs() {
		return port + " " + responseCode + " " + allCertificates;
	}

	@Override
	public String getName(Context context) {
		return context.getString(R.string.checker_https);
	}

	public static Https parse(long id, String args) {
		String[] split = args.split(" ");
		int port;
		int responseCode;
		boolean allCertificates;
		if (split.length == 3) {
			port = Integer.parseInt(split[0]);
			responseCode = Integer.parseInt(split[1]);
			allCertificates = Boolean.parseBoolean(split[2]);
		} else {
			port = 443;
			responseCode = 200;
			allCertificates = false;
		}
		return new Https(id, port, responseCode, allCertificates);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(getId());
		dest.writeInt(port);
		dest.writeInt(responseCode);
		dest.writeInt(allCertificates ? 1 : 0);
	}

	public static final Parcelable.Creator<Https> CREATOR = new Creator<Https>() {
		@Override
		public Https[] newArray(int size) {
			return new Https[size];
		}

		@Override
		public Https createFromParcel(Parcel source) {
			return new Https(source);
		}
	};
}
