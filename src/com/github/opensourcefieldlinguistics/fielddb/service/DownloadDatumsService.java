package com.github.opensourcefieldlinguistics.fielddb.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.acra.ACRA;
import org.apache.http.util.ByteArrayBuffer;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadDatumsService extends IntentService {
	private boolean useSelfSignedCertificates = false;

	public DownloadDatumsService(String name) {
		super(name);
	}

	public DownloadDatumsService() {
		super("DownloadDatumsService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		if (Config.D) {
			Log.d(Config.TAG, "Inside DownloadDatumsService intent");
		}
		String datumTagToDownload = "SampleData";

		int notificationId = (int) System.currentTimeMillis();
		String uploadStatusMessage = "Downloading sample "
				+ Config.USER_FRIENDLY_DATA_NAME;

		// tryUploadAgain
		Intent tryUploadAgain = new Intent(this, DownloadDatumsService.class);

		PendingIntent pIntent = PendingIntent.getService(this, 323813,
				tryUploadAgain, Intent.FLAG_ACTIVITY_NO_HISTORY);

		// NOTIFICATION
		RemoteViews notificationView = new RemoteViews(getPackageName(),
				R.layout.notification);
		notificationView.setTextViewText(R.id.notification_text,
				"Preparing download");
		notificationView
				.setTextViewText(R.id.notification_title, "Downloading");
		Notification noti = new NotificationCompat.Builder(this)
				.setTicker(uploadStatusMessage).setContent(notificationView)
				.setSmallIcon(R.drawable.ic_oprime).setContentIntent(pIntent)
				.build();
		noti.flags = Notification.FLAG_AUTO_CANCEL;
		notifyUser(uploadStatusMessage, noti, notificationId, false);

		String userFriendlyErrorMessage = "";
		String urlString = Config.DEFAULT_SAMPLE_DATA_URL + "?key=%22"
				+ datumTagToDownload + "%22";
		if (Config.D) {
			Log.d(Config.TAG, urlString);
		}
		ACRA.getErrorReporter().putCustomData("downloadDatums",
				datumTagToDownload);
		ACRA.getErrorReporter().putCustomData("urlString", urlString);
		uploadStatusMessage = "Contacting server";
		notifyUser(uploadStatusMessage, noti, notificationId, false);

		/*
		 * http://developer.android.com/reference/java/net/HttpURLConnection.
		 * html
		 */
		try {
			URL url = new URL(urlString);
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							Config.DEFAULT_PUBLIC_USERNAME,
							Config.DEFAULT_PUBLIC_USER_PASS.toCharArray());
				}
			});

			CookieManager cookieManager = new CookieManager();
			// cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(cookieManager);

			HttpsURLConnection urlConnection = (HttpsURLConnection) url
					.openConnection();
			// urlConnection
			// .setRequestProperty(
			// "User-Agent",
			// "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.2 Safari/537.36 ( compatible ) Android App");
			// urlConnection.setRequestProperty("Accept", "*/*");

			if (useSelfSignedCertificates) {
				KeyStore trusted = KeyStore.getInstance("BKS");
				InputStream in = getApplicationContext().getResources()
						.openRawResource(R.raw.sslkeystore);
				trusted.load(in, Config.KEYSTORE_PASS.toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trusted);
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), null);
				urlConnection
						.setSSLSocketFactory(sslContext.getSocketFactory());
			}

			try {
				if (!url.getHost().equals(urlConnection.getURL().getHost())) {
					Log.d(Config.TAG,
							"We were redirected! Kick the user out to the browser to sign on?");
				}

				/* Open the input or error stream */
				int status = urlConnection.getResponseCode();
				if (Config.D) {
					Log.d(Config.TAG, "Server status code " + status);
				}
				uploadStatusMessage = "Server contacted.";
				BufferedInputStream reader;
				if (status < 400 && urlConnection.getInputStream() != null) {
					reader = new BufferedInputStream(
							urlConnection.getInputStream());
				} else {
					userFriendlyErrorMessage = "Server replied " + status;
					reader = new BufferedInputStream(
							urlConnection.getErrorStream());
				}
				notifyUser(uploadStatusMessage, noti, notificationId, false);

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
				userFriendlyErrorMessage = this
						.processCouchDBMapResponse(JSONResponse);

			} catch (IOException e) {
				userFriendlyErrorMessage = "Problem reading server response.";
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
		} catch (MalformedURLException e1) {
			userFriendlyErrorMessage = "Problem determining which server to contact, please report this error.";
			e1.printStackTrace();
		} catch (IOException e1) {
			userFriendlyErrorMessage = "Problem contacting the server.";
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			userFriendlyErrorMessage = "Problem opening key store to contact the server.";
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			userFriendlyErrorMessage = "Problem decoding key store to contact the server.";
			e1.printStackTrace();
		} catch (CertificateException e1) {
			userFriendlyErrorMessage = "Problem opening ssl certificate to contact the server.";
			e1.printStackTrace();
		} catch (KeyManagementException e1) {
			userFriendlyErrorMessage = "Problem opening key manager to contact the server.";
			e1.printStackTrace();
		}

		/*
		 * Displays error if exists upon upload, otherwise cancels the
		 * notification
		 */
		if (!"".equals(userFriendlyErrorMessage)) {
			notifyUser(" " + userFriendlyErrorMessage, noti, notificationId,
					true);
			// ACRA.getErrorReporter().handleException(
			// new Exception(userFriendlyErrorMessage));

		} else {
			/* Success: remove the notification */
			// ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
			// .cancel(notificationId);
		}
	}

	public String processCouchDBMapResponse(String responseJSON) {
		// ACRA.getErrorReporter().handleException(
		// new Exception("*** Download Data Completed ***"));
		return "";
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
