package ca.ilanguage.oprime.offline.service;

import java.io.File;

import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.UpdateConflictException;

import ca.ilanguage.oprime.content.OPrimeApp;
import ca.ilanguage.oprime.offline.provider.OPrimeEktorpAsyncTask;
import ca.ilanguage.oprime.offline.provider.TouchDBItemUtils;

import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class AuthenticateUserOffline extends IntentService {
  protected static final String TAG = "Pivot88";

  // constants
  protected String mDatabaseName = "_users";
  protected String mLocalTouchDBFileDir = "";
  protected String mRemoteCouchDBURL = "https://semisecureadmin:none@cesine.iriscouch.com/_users";

  // couch internals
  protected static TDServer server;
  protected static HttpClient httpClient;

  // ektorp impl
  protected CouchDbInstance dbInstance;
  protected CouchDbConnector couchDbConnector;

  int mBackPressedCount = 0;

  // static inializer to ensure that touchdb:// URLs are handled properly
  {
    TDURLStreamHandlerFactory.registerSelfIgnoreError();
  }

  public AuthenticateUserOffline(String name) {
    super(name);
  }

  public AuthenticateUserOffline() {
    super("AuthenticateUserOffline");
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    mLocalTouchDBFileDir = ((OPrimeApp) getApplication())
        .getLocalCouchDir();
    new File(mLocalTouchDBFileDir).mkdirs();

  }

  protected void startEktorp() {
    Log.v(TAG, "starting ektorp");

    if (httpClient != null) {
      httpClient.shutdown();
    }

    httpClient = new TouchDBHttpClient(server);
    dbInstance = new StdCouchDbInstance(httpClient);

    OPrimeEktorpAsyncTask startupTask = new OPrimeEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        couchDbConnector = dbInstance.createConnector(mDatabaseName, true);
      }

      @Override
      protected void onSuccess() {
        Log.v(TAG, "Ektorp has started");
        /*
         * Authenticate user
         */

      }
    };
    startupTask.execute();
  }

  public void createDBItem(String name) {
    final JsonNode item = TouchDBItemUtils.createWithText(name);
    OPrimeEktorpAsyncTask createItemTask = new OPrimeEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        couchDbConnector.create(item);
      }

      @Override
      protected void onSuccess() {
        Log.d(TAG, "Document created successfully");
      }

      @Override
      protected void onUpdateConflict(
          UpdateConflictException updateConflictException) {
        Log.d(TAG, "Got an update conflict for: " + item.toString());
      }
    };
    createItemTask.execute();
  }

  public void toggleItemChecked(final JsonNode item) {
    TouchDBItemUtils.toggleCheck(item);

    OPrimeEktorpAsyncTask updateTask = new OPrimeEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        couchDbConnector.update(item);
      }

      @Override
      protected void onSuccess() {
        Log.d(TAG, "Document updated successfully");
      }

      @Override
      protected void onUpdateConflict(
          UpdateConflictException updateConflictException) {
        Log.d(TAG, "Got an update conflict for: " + item.toString());
      }
    };
    updateTask.execute();
  }

  public void deleteDBItem(final JsonNode item) {
    OPrimeEktorpAsyncTask deleteTask = new OPrimeEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        couchDbConnector.delete(item);
      }

      @Override
      protected void onSuccess() {
        Log.d(TAG, "Document deleted successfully");
      }

      @Override
      protected void onUpdateConflict(
          UpdateConflictException updateConflictException) {
        Log.d(TAG, "Got an update conflict for: " + item.toString());
      }
    };
    deleteTask.execute();
  }

}
