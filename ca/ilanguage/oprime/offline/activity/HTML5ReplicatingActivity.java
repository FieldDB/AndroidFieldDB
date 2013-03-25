package ca.ilanguage.oprime.offline.activity;

import java.io.File;
import java.io.IOException;

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
import android.widget.Toast;

import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.content.JavaScriptInterface;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.listener.TDListener;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

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

  protected String mLocalTouchDBFileDir = "";
  protected String mRemoteCouchDBURL = "";
  protected String mCompleteURLtoCouchDBServer = "";
  protected String mLocalCouchAppInitialURL = "";
  protected String mLoginInitialAppServerUrl = "https://oprime.iriscouch.com/login/_design/pages/authentication.html";
  protected String mDatabaseName = "dboprimesample";
  protected String mDefaultRemoteCouchURL = "https://oprime.iriscouch.com";
  protected String mDefaultLoginDatabase = "login";
  protected final String mOfflineInitialAppServerUrl = "http://localhost:8148/"
      + mDefaultLoginDatabase + "/_design/pages/authentication.html";

  // couch internals
  protected static TDServer server;
  protected static HttpClient httpClient;
  protected TDListener mLocalCouchDBListener;
  protected int mTouchDBListenerPort = 8138;

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
    TDURLStreamHandlerFactory.registerSelfIgnoreError();
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
    return mCompleteURLtoCouchDBServer;
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
      userdb = prefs.getString(PREFERENCE_USERS_DB_NAME, mDefaultLoginDatabase);
      if (userdb == null || mDefaultLoginDatabase.equals(userdb)) {
        return false;
      }
    }
    this.mDatabaseName = userdb;

    /*
     * IF its a real user, then take them to their offline database TODO test
     * this, if you don't use the app, and exit immediately does it still take
     * you to the online login?
     */
    if (!mDefaultLoginDatabase.equals(userdb)) {
      this.mInitialAppServerUrl = this.mOfflineInitialAppServerUrl.replace(
          mDefaultLoginDatabase, userdb);
    }

    if (username != null) {
      username = username.toLowerCase();
      editor.putString(PREFERENCE_USERNAME, username);
    } else {
      username = prefs.getString(PREFERENCE_USERNAME, "public");
      if (username == null || "public".equals(username)) {
        return false;
      }
    }
    if (password != null) {
      editor.putString(PREFERENCE_PASSWORD, password);
    } else {
      password = prefs.getString(PREFERENCE_PASSWORD, "none");
      if (password == null || "none".equals(password)) {
        return false;
      }
    }

    if (completeURLtoCouchDBServer != null) {
      editor.putString(PREFERENCE_COUCH_SERVER_DOMAIN,
          completeURLtoCouchDBServer);
    } else {
      completeURLtoCouchDBServer = prefs.getString(
          PREFERENCE_COUCH_SERVER_DOMAIN, mDefaultRemoteCouchURL);
      if (completeURLtoCouchDBServer == null) {
        return false;
      }
      mCompleteURLtoCouchDBServer = completeURLtoCouchDBServer;
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
      server = new TDServer(mLocalTouchDBFileDir);
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
    TDView.setCompiler(new TDJavaScriptViewCompiler());
  }

  /**
   * This opens security holes as other apps and computers on the local network
   * can access the touchdb, with no credentials, modify things, and those
   * modifications will be pushed to the server with the users credentials
   */
  @Deprecated
  public void turnOnDatabaseListener(boolean loadUrl) {
    (new File(mLocalTouchDBFileDir)).mkdirs();

    TDServer server;
    try {
      server = new TDServer(mLocalTouchDBFileDir);
      mLocalCouchDBListener = new TDListener(server, mTouchDBListenerPort);
      mLocalCouchDBListener.start();
      if (D) {
        Log.i(TAG, "Started the local offline couchdb database listener.");
      }

      if (loadUrl) {
        /* If the db is known to exist, take them there */
        if (!isRequestedDBAKnownOfflineDBName(mDatabaseName)) {
          Toast
              .makeText(
                  this,
                  "Your offline app doesn't seem to be ready. Taking you to the online login.",
                  Toast.LENGTH_LONG).show();
          this.mInitialAppServerUrl = mLoginInitialAppServerUrl;
          mWebView.loadUrl(mLocalCouchAppInitialURL);
        } else {
          if (D)
            Log.i(TAG, "loading " + mLocalCouchAppInitialURL);
          mWebView.loadUrl(mLocalCouchAppInitialURL);
        }

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

    httpClient = new TouchDBHttpClient(server);
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

  public boolean stopEktorpAndTDListener() {
    boolean turningOffDBs = false;
    if (httpClient != null) {
      Log.d(TAG, "Turning off TouchDBHttpClient for Ektorp and views");
      httpClient.shutdown();
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
      // server.close();

      /*
       * 12-21 14:41:18.976: E/AndroidRuntime(32196): FATAL EXCEPTION: main
       * 12-21 14:41:18.976: E/AndroidRuntime(32196):
       * android.os.NetworkOnMainThreadException 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * android.os.StrictMode$AndroidBlockGuardPolicy
       * .onNetwork(StrictMode.java:1117) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * org.apache.harmony.xnet.provider.jsse.OpenSSLSocketImpl
       * .close(OpenSSLSocketImpl.java:908) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * org.apache.http.impl.SocketHttpClientConnection
       * .shutdown(SocketHttpClientConnection.java:183) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * org.apache.http.impl.conn.DefaultClientConnection
       * .shutdown(DefaultClientConnection.java:150) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * org.apache.http.impl.conn.AbstractPooledConnAdapter
       * .shutdown(AbstractPooledConnAdapter.java:169) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * org.apache.http.impl.conn.AbstractClientConnAdapter
       * .abortConnection(AbstractClientConnAdapter.java:378) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * org.apache.http.client.methods
       * .HttpRequestBase.abort(HttpRequestBase.java:159) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * com.couchbase.touchdb.replicator.changetracker
       * .TDChangeTracker.stop(TDChangeTracker.java:294) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * com.couchbase.touchdb.replicator.TDPuller.stop(TDPuller.java:83) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * com.couchbase.touchdb.replicator
       * .TDReplicator.databaseClosing(TDReplicator.java:103) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * com.couchbase.touchdb.TDDatabase.close(TDDatabase.java:333) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * com.couchbase.touchdb.TDServer.close(TDServer.java:142) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * ca.ilanguage.oprime.offline.activity
       * .HTML5ReplicatingActivity.onBackPressed
       * (HTML5ReplicatingActivity.java:121) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * android.app.Activity.onKeyUp(Activity.java:2145) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * android.view.KeyEvent.dispatch(KeyEvent.java:2633) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * android.app.Activity.dispatchKeyEvent(Activity.java:2375) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * com.android.internal.policy.impl
       * .PhoneWindow$DecorView.dispatchKeyEvent(PhoneWindow.java:1847) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * android.view.ViewRootImpl.deliverKeyEventPostIme
       * (ViewRootImpl.java:3701) 12-21 14:41:18.976: E/AndroidRuntime(32196):
       * at
       * android.view.ViewRootImpl.handleImeFinishedEvent(ViewRootImpl.java:3651
       * ) 12-21 14:41:18.976: E/AndroidRuntime(32196): at
       * android.view.ViewRootImpl$ViewRootHandler
       * .handleMessage(ViewRootImpl.java:2818) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * android.os.Handler.dispatchMessage(Handler.java:99) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at android.os.Looper.loop(Looper.java:137)
       * 12-21 14:41:18.976: E/AndroidRuntime(32196): at
       * android.app.ActivityThread.main(ActivityThread.java:5039) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * java.lang.reflect.Method.invokeNative(Native Method) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * java.lang.reflect.Method.invoke(Method.java:511) 12-21 14:41:18.976:
       * E/AndroidRuntime(32196): at
       * com.android.internal.os.ZygoteInit$MethodAndArgsCaller
       * .run(ZygoteInit.java:793) 12-21 14:41:18.976: E/AndroidRuntime(32196):
       * at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:560) 12-21
       * 14:41:18.976: E/AndroidRuntime(32196): at
       * dalvik.system.NativeStart.main(Native Method)
       */
      turningOffDBs = true;
    }

    if (mLocalCouchDBListener != null) {
      Log.d(TAG, "Turning off TDListener");
      mLocalCouchDBListener.stop();
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

}
