package ca.ilanguage.oprime.ui;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.javascript.JavaScriptInterface;
import ca.ilanguage.oprime.model.AssetIncludeWorkaround;

public abstract class HTML5Activity extends Activity {
  public class HTML5JavaScriptInterface extends JavaScriptInterface {
    private static final long serialVersionUID = 373085850425945181L;

    HTML5Activity             mUIParent;

    public HTML5JavaScriptInterface(boolean d, String tag, String outputDir, Context context, HTML5Activity UIParent,
        String assetsPrefix) {
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

  class MyWebChromeClient extends WebChromeClient {
    public Activity mParentActivity;

    public Activity getParentActivity() {
      return this.mParentActivity;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
      if (cm.message() == null) {
        return true;
      }
      // if (D)
      Log.d(HTML5Activity.this.TAG, cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());

      /*
       * Handle CORS server refusal to connect by telling user the entire error.
       */
      if (cm.message().startsWith("XMLHttpRequest cannot load")) {
        new AlertDialog.Builder(HTML5Activity.this).setTitle("")
            .setMessage(cm.message() + "\nPlease contact the server administrator.")
            .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                return;
              }
            }).setCancelable(false).create().show();
      }
      return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
      new AlertDialog.Builder(HTML5Activity.this).setTitle("").setMessage(message)
          .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              result.confirm();
            }
          }).setCancelable(false).create().show();

      return true;
    }

    /**
     * 
     * Could override like this, but that woudl make the saveApp funciton
     * obligatory on the apps if it has been 30 seconds, then save the app, and
     * redirect back to here after its done if(mLastUnloadSaveAppCalledTimestamp
     * - System.currentTimeMillis() > 30000){ Log.d(TAG,
     * "Calling window.saveApp("+url+")");
     * view.loadUrl("javascript:window.saveApp("+url+")");
     * mLastUnloadSaveAppCalledTimestamp = System.currentTimeMillis(); return
     * true; }else{ return super.onJsBeforeUnload(view, url, message, result); }
     */
    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
      view.loadUrl("javascript:window.saveApp()");
      Log.d(HTML5Activity.this.TAG, "Calling window.saveApp()");

      return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
      new AlertDialog.Builder(HTML5Activity.this).setTitle("").setMessage(message)
          .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              result.confirm();
            }
          }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              result.cancel();
            }
          }).create().show();

      return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
      // new AlertDialog.Builder(HTML5Activity.this)
      // .setTitle("")
      // .setMessage(message)
      // .setNeutralButton(android.R.string.cancel,
      // new AlertDialog.OnClickListener() {
      // public void onClick(DialogInterface dialog, int which) {
      // result.cancel();
      // }
      // })
      // .setPositiveButton(android.R.string.ok,
      // new AlertDialog.OnClickListener() {
      // public void onClick(DialogInterface dialog, int which) {
      // result.confirm();
      // }
      // }).setCancelable(false).create().show();

      if (message.toLowerCase().contains("date")) {
        // Get today's date
        Calendar calendar = Calendar.getInstance();

        if ((defaultValue != null) && (defaultValue.length() > 0)) {
          // Set it to the previously-entered date, if it's formatted correctly
          try {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(defaultValue));
          } catch (ParseException e) {
            Log.i(HTML5Activity.this.TAG, "Incorrectly formatted date: " + defaultValue);
          }
        }

        // Create the dialog
        DatePickerDialog dialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
          @Override
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Send result back to JS
            result.confirm(year + "-" + String.format("%02d", (monthOfYear + 1)) + "-"
                + String.format("%02d", (dayOfMonth)) + " 00:00:00");
          }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        // Ensure that he window.prompt even cancels successfully when the user
        // clicks "Cancel"
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Unknown", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
              // Send cancel back to JS
              result.cancel();
            }
          };
        });

        // Add the title to the dialog
        dialog.setTitle(message);

        // Set the date to appear in the dialog
        dialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        // Display DatePickerDialog
        dialog.show();
      } else {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this.mParentActivity);
        View promptsView = li.inflate(R.layout.dialog_edit_text, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.mParentActivity);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // If there was a previous value, display it
        if (defaultValue != null) {
          userInput.setText(defaultValue);
        }

        if (message.toLowerCase().endsWith("number")) {
          userInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        TextView prompt = (TextView) promptsView.findViewById(R.id.prompt);
        prompt.setText(message);
        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(HTML5Activity.this.getString(R.string.ok_label), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int id) {
                // get user input and set it to result
                // edit text
                Toast.makeText(HTML5Activity.this.getApplicationContext(), userInput.getText().toString(),
                    Toast.LENGTH_LONG).show();
                result.confirm(userInput.getText().toString());
              }
            })
            .setNegativeButton(HTML5Activity.this.getString(R.string.cancel_label),
                new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int id) {
                    result.cancel();
                  }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
      }

      return true;
    }

    public void setParentActivity(Activity mParentActivity) {
      this.mParentActivity = mParentActivity;
    };
  }

  class OPrimeWebViewClient extends WebViewClient {
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      handler.proceed();
    }
  }

  class OPrimeWebViewClientWorkaroundForHTML5Anchors extends AssetIncludeWorkaround {
    public OPrimeWebViewClientWorkaroundForHTML5Anchors(Context context) {
      super(context);
    }

    // protected String anchor;

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      handler.proceed();
    }

    // public void onPageFinished(WebView view, String url) {
    //
    // if (this.anchor != null) {
    // if (D)
    // Log.i(TAG, "\tURL anchor/parameters: " + this.anchor);
    // view.loadUrl("javascript:window.location.hash='" + this.anchor + "'");
    // this.anchor = null;
    // }
    // }
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

  public boolean   D   = true;

  protected String logs;

  protected String mInitialAppServerUrl;

  protected String mOutputDir;

  protected String mWebAppBaseDir;

  public WebView   mWebView;

  protected String TAG = "HTML5Activity";

  public abstract Application getApp();

  public String getAppLogs(String type) {
    String logcatArgs = "";
    int processID = android.os.Process.myPid();
    if (type == null) {
      logcatArgs = "logcat -d TDDatabase:W " + this.TAG + ":W dalvikvm:W  *:S ";// |
      // grep
      // "
      // + processID;
    } else {
      // logcatArgs = "-v time "+TAG+" TDDatabase dalvikvm *:S";
      logcatArgs = "logcat -d TDDatabase:I " + this.TAG + ":I dalvikvm:D  *:S | grep " + processID;
    }
    Process mLogcatProc = null;
    BufferedReader reader = null;
    this.logs = "Error collecting logs for " + this.TAG;
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
      Log.d(this.TAG, "Unable to collect logs for " + this.TAG);
    }

    finally {
      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
          Log.d(this.TAG, "Unable to collect logs for " + this.TAG);
        }

    }
    return this.logs;

  }

  public abstract JavaScriptInterface getJavaScriptInterface();

  public void loadUrlToWebView(String message) {
    this.mWebView.loadUrl(message);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Config.PICTURE_TAKEN:
      if (data != null) {
        String pictureFilePath = data.getExtras().getString(Config.EXTRA_RESULT_FILENAME);
        this.mWebView.loadUrl("javascript:OPrime.hub.publish('pictureCaptureSucessfullyCompleted','" + pictureFilePath
            + "');");
        if (this.D)
          Log.d(this.TAG, "In the result for PICTURE_TAKEN. " + pictureFilePath);
        break;
      }
    default:
      break;
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (this.D)
      Log.d(this.TAG, "Configuration has changed (rotation). Not redrawing the screen.");
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
    if (this.getJavaScriptInterface().mListenForEndAudioInterval != null
        && !this.getJavaScriptInterface().mListenForEndAudioInterval.isCancelled()) {
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
      this.startActivityForResult(inte, Config.SWITCH_LANGUAGE);
      inte.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      return true;
    } else if (item.getItemId() == R.id.result_folder) {
      final boolean fileManagerAvailable = isIntentAvailable(this, "org.openintents.action.PICK_FILE");
      if (!fileManagerAvailable) {
        Toast
            .makeText(
                this.getApplicationContext(),
                "To open and export recorded files or "
                    + "draft data you can install the OI File Manager, "
                    + "it allows you to browse your SDCARD directly on your mobile device."
                    + " There are other apps which allow you to view the files, but OI is the one this app uses when you click on this button",
                Toast.LENGTH_LONG).show();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
            .parse("market://details?id=org.openintents.filemanager"));
        this.startActivity(goToMarket);
      } else {
        Intent openResults = new Intent("org.openintents.action.PICK_FILE");
        openResults.setData(Uri.parse("file://" + this.mOutputDir));
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

    MyWebChromeClient customChromeClient = new MyWebChromeClient();
    customChromeClient.setParentActivity(this);
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
    // webSettings.setDatabasePath(this.getFilesDir().getAbsolutePath()+"webdb/");
    // //TODO change to this
    webSettings.setDatabasePath("/data/data/" + this.getApplicationContext().getPackageName() + "/databases/");
    if (this.D)
      Log.d(this.TAG, "Turning on dom storage enabled webSettings.setDomStorageEnabled " + "/data/data/"
          + this.getApplicationContext().getPackageName() + "/databases/");

    webSettings.setUserAgentString(webSettings.getUserAgentString() + " " + Config.USER_AGENT_STRING);

    // getJavaScriptInterface().setUIParent(this);

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
      Log.w(this.TAG, "This Android SDK " + android.os.Build.VERSION.SDK_INT
          + " has a bug in the WebView which gives a file not found error if the HTML5 uses a # or ? to set variables.");
      this.mWebView.setWebViewClient(new OPrimeWebViewClientWorkaroundForHTML5Anchors(this));
      // mWebView.setWebViewClient(new OPrimeWebViewClient());

    }

    /*
     * SDK <= 10 : use a normal OPrime WebViewClient, the WebView loading from
     * assets works as expected
     */
    else if (android.os.Build.VERSION.SDK_INT <= 10) {
      Log.w(this.TAG, "This Android SDK " + android.os.Build.VERSION.SDK_INT
          + " may or may not be able to display a file if the HTML5 uses a # or ? to set variables.");
      this.mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

    if (this.D)
      Log.i(this.TAG, "Loading " + this.mInitialAppServerUrl);
    this.mWebView.loadUrl(this.mInitialAppServerUrl);
  }

  public abstract void setJavaScriptInterface(JavaScriptInterface javaScriptInterface);

  protected abstract void setUpVariables();

}
