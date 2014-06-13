package com.github.opensourcefieldlinguistics.fielddb.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.acra.ACRA;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import ca.ilanguage.oprime.datacollection.NotifyingIntentService;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.BuildConfig;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;
import com.google.gson.JsonObject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class UploadAudioVideoService extends NotifyingIntentService {

	protected String username = "default";
	public UploadAudioVideoService(String name) {
		super(name);
	}

	public UploadAudioVideoService() {
		super("UploadAudioVideoService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		/* only upload files when connected to wifi */
		ConnectivityManager connManager = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!wifi.isConnected()) {
			return;
		}

		/* only upload files with content */
		if (intent.getData() == null) {
			return;
		}
		Uri uri = intent.getData();
		if (uri.getPath() == null) {
			return;
		}
		File f = new File(uri.getPath());
		if (!f.exists()) {
			return;
		}
		if (f.length() < 5000) {
			Log.d(Config.TAG, "Not uploading, " + uri.getLastPathSegment()
					+ " was too small " + f.length());
			return;
		}

		this.D = Config.D;
		this.statusMessage = "Uploading audio video";
		this.tryAgain = intent;
		this.keystoreResourceId = R.raw.sslkeystore;
		if (Config.D) {
			Log.d(Config.TAG, "Inside UploadAudioVideoService intent");
		}
		if (!BuildConfig.DEBUG)
			ACRA.getErrorReporter().putCustomData("action",
					"uploadAudioVideo:::");
		if (!BuildConfig.DEBUG)
			ACRA.getErrorReporter().putCustomData("urlString",
					Config.DEFAULT_UPLOAD_AUDIO_VIDEO_URL);

		super.onHandleIntent(intent);

		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			if (!BuildConfig.DEBUG)
				ACRA.getErrorReporter().handleException(
						new Exception(this.userFriendlyErrorMessage));
			return;
		}

		if (intent.hasExtra(Config.EXTRA_PARTICIPANT_ID)) {
			username = intent.getExtras()
					.getString(Config.EXTRA_PARTICIPANT_ID);
		}

		String JSONResponse = this.upload(intent.getData());
		if (JSONResponse == null && "".equals(this.userFriendlyErrorMessage)) {
			this.userFriendlyErrorMessage = "Server response was missing. Please report this.";
		}
		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			if (!BuildConfig.DEBUG)
				ACRA.getErrorReporter().handleException(
						new Exception(this.userFriendlyErrorMessage));
			return;
		}

		processUploadResponse(intent.getData(), JSONResponse);
		if (!"".equals(this.userFriendlyErrorMessage)) {
			this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti,
					this.notificationId, true);
			if (!BuildConfig.DEBUG)
				ACRA.getErrorReporter().handleException(
						new Exception(this.userFriendlyErrorMessage));
			return;
		}

		/* all is well, get their cookie set */
		// this.getCouchCookie(username, generatedPassword,
		// Config.DEFAULT_DATA_LOGIN);

		/* Success: remove the notification */
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancel(this.notificationId);
		if (!BuildConfig.DEBUG)
			ACRA.getErrorReporter().handleException(
					new Exception("*** Uploadinged user ssucessfully ***"));
	}

	public String upload(Uri uri) {
		String filePath = uri.getPath();
		this.statusMessage = "Uploading audio " + uri.getLastPathSegment();
		if (!BuildConfig.DEBUG)
			ACRA.getErrorReporter().putCustomData("registerUser",
					uri.getLastPathSegment());
		String urlStringAuthenticationSession = Config.DEFAULT_UPLOAD_AUDIO_VIDEO_URL;

		// http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(urlStringAuthenticationSession);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("file", new FileBody(new File(filePath)));
		builder.addTextBody("userName", username);
		builder.addTextBody("token", Config.DEFAULT_UPLOAD_TOKEN);
		builder.addTextBody("dbname", Config.DEFAULT_CORPUS);
		builder.addTextBody("returnTextGrid", "true");

		post.setEntity(builder.build());
		HttpResponse response;
		String JSONResponse = "";
		try {
			response = client.execute(post);
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status >= 500) {
				this.userFriendlyErrorMessage = "Server error, please report this error.";
			} else if (status >= 400) {
				this.userFriendlyErrorMessage = "Something was wrong with this file. It could not be processed.";
			} else {
				JSONResponse = "";
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String newLine;
				do {
					newLine = reader.readLine();
					if (newLine != null) {
						JSONResponse += newLine;
					}
				} while (newLine != null);

			}
			entity.consumeContent();
			client.getConnectionManager().shutdown();
		} catch (ClientProtocolException e1) {
			this.userFriendlyErrorMessage = "Problem using POST, please report this error.";
			e1.printStackTrace();
		} catch (IOException e1) {
			this.userFriendlyErrorMessage = "Problem opening upload connection to server, please report this error.";
			e1.printStackTrace();
		}

		if ("".equals(JSONResponse)) {
			this.userFriendlyErrorMessage = "Unknown error reading sample data from server";
			return null;
		}

		if (!"".equals(this.userFriendlyErrorMessage)) {
			return null;
		}
		return JSONResponse;
	}
	public int processUploadResponse(Uri uri, String jsonResponse) {
		JsonObject json = (JsonObject) NotifyingIntentService.jsonParser
				.parse(jsonResponse);
		if (json.has("userFriendlyErrors")) {
			this.userFriendlyErrorMessage = json.get("userFriendlyErrors")
					.getAsString();
			return 0;
		}
		if (!json.has("user")) {
			this.userFriendlyErrorMessage = "The server response is very strange, please report this.";
			return 0;
		}

		return 0;
	}
}
