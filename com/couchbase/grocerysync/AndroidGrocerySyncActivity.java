package com.couchbase.grocerysync;

import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.ReplicationCommand;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult.Row;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.DesignDocument;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;
import com.couchbase.ektorp.CouchbaseDocumentAsyncTask;
import com.couchbase.ektorp.CouchbaseReplicationAsyncTask;

public class AndroidGrocerySyncActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnKeyListener {

	public static String TAG = "GrocerySync";

	//constants
	public static final String DATABASE_NAME = "grocery-sync";

	//splash screen
	protected SplashScreenDialog splashDialog;

	//main screen
	protected EditText addItemEditText;
	protected ListView itemListView;
	protected GrocerySyncListAdapter itemListViewAdapter;

	//couch internals
	protected static ServiceConnection couchServiceConnection;
	protected static AndroidHttpClient httpClient;

	//ektorp impl
	protected CouchDbConnector couchDbConnector;
	protected ReplicationCommand pushReplication;
	protected ReplicationCommand pullReplication;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        //connect items from layout
        addItemEditText = (EditText)findViewById(R.id.addItemEditText);
        itemListView = (ListView)findViewById(R.id.itemListView);

        //connect listeners
		addItemEditText.setOnKeyListener(this);

		//show splash and start couch
    	showSplashScreen();
    	startCouch();
    }

	protected void onDestroy() {
		Log.v(TAG, "onDestroy");

		unbindService(couchServiceConnection);

		//need to stop the async task thats following the changes feed
		itemListViewAdapter.cancelContinuous();

		//clean up our http client connection manager
		if(httpClient != null) {
			httpClient.shutdown();
		}

		super.onDestroy();
	}

    protected ICouchbaseDelegate couchCallbackHandler = new ICouchbaseDelegate() {

		public void exit(String error) {
			AlertDialog.Builder builder = new AlertDialog.Builder(AndroidGrocerySyncActivity.this);
			builder.setMessage(error)
			       .setCancelable(false)
			       .setPositiveButton(R.string.error_dialog_button, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   AndroidGrocerySyncActivity.this.finish();
			           }
			       })
			       .setTitle(R.string.error_dialog_title);
			AlertDialog alert = builder.create();
			alert.show();
		}

		public void couchbaseStarted(String host, int port) {
			Log.v(TAG, "got couch started " + host + " " + port);
			AndroidGrocerySyncActivity.this.removeSplashScreen();
			startEktorp(host, port);
		}
	};

	protected void startCouch() {
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), couchCallbackHandler);
		couchServiceConnection = couch.startCouchbase();
	}

	protected void startEktorp(String host, int port) {
		Log.v(TAG, "starting ektorp");

		if(httpClient != null) {
			httpClient.shutdown();
		}
		httpClient = (AndroidHttpClient) new AndroidHttpClient.Builder().host(host).port(port).maxConnections(100).build();

		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		couchDbConnector = dbInstance.createConnector(DATABASE_NAME, true);

		//ensure we have a design document with a view
		String dDocId = "_design/grocery-local";
		String byDateViewName = "byDate";
		String byDateViewMapFunction = "function(doc) {if (doc.created_at) emit(doc.created_at, doc);}";

		//update the design document if it exists, or create it if it does not exist
		try {
			DesignDocument dDoc = couchDbConnector.get(DesignDocument.class, dDocId);
			dDoc.addView("byDate", new DesignDocument.View(byDateViewMapFunction));
			couchDbConnector.update(dDoc);
		}
		catch(DocumentNotFoundException ndfe) {
			DesignDocument dDoc = new DesignDocument(dDocId);
			dDoc.addView("byDate", new DesignDocument.View(byDateViewMapFunction));
			couchDbConnector.create(dDoc);
		}


		//attach list adapter to the list and handle clicks
		ViewQuery viewQuery = new ViewQuery().designDocId(dDocId).viewName(byDateViewName).descending(true);
		itemListViewAdapter = new GrocerySyncListAdapter(AndroidGrocerySyncActivity.this, couchDbConnector, viewQuery);
		itemListView.setAdapter(itemListViewAdapter);
		itemListView.setOnItemClickListener(AndroidGrocerySyncActivity.this);
		itemListView.setOnItemLongClickListener(AndroidGrocerySyncActivity.this);


		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		Log.v(TAG, "before push starts");

		pushReplication = new ReplicationCommand.Builder()
			.source(DATABASE_NAME)
			.target(prefs.getString("sync_url", "http://couchbase.iriscouch.com/grocery-sync"))
			.continuous(true)
			.build();

		//dbInstance.replicate(pushReplication);
		new CouchbaseReplicationAsyncTask(dbInstance).execute(pushReplication);

		Log.v(TAG, "before pull starts");

		pullReplication = new ReplicationCommand.Builder()
			.source(prefs.getString("sync_url", "http://couchbase.iriscouch.com/grocery-sync"))
			.target(DATABASE_NAME)
			.continuous(true)
			.build();

		//dbInstance.replicate(pullReplication);
		new CouchbaseReplicationAsyncTask(dbInstance).execute(pullReplication);
		Log.v(TAG, "finished starting ektorp");
	}

	public void stopEktorp() {
	}


	/**
	 * Handle typing item text
	 */
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

			String inputText = addItemEditText.getText().toString();
			if(!inputText.equals("")) {
				createGroceryItem(inputText);
			}
			addItemEditText.setText("");
			return true;
		}
		return false;
	}

	/**
	 * Handle click on item in list
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Row row = (Row)parent.getItemAtPosition(position);
        JsonNode item = row.getValueAsNode();
		toggleItemChecked(item);
	}

	/**
	 * Handle long-click on item in list
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Row row = (Row)parent.getItemAtPosition(position);
        final JsonNode item = row.getValueAsNode();
		JsonNode textNode = item.get("text");
		String itemText = "";
		if(textNode != null) {
			itemText = textNode.getTextValue();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(AndroidGrocerySyncActivity.this);
		AlertDialog alert = builder.setTitle("Delete Item?")
			   .setMessage("Are you sure you want to delete \"" + itemText + "\"?")
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   deleteGroceryItem(item);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // Handle Cancel
		           }
		       })
		       .create();

		alert.show();

		return true;
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

	/**
	 * Add settings item to the menu
	 */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Launch the settings activity
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, GrocerySyncPreferencesActivity.class));
                return true;
        }
        return false;
    }

    public void createGroceryItem(String name) {
    	JsonNode item = GroceryItemUtils.createWithText(name);
    	CouchbaseDocumentAsyncTask createTask = new CouchbaseDocumentAsyncTask(couchDbConnector, CouchbaseDocumentAsyncTask.OPERATION_CREATE);
    	createTask.execute(item);
    }

    public void toggleItemChecked(JsonNode item) {
    	item = GroceryItemUtils.toggleCheck(item);
    	CouchbaseDocumentAsyncTask updateTask = new CouchbaseDocumentAsyncTask(couchDbConnector, CouchbaseDocumentAsyncTask.OPERATION_UPDATE);
    	updateTask.execute(item);
    }

    public void deleteGroceryItem(JsonNode item) {
    	CouchbaseDocumentAsyncTask deleteTask = new CouchbaseDocumentAsyncTask(couchDbConnector, CouchbaseDocumentAsyncTask.OPERATION_DELETE);
    	deleteTask.execute(item);
    }

}