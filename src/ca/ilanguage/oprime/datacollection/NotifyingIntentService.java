package ca.ilanguage.oprime.datacollection;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.util.ByteArrayBuffer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class NotifyingIntentService extends IntentService {
	protected boolean D = ca.ilanguage.oprime.Config.D;
	protected boolean useSelfSignedCertificates = false;
	protected int notificationId;
	protected String statusMessage;
	protected Notification noti;
	protected String userFriendlyErrorMessage;
	protected JsonArray resultsJSON;
	protected Intent tryAgain;
	protected int keystoreResourceId;
	protected static JsonParser jsonParser = new JsonParser();

	public NotifyingIntentService(String name) {
		super(name);
	}

	public NotifyingIntentService() {
		super("NotifyingIntentService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		this.notificationId = (int) System.currentTimeMillis();
		if (this.statusMessage == null) {
			this.statusMessage = "Starting service...";
		}
		// tryUploadAgain
		PendingIntent pIntent = PendingIntent.getService(this, 323132,
				tryAgain, Intent.FLAG_ACTIVITY_NO_HISTORY);

		// NOTIFICATION
		RemoteViews notificationView = new RemoteViews(getPackageName(),
				R.layout.notification);
		notificationView.setTextViewText(R.id.notification_text,
				this.statusMessage);
		notificationView.setTextViewText(R.id.notification_title,
				this.statusMessage);
		this.noti = new NotificationCompat.Builder(this)
				.setTicker(statusMessage).setContent(notificationView)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.build();
		this.noti.flags = Notification.FLAG_AUTO_CANCEL;
		this.notifyUser(statusMessage, this.noti, notificationId, false);
		this.userFriendlyErrorMessage = "";

		CookieManager cookieManager = new CookieManager();
		// cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);

		if (useSelfSignedCertificates) {
			KeyStore trustedSelfSignedCertificates;
			try {
				trustedSelfSignedCertificates = KeyStore.getInstance("BKS");
				InputStream in = getApplicationContext().getResources()
						.openRawResource(keystoreResourceId);
				trustedSelfSignedCertificates.load(in,
						Config.KEYSTORE_PASS.toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustedSelfSignedCertificates);
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), null);
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
						.getSocketFactory());
			} catch (KeyStoreException e1) {
				this.userFriendlyErrorMessage = "Problem opening key store to contact the server.";
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				this.userFriendlyErrorMessage = "Problem decoding key store to contact the server.";
				e1.printStackTrace();
			} catch (CertificateException e1) {
				this.userFriendlyErrorMessage = "Problem opening ssl certificate to contact the server.";
				e1.printStackTrace();
			} catch (KeyManagementException e1) {
				this.userFriendlyErrorMessage = "Problem opening key manager to contact the server.";
				e1.printStackTrace();
			} catch (IOException e) {
				this.userFriendlyErrorMessage = "Problem reading key store to contact the server.";
				e.printStackTrace();
			}
		}

	}

	/*
	 * http://developer.android.com/reference/java/net/HttpURLConnection.html
	 */
	public void getCouchCookie(String username, String password, String authUrl) {
		String urlStringAuthenticationSession = authUrl;
		URL url;
		HttpURLConnection urlConnection;
		try {
			url = new URL(urlStringAuthenticationSession);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection
					.setRequestProperty("Content-Type", "application/json");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem determining which server to contact, please report this error.";
			return;
		} catch (ProtocolException e) {
			this.userFriendlyErrorMessage = "Problem using POST, please report this error.";
			e.printStackTrace();
			return;
		} catch (IOException e) {
			this.userFriendlyErrorMessage = "Problem opening connection to server, please report this error.";
			e.printStackTrace();
			return;
		}

		JsonObject jsonParam = new JsonObject();
		jsonParam.addProperty("name", username);
		jsonParam.addProperty("password", password);
		DataOutputStream printout;
		try {
			printout = new DataOutputStream(urlConnection.getOutputStream());
			String jsonString = jsonParam.toString();
			printout.write(jsonString.getBytes());
			printout.flush();
			printout.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem writing to the server connection.";
			return;
		}
		String JSONResponse = this.processResponse(url, urlConnection);
		if (!"".equals(this.userFriendlyErrorMessage)) {
			return;
		}

		/* TODO use the server's actual error message */
		if (JSONResponse == null || !JSONResponse.startsWith("{\"ok\":true")) {
			this.userFriendlyErrorMessage = "Problem logging in  "
					+ JSONResponse;
			return;
		}
	}

	public String processResponse(URL url, HttpURLConnection urlConnection) {
		if (!url.getHost().equals(urlConnection.getURL().getHost())) {
			Log.d(Config.TAG,
					"We were redirected! Kick the user out to the browser to sign on?");
		}
		/* Open the input or error stream */
		int status;
		try {
			status = urlConnection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem getting server resonse code.";
			return null;
		}
		if (Config.D) {
			Log.d(Config.TAG, "Server status code " + status);
		}
		this.statusMessage = "Downloading.";
		BufferedInputStream reader;
		try {
			if (status < 400 && urlConnection.getInputStream() != null) {
				reader = new BufferedInputStream(urlConnection.getInputStream());
			} else {
				this.userFriendlyErrorMessage = "Server replied " + status;
				reader = new BufferedInputStream(urlConnection.getErrorStream());
			}
			this.notifyUser(this.statusMessage, this.noti, notificationId,
					false);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int read = 0;
			int bufSize = 512;
			byte[] buffer = new byte[bufSize];
			while (true) {
				read = reader.read(buffer);
				if (read == -1) {
					break;
				}
				baf.append(buffer, 0, read);
			}
			String JSONResponse = new String(baf.toByteArray());
			Log.d(Config.TAG, url + ":::" + JSONResponse);
			return JSONResponse;
		} catch (IOException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem writing to the server connection.";
			return null;
		}
	}

	public void notifyUser(String message, Notification notification, int id,
			boolean showTryAgain) {
		if (Config.D) {
			Log.d(Config.TAG, message);
		}
		notification.tickerText = message;
		notification.contentView.setTextViewText(R.id.notification_text,
				message);
		if (showTryAgain) {
			notification.contentView.setTextViewText(R.id.notification_title,
					"Try again?");
		}
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
				id, notification);
	}
}
