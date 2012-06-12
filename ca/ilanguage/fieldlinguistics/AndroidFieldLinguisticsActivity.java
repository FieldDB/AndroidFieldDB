package ca.ilanguage.fieldlinguistics;

import java.io.IOException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

import ca.ilanguage.fieldlinguistics.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AndroidFieldLinguisticsActivity extends Activity {
	private static final String TAG = "AndroidFieldLinguisticsActivity";
	public static final boolean D = true;
	private WebView mWebView;
	private static final String DATABASE_NAME = "tests";
	
	/* Needed to initialize TouchDB */
	static {
		TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Start an instance of TDServer
		TDServer server = null;
		String filesDir = getFilesDir().getAbsolutePath();
		try {
			server = new TDServer(filesDir);
		} catch (IOException e) {
			Log.e(TAG, "Error starting TDServer", e);
		}
		
		// Connect Ektorp to the TDServer instance
		HttpClient httpClient = new TouchDBHttpClient(server);
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		
		setContentView(R.layout.main);

		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.addJavascriptInterface(new JavaScriptInterface(this, dbInstance), "Android");
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setWebChromeClient(new MyWebChromeClient());
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSaveFormData(true);

		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setAppCacheEnabled(true);
		webSettings.setDomStorageEnabled(true);

		webSettings.setUserAgentString(webSettings.getUserAgentString() + " "
				+ getString(R.string.app_name));

		mWebView.loadUrl("file:///android_asset/index.html");
	}
	
	class MyWebChromeClient extends WebChromeClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			mWebView.loadUrl("javascript:console.log('URL: " + url + "')");
			if (D)
				Log.d(TAG, "Overrode Url loading in WebChromeClient");
			return true;
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage cm) {
			if (D)
				Log.d(TAG, cm.message() + " -- From line " + cm.lineNumber()
						+ " of " + cm.sourceId());
			return true;
		}
	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			mWebView.loadUrl("javascript:console.log('URL: " + url + "')");
			if (D)
				Log.d(TAG, "Overrode Url loading in WebViewClient");
			return true;
		}
	}	
	
	public class JavaScriptInterface {
		private static final String TAG = "JavaScriptInterface";
		private Context mContext;
		private CouchDbInstance couch;
		private CouchDbConnector db;

		/** Instantiate the interface and set the context */
		JavaScriptInterface(Context c, CouchDbInstance couch) {
			mContext = c;
			this.couch = couch;
			this.db = couch.createConnector(DATABASE_NAME, true);
		}

		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
		}
		
		public void log(String msg) {
			Log.d(TAG, msg);
		}
	}
}