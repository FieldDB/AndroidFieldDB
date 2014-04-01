package com.github.opensourcefieldlinguistics.fielddb.service;

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
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.acra.ACRA;
import org.apache.http.util.ByteArrayBuffer;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadDatumsService extends IntentService {
	private boolean useSelfSignedCertificates = false;
	int notificationId;
	String uploadStatusMessage;
	String datumTagToDownload;
	Notification noti;
	String userFriendlyErrorMessage;
	String urlStringSampleDataDownload;
	JsonArray resultsJSON;
	ArrayList<String> additionalDownloads;

	private static JsonParser jsonParser = new JsonParser();

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
		this.datumTagToDownload = "SampleData";
		this.notificationId = (int) System.currentTimeMillis();
		this.uploadStatusMessage = "Downloading sample "
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
		this.noti = new NotificationCompat.Builder(this)
				.setTicker(uploadStatusMessage).setContent(notificationView)
				.setSmallIcon(R.drawable.ic_oprime).setContentIntent(pIntent)
				.build();
		this.noti.flags = Notification.FLAG_AUTO_CANCEL;
		this.notifyUser(uploadStatusMessage, this.noti, notificationId, false);

		this.userFriendlyErrorMessage = "";
		this.urlStringSampleDataDownload = Config.DEFAULT_SAMPLE_DATA_URL
				+ "?key=%22" + datumTagToDownload + "%22";
		if (Config.D) {
			Log.d(Config.TAG, this.urlStringSampleDataDownload);
		}
		ACRA.getErrorReporter().putCustomData("downloadDatums",
				datumTagToDownload);
		ACRA.getErrorReporter().putCustomData("urlString",
				this.urlStringSampleDataDownload);
		this.uploadStatusMessage = "Contacting server";
		this.notifyUser(uploadStatusMessage, this.noti, this.notificationId,
				false);

		CookieManager cookieManager = new CookieManager();
		// cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);

		if (useSelfSignedCertificates) {
			KeyStore trustedSelfSignedCertificates;
			try {
				trustedSelfSignedCertificates = KeyStore.getInstance("BKS");
				InputStream in = getApplicationContext().getResources()
						.openRawResource(R.raw.sslkeystore);
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

		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			// ACRA.getErrorReporter().handleException(
			// new Exception(this.userFriendlyErrorMessage));
			return;
		}

		this.getCouchCookie();
		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);// ACRA.getErrorReporter().handleException(
			// new Exception(this.userFriendlyErrorMessage));
			return;
		}

		this.getSampleData();
		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			// ACRA.getErrorReporter().handleException(
			// new Exception(this.userFriendlyErrorMessage));
			return;
		}

		this.processCouchDBMapResponse();
		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			// ACRA.getErrorReporter().handleException(
			// new Exception(this.userFriendlyErrorMessage));
			return;
		}

		/* Success: remove the notification */
		// ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
		// .cancel(notificationId);
	}

	/*
	 * http://developer.android.com/reference/java/net/HttpURLConnection.html
	 */
	public void getCouchCookie() {
		String urlStringAuthenticationSession = Config.DEFAULT_DATA_LOGIN;
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
		jsonParam.addProperty("name", Config.DEFAULT_PUBLIC_USERNAME);
		jsonParam.addProperty("password", Config.DEFAULT_PUBLIC_USER_PASS);
		DataOutputStream printout;
		try {
			printout = new DataOutputStream(urlConnection.getOutputStream());
			String jsonString = jsonParam.toString();
			Log.d(Config.TAG, jsonString);
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

	public void getSampleData() {
		URL url;
		try {
			url = new URL(this.urlStringSampleDataDownload);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem determining which server to contact, please report this error.";
			return;
		}

		HttpURLConnection urlConnection;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			this.userFriendlyErrorMessage = "Problem contacting the server to download sample data.";
			return;
		}

		String JSONResponse = this.processResponse(url, urlConnection);
		if (!"".equals(this.userFriendlyErrorMessage)) {
			return;
		}
		if (JSONResponse == null) {
			this.userFriendlyErrorMessage = "Unknown error reading sample data from server";
			return;
		}
		JsonObject json = (JsonObject) jsonParser.parse(JSONResponse);
		this.resultsJSON = json.getAsJsonArray("rows");
		return;
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
		this.uploadStatusMessage = "Server contacted.";
		BufferedInputStream reader;
		try {
			if (status < 400 && urlConnection.getInputStream() != null) {
				reader = new BufferedInputStream(urlConnection.getInputStream());
			} else {
				this.userFriendlyErrorMessage = "Server replied " + status;
				reader = new BufferedInputStream(urlConnection.getErrorStream());
			}
			this.notifyUser(this.uploadStatusMessage, this.noti,
					notificationId, false);

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

	public void processCouchDBMapResponse() {
		if (this.resultsJSON == null || this.resultsJSON.size() == 0) {
			this.userFriendlyErrorMessage = "The sample data was empty, please report this.";
			return;
		}
		JsonObject datumJson;
		String id = "";
		Uri uri;
		String[] datumProjection = { DatumTable.COLUMN_ID };
		Cursor cursor;
		String mediaFilesAsString = "";
		ContentValues datumAsValues;
		additionalDownloads = new ArrayList<String>();
		for (int row = 0; row < this.resultsJSON.size(); row++) {
			datumJson = (JsonObject) this.resultsJSON.get(row);
			datumJson = datumJson.getAsJsonObject("value");
			id = datumJson.get("_id").getAsString();
			uri = Uri.withAppendedPath(DatumContentProvider.CONTENT_URI, id);
			cursor = getContentResolver().query(uri, datumProjection, null,
					null, null);

			// TODO instead, update it, without losing info
			if (cursor == null || cursor.getCount() <= 0) {
				/* save it */
				try {
					datumAsValues = new ContentValues();
					datumAsValues.put(DatumTable.COLUMN_ID, id);
					datumAsValues.put(DatumTable.COLUMN_REV,
							datumJson.get("_rev").getAsString());
					datumAsValues.put(DatumTable.COLUMN_CREATED_AT, datumJson
							.get("created_at").getAsString());
					datumAsValues.put(DatumTable.COLUMN_UPDATED_AT, datumJson
							.get("updated_at").getAsString());
					datumAsValues.put(
							DatumTable.COLUMN_APP_VERSIONS_WHEN_MODIFIED,
							datumJson.get("appVersionsWhenModified")
									.getAsString());
					datumAsValues.put(DatumTable.COLUMN_RELATED,
							datumJson.get("related").getAsString());

					datumAsValues.put(DatumTable.COLUMN_UTTERANCE, datumJson
							.get("utterance").getAsString());
					datumAsValues.put(DatumTable.COLUMN_MORPHEMES, datumJson
							.get("morphemes").getAsString());
					datumAsValues.put(DatumTable.COLUMN_GLOSS,
							datumJson.get("gloss").getAsString());
					datumAsValues.put(DatumTable.COLUMN_TRANSLATION, datumJson
							.get("translation").getAsString());
					datumAsValues.put(DatumTable.COLUMN_ORTHOGRAPHY, datumJson
							.get("orthography").getAsString());
					datumAsValues.put(DatumTable.COLUMN_CONTEXT,
							datumJson.get("context").getAsString());
					datumAsValues.put(DatumTable.COLUMN_TAGS,
							datumJson.get("tags").getAsString());
					datumAsValues.put(DatumTable.COLUMN_VALIDATION_STATUS,
							datumJson.get("validationStatus").getAsString());
					datumAsValues.put(DatumTable.COLUMN_ENTERED_BY_USER,
							datumJson.get("enteredByUser").getAsString());
					datumAsValues.put(DatumTable.COLUMN_MODIFIED_BY_USER,
							datumJson.get("modifiedByUser").getAsString());
					datumAsValues.put(DatumTable.COLUMN_COMMENTS, datumJson
							.get("comments").getAsString());

					mediaFilesAsString = datumJson.get("images").getAsString();
					mediaFilesAsString = this
							.addAdditionalDownloads(mediaFilesAsString);
					datumAsValues.put(DatumTable.COLUMN_IMAGE_FILES,
							mediaFilesAsString);

					mediaFilesAsString = datumJson.get("audioVideo")
							.getAsString();
					mediaFilesAsString = this
							.addAdditionalDownloads(mediaFilesAsString);
					datumAsValues.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES,
							mediaFilesAsString);

					uri = getContentResolver().insert(
							DatumContentProvider.CONTENT_URI, datumAsValues);
				} catch (Exception e) {
					Log.d(Config.TAG, "Failed to insert this sample...");
					e.printStackTrace();
				}
			}
		}

		if (this.additionalDownloads.size() > 0) {
			Log.d(Config.TAG,
					"TODO download the image and audio files through a filter that makes them smaller... ");
		}
		// ACRA.getErrorReporter().handleException(
		// new Exception("*** Download Data Completed ***"));
		return;
	}

	public String addAdditionalDownloads(String commadelimitedUrls) {
		String[] urls = commadelimitedUrls.split(",");
		String filenames = "";
		for (String url : urls) {
			url = url.replaceAll("SERVER_URL", Config.DEFAULT_DATA_SERVER_URL);
			this.additionalDownloads.add(url);
			if (!"".equals(filenames)) {
				filenames = filenames + ",";
			}
			filenames = filenames + Uri.parse(url).getLastPathSegment();
		}
		return filenames;
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
