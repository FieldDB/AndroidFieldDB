package com.github.fielddb.service;

import java.io.File;
import java.io.IOException;

import com.github.fielddb.BugReporter;
import com.github.fielddb.Config;
import com.github.fielddb.datacollection.NotifyingIntentService;
import com.github.fielddb.datacollection.MultipartPostRequest;
import com.github.fielddb.R;
import com.google.gson.JsonObject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

/**
 * FIXME this needs to be updated to not use new http connections
 */
public class UploadAudioVideoService extends NotifyingIntentService {
  protected String mDeviceDetails = "{}";
  protected String mUsername = "default";

  public UploadAudioVideoService(String name) {
    super(name);
  }

  public UploadAudioVideoService() {
    super("UploadAudioVideoService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (Config.D) {
      Log.d(Config.TAG, " we are in debug mode, not uploading audio/video file");
      return;
    }

    /* only upload files when connected to wifi */
    ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    if (!wifi.isConnected()) {
      Log.d(Config.TAG, " we are not using wifi, not uploading audio/video file");
      // return;
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
      Log.d(Config.TAG, "Not uploading, " + uri.getLastPathSegment() + " was too small " + f.length());
      return;
    }

    this.statusMessage = "Uploading audio video";
    this.tryAgain = intent;
    this.keystoreResourceId = R.raw.sslkeystore;
    if (Config.D) {
      Log.d(Config.TAG, "Inside UploadAudioVideoService intent");
    }
    BugReporter.putCustomData("action", "uploadAudioVideo:::");
    BugReporter.putCustomData("urlString", Config.DEFAULT_UPLOAD_AUDIO_VIDEO_URL);

    super.onHandleIntent(intent);

    if (!"".equals(this.userFriendlyErrorMessage)) {
      this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti, this.notificationId, true);
      BugReporter.sendBugReport(this.userFriendlyErrorMessage);
      return;
    }

    if (intent.hasExtra(Config.EXTRA_PARTICIPANT_ID)) {
      mUsername = intent.getExtras().getString(Config.EXTRA_PARTICIPANT_ID);
    }
    if (intent.hasExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION)) {
      mDeviceDetails = intent.getExtras().getString(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION);
    }

    String JSONResponse = this.upload(intent.getData());
    if (JSONResponse == null && "".equals(this.userFriendlyErrorMessage)) {
      this.userFriendlyErrorMessage = "Server response was missing. Please report this.";
    }
    if (!"".equals(this.userFriendlyErrorMessage)) {
      this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti, this.notificationId, true);
      BugReporter.sendBugReport(this.userFriendlyErrorMessage);
      return;
    }

    processUploadResponse(intent.getData(), JSONResponse);
    if (!"".equals(this.userFriendlyErrorMessage)) {
      this.notifyUser(" " + this.userFriendlyErrorMessage, this.noti, this.notificationId, true);
      BugReporter.sendBugReport(this.userFriendlyErrorMessage);
      return;
    }

    /* all is well, get their cookie set */
    // this.getCouchCookie(username, generatedPassword,
    // Config.DEFAULT_DATA_LOGIN);

    /* Success: remove the notification */
    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(this.notificationId);
    com.github.fielddb.model.Activity.sendActivity("{\"uploaded\" : \"audio\"}", "{}",
        "*** Uploaded audio sucessfully ***");
  }

  public String upload(Uri uri) {
    String filePath = uri.getPath();
    this.statusMessage = "Uploading audio " + uri.getLastPathSegment();
    BugReporter.putCustomData("uploadAudio", uri.getLastPathSegment());
    String JSONResponse = "";

    try {
      MultipartPostRequest request = new MultipartPostRequest(Config.DEFAULT_UPLOAD_AUDIO_VIDEO_URL);
      request.addFormField("username", mUsername);
      request.addFormField("token", Config.DEFAULT_UPLOAD_TOKEN);
      request.addFormField("dbname", Config.DEFAULT_CORPUS);
      request.addFormField("returnTextGrid", "true");
      request.addFilePart("videoFile", new File(filePath));
      JSONResponse = request.execute();
    } catch (IOException e) {
      this.userFriendlyErrorMessage = "Problem opening upload connection to server, please report this error. " + e.getMessage();
      Log.d(Config.TAG, "Failed to execute request.");
      e.printStackTrace();
      return null;
    }

    if (!"".equals(this.userFriendlyErrorMessage)) {
      return JSONResponse;
    }
    return JSONResponse;
  }

  public int processUploadResponse(Uri uri, String jsonResponse) {
    if (jsonResponse != null && Config.D) {
      Log.d(Config.TAG, jsonResponse);
    }
    JsonObject json = (JsonObject) NotifyingIntentService.jsonParser.parse(jsonResponse);
    if (json.has("userFriendlyErrors")) {
      this.userFriendlyErrorMessage = json.get("userFriendlyErrors").getAsString();
      return 0;
    }
    if (!json.has("files")) {
      this.userFriendlyErrorMessage = "The server response is very strange, please report this.";
      return 0;
    } else {
      com.github.fielddb.model.Activity.sendActivity("uploadAudio", jsonResponse);
    }

    return 0;
  }
}
