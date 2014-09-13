package com.github.opensourcefieldlinguistics.fielddb.android.activity;

import java.io.File;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import ca.ilanguage.oprime.content.JavaScriptInterface;
import ca.ilanguage.oprime.offline.activity.HTML5ReplicatingActivity;

import com.github.opensourcefieldlinguistics.fielddb.android.content.FieldDBApp;
import com.github.opensourcefieldlinguistics.fielddb.android.content.FieldDBJavaScriptInterface;

public class FieldDBActivity extends HTML5ReplicatingActivity {
  public static final String PREFERENCE_NAME = "fielddbapppreferences";
  private FieldDBJavaScriptInterface mJavaScriptInterface;
  protected String mLoginInitialAppServerUrl = "https://corpusdev.lingsync.org/public-firstcorpus/_design/pages/corpus.html";
  protected String mDefaultRemoteCouchURL = "https://corpusdev.lingsync.org";
  protected String mDefaultLoginDatabase = "public-firstcorpus";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    // mTouchDBListenerPort = 8158;
    mLocalTouchDBFileDir = getApp().getLocalCouchDir();
    turnOnDatabaseListener(false);
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if ("1".equals(mLocalCouchDBListener.getStatus())) {
      Log.e(TAG, "Server status is off!");
    } else {
      Log.e(TAG, "Server status is on!");
    }
  }

  @Override
  public void setCouchInfoBasedOnUserDb(String userdb, String username,
      String password, String completeURLtoCouchDBServer) {

    boolean credentialsSet = saveAndValidateCouchInfoOrUsePrevious(userdb, username,
        password, completeURLtoCouchDBServer, FieldDBApp.PREFERENCE_PREFERENCE_NAME);
    Log.d(TAG, "Credentials were set: " + credentialsSet);
    if (!credentialsSet) {
      this.mInitialAppServerUrl = this.mLoginInitialAppServerUrl;
    }
  }

  /**
   * Is typically turned on by the Javascript
   */
  @Override
  public void beginReplicating() {
    if (mRemoteCouchDBURL == "") {
      return;
    }
    if ("public-firstcorpus".equals(mDatabaseName)) {
      Log.d(TAG,
          "Not replicating, the user is trying to use the login database.");
      return;
    }
    mLocalTouchDBFileDir = getApp().getLocalCouchDir();
    (new File(mLocalTouchDBFileDir)).mkdirs();
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

    this.mOutputDir = getApp().getOutputDir();
    mLocalTouchDBFileDir = getApp().getLocalCouchDir();
    this.setJavaScriptInterface(new FieldDBJavaScriptInterface(D, TAG,
        this.mOutputDir, getApplicationContext(), this, "release/"));
    /*
     * Set the previous user's credentails, if the db is not known to exist, and
     * this is when the app first opens, take them to the online login instead
     */
    setCouchInfoBasedOnUserDb(null, null, null, null);

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

  public FieldDBApp getApp() {
    return (FieldDBApp) this.getApplication();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    /* dont show the menu */
    return true;
  }

  
}
