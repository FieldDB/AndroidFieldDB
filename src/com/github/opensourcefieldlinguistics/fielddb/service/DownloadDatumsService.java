package com.github.opensourcefieldlinguistics.fielddb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import ca.ilanguage.oprime.datacollection.SecureHttpClient;

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

		String datumTagToDownload = "SampleData";
		ACRA.getErrorReporter().putCustomData("downloadDatums",
				datumTagToDownload);
		ACRA.getErrorReporter().handleException(
				new Exception("*** Download Data Started ***"));

		SecureHttpClient httpClient = new SecureHttpClient(
				getApplicationContext());
		httpClient.setKeystoreIdandPassword(R.raw.sslkeystore,
				Config.KEYSTORE_PASS);

		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(Config.DEFAULT_SAMPLE_DATA_URL
				+ datumTagToDownload);
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null,
				Charset.forName("UTF-8"));

		String userFriendlyErrorMessage = "";
		uploadStatusMessage = "Contacting server";
		notifyUser(uploadStatusMessage, noti, notificationId, false);

		HttpResponse response;
		try {
			response = httpClient.execute(httpGet, localContext);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			uploadStatusMessage = "Server contacted";
			notifyUser(uploadStatusMessage, noti, notificationId, false);
			String JSONResponse = "";
			String newLine;
			do {
				newLine = reader.readLine();
				if (newLine != null) {
					JSONResponse += newLine;
				}
			} while (newLine != null);
			Log.d(Config.TAG, JSONResponse);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * Displays enotificationManagerrror if exists upon upload, otherwise
		 * cancels the notification
		 */
		if (!"".equals(userFriendlyErrorMessage)) {
			notifyUser(" " + userFriendlyErrorMessage, noti, notificationId,
					true);
		} else {
			/* Success: remove the notification */
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(notificationId);
		}
	}

	public void notifyUser(String message, Notification notification, int id,
			boolean showTryAgain) {
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
