package com.github.opensourcefieldlinguistics.fielddb.android.activity;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import ca.ilanguage.oprime.content.JavaScriptInterface;
import ca.ilanguage.oprime.offline.activity.HTML5ReplicatingActivity;

import com.github.opensourcefieldlinguistics.fielddb.android.content.FieldDBApp;
import com.github.opensourcefieldlinguistics.fielddb.android.content.FieldDBJavaScriptInterface;

public class FieldDBActivity extends HTML5ReplicatingActivity {
  private FieldDBJavaScriptInterface mJavaScriptInterface;
  private FieldDBApp app;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.setApp(getApplication());

    /* not using the android splash screen */
    splashScreenCanceled = true;
    setCouchInfoBasedOnUserDb(null, null, null, null);
//    this.beginReplicating();
    turnOnDatabaseListener(false);
    super.onCreate(savedInstanceState);

  }

  public void setCouchInfoBasedOnUserDb(String userdb, String username,
      String password, String serverdomain) {
    /*
     * If details are provided, save them to the preferences. Otherwise find out
     * the previous user, and go to their db, or go to the public db if there
     * was no previous user
     */
    SharedPreferences prefs = this.getSharedPreferences(
        FieldDBApp.PREFERENCE_PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = this.getSharedPreferences(
        FieldDBApp.PREFERENCE_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();

    if (userdb == null) {
      userdb = prefs.getString(FieldDBApp.PREFERENCE_USERS_DB_NAME, "public-firstcorpus");
    } else {
      editor.putString(FieldDBApp.PREFERENCE_USERS_DB_NAME, userdb);
    }

    if (username == null) {
      username = prefs.getString(FieldDBApp.PREFERENCE_USERNAME, "public");
    } else {
      editor.putString(FieldDBApp.PREFERENCE_USERNAME, username);
    }

    if (password == null) {
      password = prefs.getString(FieldDBApp.PREFERENCE_PASSWORD, "none");
    } else {
      editor.putString(FieldDBApp.PREFERENCE_PASSWORD, password);
    }

    if (serverdomain == null) {
      serverdomain = prefs.getString(FieldDBApp.PREFERENCE_COUCH_SERVER_DOMAIN,
        "ifielddevs.iriscouch.com");
//        "corpus.lingsync.org"); //TODO fix peer certificates on all startsll certs

    } else {
      editor.putString(FieldDBApp.PREFERENCE_COUCH_SERVER_DOMAIN, serverdomain);
    }
    editor.commit();

    this.mDatabaseName = userdb;
    this.mRemoteCouchDBURL = "https://" + username + ":" + password + "@"
        + serverdomain + "/" + userdb;
    if(D) Log.d(TAG, "This is the remote couch db url "+mRemoteCouchDBURL);
    this.mLocalCouchAppInitialURL = "http://localhost:8128/" + userdb
        + "/_design/pages/index.html";
    /*
     * If the user is unknown, take them to the authentication page in the
     * assets
     */
    // if("public".equals(username)
    this.mInitialAppServerUrl = "file:///android_asset/release/authentication.html";

  }

  /**
   * Is typically turned on by the Javascript
   */
  @Override
  public void beginReplicating() {
    if (mRemoteCouchDBURL == "") {
      return;
    }
    mLocalTouchDBFileDir = ((FieldDBApp) getApplication()).getLocalCouchDir();
    new File(mLocalTouchDBFileDir).mkdirs();
    startTouchDB();
    startEktorp();
  }

  /**
   * Is typically turned off by the Javascript
   */
  @Override
  public void stopReplicating() {
    stopEktorpAndTDListener();
  }

  protected void setUpVariables() {
    this.TAG = getApp().getTag();
    this.D = getApp().isD();
    Log.d(TAG, "Setting TAG " + this.TAG + " and Debug " + this.D);

    this.mOutputDir = app.getOutputDir();
    this.setJavaScriptInterface( new FieldDBJavaScriptInterface(D, TAG,
        this.mOutputDir, getApplicationContext(), this, "release/"));
  }

  @Override
  public JavaScriptInterface getJavaScriptInterface() {
    return this.mJavaScriptInterface;
  }

  @Override
  public void setJavaScriptInterface(JavaScriptInterface javaScriptInterface) {
    this.mJavaScriptInterface = (FieldDBJavaScriptInterface) javaScriptInterface;
  }

  public String getDatabaseName() {
    return mDatabaseName;
  }

  @Override
  public FieldDBApp getApp() {
    return app;
  }

  @Override
  public void setApp(Application app) {
    this.app = (FieldDBApp) app;
  }

}
