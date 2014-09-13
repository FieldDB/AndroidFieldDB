package ca.ilanguage.oprime.ui;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.javascript.AssetIncludeWorkaroundWebViewClient;
import ca.ilanguage.oprime.javascript.JavaScriptInterface;
import ca.ilanguage.oprime.javascript.OPrimeChromeClient;
import ca.ilanguage.oprime.javascript.OPrimeWebViewClient;

public abstract class HTML5Activity extends Activity {
  protected String logs;
  public WebView mWebView;

  public abstract JavaScriptInterface getJavaScriptInterface();

  public abstract void setJavaScriptInterface(JavaScriptInterface javaScriptInterface);

  protected abstract void setUpVariables();

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Config.CODE_PICTURE_TAKEN:
      if (data != null) {
        Log.d(Config.TAG, "Deprecated PICTURE_TAKEN ");
        break;
      }
    default:
      break;
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (Config.D)
      Log.d(Config.TAG, "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.fragment_html5webview);
    this.setUpVariables();
    this.prepareWebView();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the currently selected menu XML resource.
    MenuInflater inflater = this.getMenuInflater();
    inflater.inflate(R.menu.home_menu, menu);

    return true;
  }

  @Override
  protected void onDestroy() {
    if (this.getJavaScriptInterface().mMediaPlayer != null) {
      this.getJavaScriptInterface().mMediaPlayer.stop();
      this.getJavaScriptInterface().mMediaPlayer.release();
    }
    if (this.getJavaScriptInterface().mListenForEndAudioInterval != null && !this.getJavaScriptInterface().mListenForEndAudioInterval.isCancelled()) {
      this.getJavaScriptInterface().mListenForEndAudioInterval.cancel(true);
      // getJavaScriptInterface().mListenForEndAudioInterval = null;
    }
    super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.open_settings) {
      Intent i = new Intent(this.getBaseContext(), ParticipantDetails.class);
      i.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      this.startActivity(i);
      return true;
    } else if (item.getItemId() == R.id.language_settings) {
      Intent inte = new Intent(this.getBaseContext(), ParticipantDetails.class);
      this.startActivityForResult(inte, Config.CODE_SWITCH_LANGUAGE);
      inte.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      return true;
    } else if (item.getItemId() == R.id.result_folder) {
      final boolean fileManagerAvailable = isIntentAvailable(this, "org.openintents.action.PICK_FILE");
      if (!fileManagerAvailable) {
        Toast.makeText(
            this.getApplicationContext(),
            "To open and export recorded files or " + "draft data you can install the OI File Manager, " + "it allows you to browse your SDCARD directly on your mobile device."
                + " There are other apps which allow you to view the files, but OI is the one this app uses when you click on this button", Toast.LENGTH_LONG).show();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=org.openintents.filemanager"));
        this.startActivity(goToMarket);
      } else {
        Intent openResults = new Intent("org.openintents.action.PICK_FILE");
        openResults.setData(Uri.parse("file://" + Config.DEFAULT_OUTPUT_DIRECTORY));
        this.startActivity(openResults);
      }
      // Intent intentReplay = new Intent(getBaseContext(),
      // ParticipantDetails.class);
      // startActivityForResult(intentReplay, OPrime.REPLAY_RESULTS);
      return true;
    } else if (item.getItemId() == R.id.issue_tracker) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/iLanguage/OPrime/issues"));
      this.startActivity(browserIntent);
      return true;
    }
    return false;
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
  protected void prepareWebView() {
    this.mWebView = (WebView) this.findViewById(R.id.html5WebView);
    this.mWebView.addJavascriptInterface(this.getJavaScriptInterface(), "Android");

    OPrimeChromeClient customChromeClient = new OPrimeChromeClient();
    this.mWebView.setWebChromeClient(customChromeClient);

    this.mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
    // mWebView.setDownloadListener(new DownloadListener() {
    //
    // @Override
    // public void onDownloadStart(String url, String userAgent,
    // String contentDisposition, String mimetype, long contentLength) {
    // // handle download, here we use brower to download, also you can try
    // other approach.
    // Uri uri = Uri.parse(url);
    // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    // startActivity(intent);
    // }
    // });
    WebSettings webSettings = this.mWebView.getSettings();
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setSaveFormData(true);

    webSettings.setDefaultTextEncodingName("utf-8");
    webSettings.setAppCacheEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setDatabasePath(this.getFilesDir().getAbsolutePath() + "webdb/");
    if (Config.D)
      Log.d(Config.TAG, "Turning on dom storage enabled webSettings.setDomStorageEnabled " + this.getFilesDir().getAbsolutePath() + "/databases/");

    webSettings.setUserAgentString(webSettings.getUserAgentString() + " " + Config.USER_AGENT_STRING);

    /*
     * Android WebViews between (2.3?) and 4.1 inclusive can't handle anchors
     * (#) or parameters (?) if they are in the assets files(?). Many
     * workarounds are discussed here:
     * http://code.google.com/p/android/issues/detail?id=17535#c100
     * 
     * The code below handles 3 cases:
     */

    /*
     * SDK >= 16 : simply use a new method on webSettings
     * setAllowUniversalAccessFromFileURLs
     */
    if (android.os.Build.VERSION.SDK_INT >= 16) {
      webSettings.setAllowUniversalAccessFromFileURLs(true);
      this.mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

    /*
     * 11 >= SDK <= 15 : use someones java class which seems to load contents of
     * assets folders into a url(?) thereby avoiding loading from the assets
     * folder
     */
    else if (android.os.Build.VERSION.SDK_INT >= 11 && android.os.Build.VERSION.SDK_INT <= 15) {
      Log.w(Config.TAG, "This Android SDK " + android.os.Build.VERSION.SDK_INT
          + " has a bug in the WebView which gives a file not found error if the HTML5 uses a # or ? to set variables.");
      this.mWebView.setWebViewClient(new AssetIncludeWorkaroundWebViewClient(this));
      // mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

    /*
     * SDK <= 10 : use a normal OPrime WebViewClient, the WebView loading from
     * assets works as expected
     */
    else if (android.os.Build.VERSION.SDK_INT <= 10) {
      Log.w(Config.TAG, "This Android SDK " + android.os.Build.VERSION.SDK_INT + " may or may not be able to display a file if the HTML5 uses a # or ? to set variables.");
      this.mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

  }

  public class HTML5JavaScriptInterface extends JavaScriptInterface {
    private static final long serialVersionUID = 373085850425945181L;

    HTML5Activity mUIParent;

    public HTML5JavaScriptInterface(boolean d, String tag, String outputDir, Context context, HTML5Activity UIParent, String assetsPrefix) {
      super(d, tag, outputDir, context, UIParent, assetsPrefix);
    }

    public HTML5JavaScriptInterface(Context context) {
      super(context);
    }

    @Override
    public HTML5Activity getUIParent() {
      return this.mUIParent;
    }

    @Override
    public void setUIParent(HTML5Activity UIParent) {
      this.mUIParent = UIParent;
    }
  }

  public static Bitmap decodeBase64(String input) {
    byte[] decodedByte = Base64.decode(input, 0);
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
  }

  /*
   * http://stackoverflow.com/questions/9768611/encode-and-decode-bitmap-object-in
   * -base64-string-in-android
   */
  public static String encodeTobase64(Bitmap image) {
    Bitmap immagex = image;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    byte[] b = baos.toByteArray();
    String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

    Log.e("LOOK", imageEncoded);
    return imageEncoded;
  }

  public static boolean isIntentAvailable(Context context, String action) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(action);
    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  @Deprecated
  public String getAppLogs(String type) {
    String logcatArgs = "";
    int processID = android.os.Process.myPid();
    if (type == null) {
      logcatArgs = "logcat -d TDDatabase:W " + Config.TAG + ":W dalvikvm:W  *:S ";// |
      // grep
      // "
      // + processID;
    } else {
      // logcatArgs = "-v time "+TAG+" TDDatabase dalvikvm *:S";
      logcatArgs = "logcat -d TDDatabase:I " + Config.TAG + ":I dalvikvm:D  *:S | grep " + processID;
    }
    Process mLogcatProc = null;
    BufferedReader reader = null;
    this.logs = "Error collecting logs for " + Config.TAG;
    try {
      mLogcatProc = Runtime.getRuntime().exec(logcatArgs.split(" "));

      reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));

      String line;
      final StringBuilder log = new StringBuilder();
      String separator = System.getProperty("line.separator");

      for (int l = 0; l < 20; l++) {
        // while ((line = reader.readLine()) != null &&
        // System.currentTimeMillis() - previoustimestamp < 500) {
        line = reader.readLine();
        if (line == null) {
          break;
        }
        log.append(line);
        // Log.d(TAG, "Log for bug report: " + line);
        log.append(separator);

      }
      this.logs = log.toString();
    }

    catch (IOException e) {
      Log.d(Config.TAG, "Unable to collect logs for " + Config.TAG);
    }

    finally {
      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
          Log.d(Config.TAG, "Unable to collect logs for " + Config.TAG);
        }

    }
    return this.logs;

  }

}
