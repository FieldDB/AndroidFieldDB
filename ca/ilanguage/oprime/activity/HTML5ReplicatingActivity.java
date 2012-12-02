package ca.ilanguage.oprime.activity;

import java.io.File;
import java.io.IOException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.ReplicationCommand;
import org.ektorp.android.util.EktorpAsyncTask;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ca.ilanguage.oprime.R;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class HTML5ReplicatingActivity extends HTML5Activity {

  protected String DATABASE_NAME = "dboprimesample";
  // constants sample for DB views
  protected String dDocName = "orpime-local";
  protected String dDocId = "_design/" + dDocName;
  protected String byDateViewName = "byDate";

  protected String mLocalTouchDBFileDir = "";
  protected String mRemoteCouchDBURL = "";

  // couch internals
  protected static TDServer server;
  protected static HttpClient httpClient;

  // ektorp impl
  protected CouchDbInstance dbInstance;
  protected CouchDbConnector couchDbConnector;
  protected ReplicationCommand pushReplicationCommand;
  protected ReplicationCommand pullReplicationCommand;

  // splash screen
  protected SplashScreenDialog splashDialog;

  int mBackPressedCount = 0;

  // static inializer to ensure that touchdb:// URLs are handled properly
  {
    TDURLStreamHandlerFactory.registerSelfIgnoreError();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    beginReplicating();
    super.onCreate(savedInstanceState);

    // show splash and start couch
    showSplashScreen();
    removeSplashScreen();

  }

  protected void beginReplicating() {
    mLocalTouchDBFileDir = this.getFilesDir().getAbsolutePath()
        + File.separator;
    startTouchDB();
    startEktorp();
  }

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

    if (mBackPressedCount < 2) {
      Toast.makeText(this, "Press again to exit.", Toast.LENGTH_LONG).show();

      if (httpClient != null) {
        httpClient.shutdown();
        return;
      }

      if (server != null) {
        server.close();
        return;
      }
    }
    super.onBackPressed();
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

  protected String desingDocNameParticipants = "participants";
  protected String desginDocIdParticipants = "_design/"
      + desingDocNameParticipants;
  protected String byStageViewName = "byStage";
  protected String byExperimentViewName = "byexperiment";
  protected String byExperimentAndPoViewName = "bySKUandPO";
  
  protected void setupTouchDBViews() {
    if (D)
      Log.d(TAG, "Setting TDView with a Javascript map reduce compiler,"
          + " this allows compiling of any views downloaded from couchapp.");
    TDView.setCompiler(new TDJavaScriptViewCompiler());
    
//    if(D)Log.d(TAG, "Not setting up any TouchDBViews");
    // install a view definition needed by the application
//    TDDatabase db = server.getDatabaseNamed(DATABASE_NAME);
//    TDView view = db.getViewNamed(String.format("%s/%s", dDocName,
//        byDateViewName));
//    view.setMapReduceBlocks(new TDViewMapBlock() {
//      @Override
//      public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
//        Object createdAt = document.get("created_at");
//        if (createdAt != null) {
//          emitter.emit(createdAt.toString(), document);
//        }
//
//      }
//    }, null, "1.0");
//    
//
   /*
    * Example of potential views delcaration with map reduce using Ektorp: untested.
    */
//    if (D)
//      Log.d(TAG, "Declaring TouchDBView stageView");
//    // install all view definitions needed by the application
//    TDDatabase db = server.getDatabaseNamed(DATABASE_NAME);
//    TDView stageView = db.getViewNamed(String.format("%s/%s",
//        desingDocNameParticipants, byStageViewName));
//    stageView.setMapReduceBlocks(new TDViewMapBlock() {
//      @Override
//      public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
//        if (D)
//          Log.d(TAG, "Running stageView map");
//
//        /*
//         * byStage={map=function(doc) { if(doc._id) { emit(doc._id,
//         * {participant_stage: doc.participant_stage, id: doc._id, experiment: doc.experiment,
//         * trial: doc.purchaseOrder, filters: [doc.experiment.number + ":experiment",
//         * doc.purchaseOrder.id + ":trial"], supplier: doc.supplier, client:
//         * doc.client}); } }}}
//         */
//        Object id = document.get("_id");
//        Object participant_stage = document.get("participant_stage");
//        Object experiment = document.get("experiment");
//        Object purchaseOrder = document.get("purchaseOrder");
//        Object supplier = document.get("supplier");
//        Object client = document.get("client");
//
//        if (id != null) {
//          emitter.emit(id.toString(), document);
//        }
//
//      }
//    }, new TDViewReduceBlock() {
//      public Object reduce(List<Object> keys, List<Object> values,
//          boolean rereduce) {
//        if (D)
//          Log.d(TAG, "Running stageView reduce");
//        return null;
//      }
//    }, "1.0");
//
//    if (D)
//      Log.d(TAG, "Declaring TouchDBView trialAndExperimentView");
//    TDView trialAndExperimentView = db.getViewNamed(String.format("%s/%s",
//        desingDocNameParticipants, byExperimentAndPoViewName));
//    trialAndExperimentView.setMapReduceBlocks(new TDViewMapBlock() {
//      @Override
//      public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
//        if (D)
//          Log.d(TAG, "Running trialAndExperimentView map");
//        /*
//         * bySKUandPO={map=function(doc) { if (doc.experiment.number) {
//         * emit(doc.experiment.number+":experiment", doc._id);
//         * emit(doc.purchaseOrder.id+":trial", doc._id); }
//         * 
//         * }, reduce=function(keys, values, rereduce) { return null; }}
//         */
//        Object id = document.get("_id");
//        Object participant_stage = document.get("participant_stage");
//        Object experiment = document.get("experiment");
//        Object purchaseOrder = document.get("purchaseOrder");
//        Object supplier = document.get("supplier");
//        Object client = document.get("client");
//
//        if (experiment != null) {
//          emitter.emit(id.toString(), document);
//        }
//
//      }
//    }, new TDViewReduceBlock() {
//      public Object reduce(List<Object> keys, List<Object> values,
//          boolean rereduce) {
//        if (D)
//          Log.d(TAG, "Running trialAndExperimentView reduce");
//        return null;
//      }
//    }, "1.0");
//
//    if (D)
//      Log.d(TAG, "Declaring TouchDBView experimentView");
//    TDView experimentView = db.getViewNamed(String.format("%s/%s",
//        desingDocNameParticipants, byExperimentViewName));
//    experimentView.setMapReduceBlocks(new TDViewMapBlock() {
//      @Override
//      public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
//        if (D)
//          Log.d(TAG, "Running experimentView map");
//        /*
//         * byexperiment={map=function(doc) { if (doc.experiment.number) {
//         * emit(doc.experiment.number+":experiment", doc._id);
//         * emit(doc.purchaseOrder.id+":trial", doc._id);
//         * //emit(doc.assignment.id+":assign", doc._id); }
//         * 
//         * }, reduce=function (key, values, rereduce) { return null; }}
//         */
//        Object id = document.get("_id");
//        Object participant_stage = document.get("participant_stage");
//        Object experiment = document.get("experiment");
//        Object purchaseOrder = document.get("purchaseOrder");
//        Object supplier = document.get("supplier");
//        Object client = document.get("client");
//
//        if (experiment != null) {
//          emitter.emit(id.toString(), document);
//        }
//
//      }
//    }, new TDViewReduceBlock() {
//      public Object reduce(List<Object> keys, List<Object> values,
//          boolean rereduce) {
//        if (D)
//          Log.d(TAG, "Running experimentView reduce");
//        return null;
//      }
//    }, "1.0");
    
    
    
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
        couchDbConnector = dbInstance.createConnector(DATABASE_NAME, true);
      }

      @Override
      protected void onSuccess() {
        Log.v(TAG, "Ektorp has started");
        mWebView.loadUrl(mInitialAppServerUrl);

        startReplications();
      }
    };
    startupTask.execute();
  }

  public void startReplications() {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    pushReplicationCommand = new ReplicationCommand.Builder()
        .source(DATABASE_NAME)
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
        .target(DATABASE_NAME).continuous(true).build();

    HTML5SyncEktorpAsyncTask pullReplication = new HTML5SyncEktorpAsyncTask() {

      @Override
      protected void doInBackground() {
        dbInstance.replicate(pullReplicationCommand);
      }
    };

    pullReplication.execute();
  }

  public void stopEktorp() {
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
  protected void removeSplashScreen() {
    if (splashDialog != null) {
      splashDialog.dismiss();
      splashDialog = null;
    }
  }

  /**
   * Shows the splash screen over the full Activity
   */
  protected void showSplashScreen() {
    splashDialog = new SplashScreenDialog(this);
    splashDialog.show();
  }

  public class SplashScreenDialog extends Dialog {

    protected ProgressBar splashProgressBar;
    protected TextView splashProgressMessage;

    public SplashScreenDialog(Context context) {
      super(context, R.style.SplashScreenStyle);

      setContentView(R.layout.splashscreen);
      setCancelable(false);
    }

  }
}
