package ca.ilanguage.oprime.offline.activity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.ReplicationCommand;
import org.ektorp.android.util.EktorpAsyncTask;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.content.JavaScriptInterface;

import com.couchbase.cblite.CBLServer;
import com.couchbase.cblite.CBLView;
import com.couchbase.cblite.ektorp.CBLiteHttpClient;
import com.couchbase.cblite.javascript.CBLJavaScriptViewCompiler;
import com.couchbase.cblite.listener.CBLListener;
import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

/**
 * 
 * This extends the HTML5Activity, adding the ability to have an offline couchdb
 * which syncs with an online couchdb
 * 
 * This is the secure one that accesses the ektorp directly, not allowing other
 * processes to get into the db, unfortunatly it doesnt seem to work on android.
 * this.mInitialAppServerUrl =
 * "touchdb:///usersdatabasename/_design/pages/index.html";
 * 
 * Mobile futon interface to Debug this.mInitialAppServerUrl =
 * "http://localhost:8138/mobilefuton/_design/mobilefuton/index.html";
 * 
 * 
 */
public abstract class HTML5ReplicatingActivity extends HTML5Activity {
  /* must be specified by child classes */
  // public static final String PREFERENCE_NAME =
  // "oprimeofflinepreferences";
  protected String PREFERENCE_NAME = "oprimepreferences";
  public static final String PREFERENCE_USERS_DB_NAME = "usersdbname";
  public static final String PREFERENCE_USERNAME = "username";
  public static final String PREFERENCE_PASSWORD = "password";
  public static final String PREFERENCE_COUCH_SERVER_DOMAIN = "couchServerDomain";
  public static final String PREFERENCE_SUCEESSFUL_OFFLINE_DATABASES = "sucessfulOfflineDatabases";

  // constants sample for DB views
  protected String dDocName = "orpime-local";
  protected String dDocId = "_design/" + dDocName;
  protected String byDateViewName = "byDate";

  // couch internals
  protected static CBLServer server;
  protected static HttpClient httpClient;
  protected CBLListener mLocalCouchDBListener;
  protected int mTouchDBListenerPort = 8138;

  protected String mLocalTouchDBFileDir = "";
  protected String mRemoteCouchDBURL = "";
  protected String mLocalCouchAppInitialURL = "";
  protected String mLoginInitialAppServerUrl = "https://oprime.iriscouch.com/login/_design/pages/authentication.html";
  protected String mDatabaseName = "dboprimesample";
  protected String mDefaultRemoteCouchURL = "https://oprime.iriscouch.com";
  protected String mDefaultLoginDatabase = "login";
  protected String mOfflineInitialAppServerUrl = "http://localhost:"
      + mTouchDBListenerPort + "/" + mDefaultLoginDatabase
      + "/_design/pages/authentication.html";
  protected String mUsersPage = "file:///android_asset/release/user.html";

  // ektorp impl
  protected CouchDbInstance dbInstance;
  protected CouchDbConnector couchDbConnector;
  protected ReplicationCommand pushReplicationCommand;
  protected ReplicationCommand pullReplicationCommand;

  // splash screen
  protected boolean splashScreenCanceled = false;
  protected String splashScreenURL = "file:///android_asset/release/splash.html";

  int mBackPressedCount = 0;
  long lastExitApp = System.currentTimeMillis();

