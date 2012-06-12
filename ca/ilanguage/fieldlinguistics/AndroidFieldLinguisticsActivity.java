package ca.ilanguage.fieldlinguistics;

import java.io.File;

import com.google.code.p.leveldb.LevelDB;

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
import android.widget.TextView;
import android.widget.Toast;

public class AndroidFieldLinguisticsActivity extends Activity {
	private static final String TAG = "AndroidFieldLinguisticsActivity";
	public static final boolean D = true;
	private WebView mWebView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.addJavascriptInterface(new JavaScriptInterface(this),
				"Android");
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
		
		// TODO remove this. It's just an example to prove that the LevelDB library works
		String mDBdir = this.getFilesDir().getAbsolutePath() + File.separator + "db";
		LevelDB.dbDestroy(mDBdir);
		LevelDB.dbOpen(mDBdir);
		LevelDB.dbPut("firstkey", "this is the value of the first key");
		LevelDB.dbPut("secondkey", "this is the value of the first key");
		LevelDB.dbPut("keyToDelete",
				"this is the value of the key that i want to delete");
		LevelDB.dbPut("fourthkey", "this is the value of the fourth key");
		LevelDB.dbDelete("keyToDelete");
		Toast.makeText(getApplicationContext(), LevelDB.dbGet("fourthkey"), Toast.LENGTH_LONG).show();
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
		Context mContext;

		/** Instantiate the interface and set the context */
		JavaScriptInterface(Context c) {
			mContext = c;
		}

		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
		}
		
		public void log(String msg) {
			Log.d(TAG, msg);
		}
	}
}