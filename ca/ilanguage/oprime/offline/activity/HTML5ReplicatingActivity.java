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

import android.app.Application;
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

  // constants sample for DB views
  protected String dDocName = "orpime-local";
  protected String dDocId = "_design/" + dDocName;
  protected String byDateViewName = "byDate";

  protected String mLocalTouchDBFileDir = "";
  protected String mRemoteCouchDBURL = "";
  protected String mCompleteURLtoCouchDBServer = "";
  protected String mLocalCouchAppInitialURL = "";
  protected String mDatabaseName = "dboprimesample";

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

  public String getCompleteURLtoCouchDBServer () {
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

  @Override
  public void onBackPressed() {
    mBackPressedCount++;
    boolean turningOffDBs = false;

    if (mBackPressedCount < 2) {
      Toast.makeText(this, "Turning off databases, press back again to exit.",
          Toast.LENGTH_LONG).show();

      turningOffDBs = stopEktorpAndTDListener();
    }

    if (!turningOffDBs) {
      super.onBackPressed();
    }

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
        mWebView.loadUrl(mLocalCouchAppInitialURL);
      }

    } catch (IOException e) {
      Log.e(TAG, "Unable to create TDServer", e);
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

}