  /*
   * static inializer to ensure that touchdb:// URLs are handled properly This
   * doent seem to have any effect.
   */
  {
    CBLURLStreamHandlerFactory.registerSelfIgnoreError();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public void loadLocalCouchApp() {
    mWebView.loadUrl(mLocalCouchAppInitialURL);
  }

  public String getLocalCouchAppInitialURL() {
    return mLocalCouchAppInitialURL;
  }

  public String getCompleteURLtoCouchDBServer() {
    return mRemoteCouchDBURL;
  }

  public String getDatabaseName() {
    return mDatabaseName;
  }

  public void setDatabaseName(String mDatabaseName) {
    this.mDatabaseName = mDatabaseName;
  }

  public abstract void setCouchInfoBasedOnUserDb(String userdb,
      String username, String password, String completeURLtoCouchDBServer);

  /**
   * If details are provided, save them to the preferences. Otherwise find out
   * the previous user, and go to their db, or go to the public db if there was
   * no previous user
   * 
   * @param userdb
   * @param username
   * @param password
   * @param completeURLtoCouchDBServer
   */
  public boolean saveAndValidateCouchInfoOrUsePrevious(String userdb,
      String username, String password, String completeURLtoCouchDBServer,
      String preferencesname) {

    SharedPreferences prefs = this.getSharedPreferences(preferencesname,
        Context.MODE_PRIVATE);

    SharedPreferences.Editor editor = this.getSharedPreferences(
        preferencesname, Context.MODE_PRIVATE).edit();

    if (userdb != null) {
      userdb = userdb.toLowerCase();
      editor.putString(PREFERENCE_USERS_DB_NAME, userdb);
    } else {
      return false;
    }
    this.mDatabaseName = userdb;

    if (username != null) {
      username = username.toLowerCase();
      editor.putString(PREFERENCE_USERNAME, username);
    } else {
      return false;
    }
    if (password != null) {
      editor.putString(PREFERENCE_PASSWORD, password);
    } else {
      return false;
    }

    if (completeURLtoCouchDBServer != null) {
      editor.putString(PREFERENCE_COUCH_SERVER_DOMAIN,
          completeURLtoCouchDBServer);
    } else {
      return false;
    }
    if (username.contains("@")) {
      return false;
    }
    if (password.contains("@")) {
      return false;
    }
    editor.commit();

    String protocol = "http://";
    if (completeURLtoCouchDBServer.contains("https://")) {
      protocol = "https://";
    }
    completeURLtoCouchDBServer = completeURLtoCouchDBServer.replaceAll(
        "https://", "").replaceAll("http://", "");

    mRemoteCouchDBURL = protocol + username + ":" + password + "@"
        + completeURLtoCouchDBServer + "/" + userdb;

    if (D)
      Log.d(TAG, "This is the remote couch db url " + protocol + "---:---"
          + "@" + completeURLtoCouchDBServer + "/" + userdb);

    return true;
  }

  public abstract JavaScriptInterface getJavaScriptInterface();

  public abstract void setJavaScriptInterface(
      JavaScriptInterface javaScriptInterface);

  public int getTouchDBListenerPort() {
    return mTouchDBListenerPort;
  }

  public void setTouchDBListenerPort(int mTouchDBListenerPort) {
    this.mTouchDBListenerPort = mTouchDBListenerPort;
  }

  public abstract Application getApp();

  public abstract void beginReplicating();

  public abstract void stopReplicating();

  // {
  // mLocalTouchDBFileDir = this.getFilesDir().getAbsolutePath()
  // + File.separator;
  // startTouchDB();
  // startEktorp();
  // turnOnDatabase();
  // }

  protected void onPause() {
    if (D)
      Log.v(TAG, "HTML5 Replicating onPause");
    super.onPause();
  }

  protected void onDestroy() {
    if (D)
      Log.v(TAG, "HTML5 Replicating onDestroy");
    super.onDestroy();
  }

  public void exitApp() {
    /* prevent too many pop-ups */
    if ((System.currentTimeMillis() - lastExitApp) < 1000) {
      return;
    }
    lastExitApp = System.currentTimeMillis();
    if (mBackPressedCount >= 1) {
      finish();
      return;
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          boolean turningOffDBs = false;
          mBackPressedCount++;

          /*
           * Turn off the databases, the webview will call exit again in a few
           * seconds
           */
          turningOffDBs = stopEktorpAndTDListener();
          if (!turningOffDBs) {
            Log.d(TAG,
                "There was apparently nothing to turn off before exiting.");
          }
          mWebView
              .loadUrl("javascript:window.setTimeout(function(){exitApp()},1000);");

          break;

        case DialogInterface.BUTTON_NEGATIVE:
          Log.d(TAG, "The user pushed back/exit by mistake.");
          mBackPressedCount = 0;
          lastExitApp = 0;
          break;
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Would you like to exit the app?")
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener).show();
    return;

  }

  @Override
  public void onBackPressed() {
    exitApp();
  }

  protected void startTouchDB() {
    (new File(mLocalTouchDBFileDir)).mkdirs();
    try {
      server = new CBLServer(mLocalTouchDBFileDir);
    } catch (IOException e) {
      Log.e(TAG, "Error starting TDServer", e);
    }

    setupTouchDBViews();
  }

  /**
   * sets up a javascript compiler for the views, which means normal views can
   * be used. If they are too slow could consider using ektorp?
   */
  protected void setupTouchDBViews() {
    if (D)
      Log.d(TAG, "Setting TDView with a Javascript map reduce compiler,"
          + " this allows compiling of any views downloaded from couchapp.");
    CBLView.setCompiler(new CBLJavaScriptViewCompiler());
  }

  /**
   * This opens security holes as other apps and computers on the local network
   * can access the touchdb, with no credentials, modify things, and those
   * modifications will be pushed to the server with the users credentials
   */
  @Deprecated
  public void turnOnDatabaseListener(boolean loadUrl) {
    (new File(mLocalTouchDBFileDir)).mkdirs();

    CBLServer server;
    try {
        server = new CBLServer(mLocalTouchDBFileDir);

      /*
       * TODO can use basic auth for replication without creds in the url
       * 
       * Explanation:
       * https://github.com/couchbaselabs/TouchDB-Android/wiki/Replication
       * -Without -Credentials-in-the-URL
       * 
       * Example:
       * https://github.com/couchbaselabs/TouchDB-Android/blob/master/TouchDB-
       * Android
       * -TestApp/src/com/couchbase/touchdb/testapp/ektorp/tests/Replicator.java
       */

      mLocalCouchDBListener = new CBLListener(server, mTouchDBListenerPort);
      mLocalCouchDBListener.start();
      if (D) {
        Log.i(TAG, "Started the local offline couchdb database listener.");
      }

    } catch (IOException e) {
      Log.e(TAG, "Unable to create a TDServer", e);
    }
  }

  protected void startEktorp() {
    Log.v(TAG, "starting ektorp");

    if (httpClient != null) {
      httpClient.shutdown();
    }

    httpClient = new CBLiteHttpClient(server);
    dbInstance = new StdCouchDbInstance(httpClient);

    HTML5SyncEktorpAsyncTask startupTask = new HTML5SyncEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        couchDbConnector = dbInstance.createConnector(mDatabaseName, true);
      }

      @Override
      protected void onSuccess() {
        Log.v(TAG, "Ektorp has started");

        startReplications();
      }
    };
    startupTask.execute();
  }

  public void startReplications() {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    if(pushReplicationCommand == null){
      pushReplicationCommand = new ReplicationCommand.Builder()
      .source(mDatabaseName)
      .target(prefs.getString("sync_url", mRemoteCouchDBURL))
      .continuous(true).build();
      HTML5SyncEktorpAsyncTask pushReplication = new HTML5SyncEktorpAsyncTask() {
        
        @Override
        protected void doInBackground() {
          dbInstance.replicate(pushReplicationCommand);
        }
      };
      pushReplication.execute();
    }


    if(pullReplicationCommand == null){
      pullReplicationCommand = new ReplicationCommand.Builder()
      .source(prefs.getString("sync_url", mRemoteCouchDBURL))
      .target(mDatabaseName).continuous(true).build();
      HTML5SyncEktorpAsyncTask pullReplication = new HTML5SyncEktorpAsyncTask() {
        
        @Override
        protected void doInBackground() {
          dbInstance.replicate(pullReplicationCommand);
        }
      };
      pullReplication.execute();
    }

  }

  public boolean stopEktorpAndTDListener() {
    boolean turningOffDBs = false;
    if (httpClient != null) {
      Log.d(TAG, "Turning off TouchDBHttpClient for Ektorp and views");
      httpClient.shutdown();
      turningOffDBs = true;
    }

    if (mLocalCouchDBListener != null) {
      Log.d(TAG, "Turning off TDListener");
      mLocalCouchDBListener.stop();
      turningOffDBs = true;
    }
    
    if (server != null) {
      // https://groups.google.com/forum/#!msg/mobile-couchbase/IlDYfOHFH-c/mUBVxGxOW8kJ
      /*
       * TODO see
       * https://groups.google.com/forum/#!msg/mobile-couchbase/IlDYfOHFH
       * -c/mUBVxGxOW8kJ for a "new branch" which fixes the main thread
       * execution of touchdb...
       */
      // Log.d(TAG, "Turning off TDServer");
      try{
        server.close();
      }catch(Exception e){
        Log.e(TAG,"There was an error when closing the TDSERVER");
        e.printStackTrace();
      }

      /*
       * 12-21 14:41:18.976: E/AndroidRuntime(32196): FATAL EXCEPTION: main
       * dalvik.system.NativeStart.main(Native Method)
       */
      turningOffDBs = true;
    }


    return turningOffDBs;
  }

  public abstract class HTML5SyncEktorpAsyncTask extends EktorpAsyncTask {

    @Override
    protected void onDbAccessException(DbAccessException dbAccessException) {
      Log.e(TAG, "DbAccessException in background", dbAccessException);
    }

  }

  /**
   * Removes the Dialog that displays the splash screen
   */
  public void removeSplashScreen() {
    splashScreenCanceled = true;
    // TODO do something else
  }

  /**
   * Shows the splash screen over the full Activity
   */
  public void showSplashScreen() {
    splashScreenCanceled = false;
    mWebView.loadUrl(splashScreenURL);
  }

  public boolean isSplashScreenCanceled() {
    return splashScreenCanceled;
  }

  public void setSplashScreenCanceled(boolean splashScreenCanceled) {
    this.splashScreenCanceled = splashScreenCanceled;
    this.removeSplashScreen();
  }

  public boolean isRequestedDBAKnownOfflineDBName(String dbname) {

    if (dbname == null) {
      return false;
    }
    if (dbname.length() < 2) {
      return false;
    }
    SharedPreferences prefs = this.getSharedPreferences(PREFERENCE_NAME,
        Context.MODE_PRIVATE);
    String sucessfullOfflindbs = prefs.getString(
        PREFERENCE_SUCEESSFUL_OFFLINE_DATABASES, "");
    if (sucessfullOfflindbs == null) {
      return false;
    } else {
      String[] dbs = sucessfullOfflindbs.split(",");
      for (int i = 0; i < dbs.length; i++) {
        if (dbs[i].equalsIgnoreCase(dbname)) {
          return true;
        }
      }
      return false;
    }
  }

  public void addSuccessfulOfflineDatabase(String dbname) {

    SharedPreferences prefs = this.getSharedPreferences(PREFERENCE_NAME,
        Context.MODE_PRIVATE);
    String sucessfullOfflindbs = prefs.getString(
        PREFERENCE_SUCEESSFUL_OFFLINE_DATABASES, "");
    if (sucessfullOfflindbs == null) {
      sucessfullOfflindbs = dbname;
    } else {
      if (!sucessfullOfflindbs.contains(dbname)) {
        SharedPreferences.Editor editor = prefs.edit();
        sucessfullOfflindbs = sucessfullOfflindbs + "," + dbname;
        editor.putString(PREFERENCE_SUCEESSFUL_OFFLINE_DATABASES,
            sucessfullOfflindbs);
        editor.commit();
      }
    }

  }

  public String getOfflineDBs() {

    File folder = new File(mLocalTouchDBFileDir);
    File[] listOfFiles = folder.listFiles();
    String dbs = "";
    for (File file : listOfFiles) {
      String filename = file.getName();
      if (filename.endsWith("touchdb")) {
        dbs = dbs + "," + filename.replace(".touchdb", "");
      }
    }

    return dbs;// dbInstance.getAllDatabases().toString();
  }

}
