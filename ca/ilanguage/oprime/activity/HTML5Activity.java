package ca.ilanguage.oprime.activity;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.content.AssetIncludeWorkaround;
import ca.ilanguage.oprime.content.OPrime;
import ca.ilanguage.oprime.content.JavaScriptInterface;
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

public abstract class HTML5Activity extends Activity {
  protected String TAG = "HTML5Activity";
  public boolean D = true;

  protected String mOutputDir;
  protected String mInitialAppServerUrl;
  public WebView mWebView;
  protected String mWebAppBaseDir;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.html5webview);
    setUpVariables();
    prepareWebView();
  }

  public abstract JavaScriptInterface getJavaScriptInterface();

  public abstract void setJavaScriptInterface(
      JavaScriptInterface javaScriptInterface);

  public abstract Application getApp();

  protected abstract void setUpVariables();

  @SuppressLint("SetJavaScriptEnabled")
  protected void prepareWebView() {
    mWebView = (WebView) findViewById(R.id.html5WebView);
    mWebView.addJavascriptInterface(this.getJavaScriptInterface(), "Android");

    MyWebChromeClient customChromeClient = new MyWebChromeClient();
    customChromeClient.setParentActivity(this);
    mWebView.setWebChromeClient(customChromeClient);

    mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setSaveFormData(true);

    webSettings.setDefaultTextEncodingName("utf-8");
    webSettings.setAppCacheEnabled(true);
    webSettings.setDomStorageEnabled(true);
    // webSettings.setDatabasePath(this.getFilesDir().getAbsolutePath()+"webdb/");
    // //TODO change to this
    webSettings.setDatabasePath("/data/data/"
        + getApplicationContext().getPackageName() + "/databases/");
    if (D)
      Log.d(TAG,
          "Turning on dom storage enabled webSettings.setDomStorageEnabled "
              + "/data/data/" + getApplicationContext().getPackageName()
              + "/databases/");

    webSettings.setUserAgentString(webSettings.getUserAgentString() + " "
        + OPrime.USER_AGENT_STRING);
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
      mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

    /*
     * 11 >= SDK <= 15 : use someones java class which seems to load contents of
     * assets folders into a url(?) thereby avoiding loading from the assets
     * folder
     */
    else if (android.os.Build.VERSION.SDK_INT >= 11
        && android.os.Build.VERSION.SDK_INT <= 15) {
      Log.w(
          TAG,
          "This Android SDK "
              + android.os.Build.VERSION.SDK_INT
              + " has a bug in the WebView which gives a file not found error if the HTML5 uses a # or ? to set variables.");
      mWebView
          .setWebViewClient(new OPrimeWebViewClientWorkaroundForHTML5Anchors(
              this));
      // mWebView.setWebViewClient(new OPrimeWebViewClient());

    }

    /*
     * SDK <= 10 : use a normal OPrime WebViewClient, the WebView loading from
     * assets works as expected
     */
    else if (android.os.Build.VERSION.SDK_INT <= 10) {
      Log.w(
          TAG,
          "This Android SDK "
              + android.os.Build.VERSION.SDK_INT
              + " may or may not be able to display a file if the HTML5 uses a # or ? to set variables.");
      mWebView.setWebViewClient(new OPrimeWebViewClient());
    }

    mWebView.loadUrl(mInitialAppServerUrl);
  }

  public void loadUrlToWebView(String message) {
    mWebView.loadUrl(message);
  }

  public static boolean isIntentAvailable(Context context, String action) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(action);
    List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
        PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case OPrime.PICTURE_TAKEN:
      if (data != null) {
        String pictureFilePath = data.getExtras().getString(
            OPrime.EXTRA_RESULT_FILENAME);
        mWebView
            .loadUrl("javascript:OPrime.hub.publish('pictureCaptureSucessfullyCompleted','"
                + pictureFilePath + "');");
        if (D)
          Log.d(TAG, "In the result for PICTURE_TAKEN. " + pictureFilePath);
        break;
      }
    default:
      break;
    }
  }

  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the currently selected menu XML resource.
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.home_menu, menu);

    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.open_settings) {
      Intent i = new Intent(getBaseContext(), ParticipantDetails.class);
      i.putExtra(OPrime.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      startActivity(i);
      return true;
    } else if (item.getItemId() == R.id.language_settings) {
      Intent inte = new Intent(getBaseContext(), ParticipantDetails.class);
      startActivityForResult(inte, OPrime.SWITCH_LANGUAGE);
      inte.putExtra(OPrime.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      return true;
    } else if (item.getItemId() == R.id.result_folder) {
      final boolean fileManagerAvailable = isIntentAvailable(this,
          "org.openintents.action.PICK_FILE");
      if (!fileManagerAvailable) {
        Toast
            .makeText(
                getApplicationContext(),
                "To open and export recorded files or "
                    + "draft data you can install the OI File Manager, "
                    + "it allows you to browse your SDCARD directly on your mobile device."
                    + " There are other apps which allow you to view the files, but OI is the one this app uses when you click on this button",
                Toast.LENGTH_LONG).show();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
            .parse("market://details?id=org.openintents.filemanager"));
        startActivity(goToMarket);
      } else {
        Intent openResults = new Intent("org.openintents.action.PICK_FILE");
        openResults.setData(Uri.parse("file://" + mOutputDir));
        startActivity(openResults);
      }
      // Intent intentReplay = new Intent(getBaseContext(),
      // ParticipantDetails.class);
      // startActivityForResult(intentReplay, OPrime.REPLAY_RESULTS);
      return true;
    } else if (item.getItemId() == R.id.issue_tracker) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW,
          Uri.parse("https://github.com/iLanguage/OPrime/issues"));
      startActivity(browserIntent);
      return true;
    }
    return false;
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

  public static Bitmap decodeBase64(String input) {
    byte[] decodedByte = Base64.decode(input, 0);
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
  }

  class MyWebChromeClient extends WebChromeClient {
    public Activity mParentActivity;

    public Activity getParentActivity() {
      return mParentActivity;
    }

    public void setParentActivity(Activity mParentActivity) {
      this.mParentActivity = mParentActivity;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
      if (cm.message() == null) {
        return true;
      }
      if (D)
        Log.d(TAG, cm.message() + " -- From line " + cm.lineNumber() + " of "
            + cm.sourceId());

      /*
       * Handle CORS server refusal to connect by telling user the entire error.
       */
      if (cm.message().startsWith("XMLHttpRequest cannot load")) {
        new AlertDialog.Builder(HTML5Activity.this)
            .setTitle("")
            .setMessage(
                cm.message() + "\nPlease contact the server administrator.")
            .setPositiveButton(android.R.string.ok,
                new AlertDialog.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    return;
                  }
                }).setCancelable(false).create().show();
      }
      return true;
    }

    /**
     * 
     * Could override like this, but that woudl make the saveApp funciton obligatory on the apps
     *  if it has been 30 seconds, then save the app, and redirect back to here after its done 
      if(mLastUnloadSaveAppCalledTimestamp - System.currentTimeMillis() > 30000){
        Log.d(TAG, "Calling window.saveApp("+url+")");
        view.loadUrl("javascript:window.saveApp("+url+")");
        mLastUnloadSaveAppCalledTimestamp = System.currentTimeMillis();
        return true;
      }else{
        return super.onJsBeforeUnload(view, url, message, result);
      }
     */
    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message,
        JsResult result) {
      view.loadUrl("javascript:window.saveApp()");
      Log.d(TAG, "Calling window.saveApp()");

      return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message,
        final JsResult result) {
      new AlertDialog.Builder(HTML5Activity.this)
          .setTitle("")
          .setMessage(message)
          .setPositiveButton(android.R.string.ok,
              new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  result.confirm();
                }
              }).setCancelable(false).create().show();

      return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message,
        String defaultValue, final JsPromptResult result) {
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
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse(defaultValue));
          } catch (ParseException e) {
            Log.i(TAG, "Incorrectly formatted date: " + defaultValue);
          }
        }

        // Create the dialog
        DatePickerDialog dialog = new DatePickerDialog(view.getContext(),
            new DatePickerDialog.OnDateSetListener() {
              @Override
              public void onDateSet(DatePicker view, int year, int monthOfYear,
                  int dayOfMonth) {
                // Send result back to JS
                result.confirm(year + "-"
                    + String.format("%02d", (monthOfYear + 1)) + "-"
                    + String.format("%02d", (dayOfMonth)) + " 00:00:00");
              }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE));

        // Ensure that he window.prompt even cancels successfully when the user
        // clicks "Cancel"
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancel_label),
            new DialogInterface.OnClickListener() {
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
        dialog.updateDate(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        // Display DatePickerDialog
        dialog.show();
      } else {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(mParentActivity);
        View promptsView = li.inflate(R.layout.dialog_edit_text, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            mParentActivity);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
            .findViewById(R.id.editTextDialogUserInput);

        // If there was a previous value, display it
        if (defaultValue != null) {
          userInput.setText(defaultValue);
        }

        if (message.toLowerCase().endsWith("number")) {
          userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        TextView prompt = (TextView) promptsView.findViewById(R.id.prompt);
        prompt.setText(message);
        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok_label),
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // get user input and set it to result
                    // edit text
                    Toast.makeText(getApplicationContext(),
                        userInput.getText().toString(), Toast.LENGTH_LONG)
                        .show();
                    result.confirm(userInput.getText().toString());
                  }
                })
            .setNegativeButton(getString(R.string.cancel_label),
                new DialogInterface.OnClickListener() {
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
    };
  }

  class OPrimeWebViewClientWorkaroundForHTML5Anchors extends
      AssetIncludeWorkaround {
    public OPrimeWebViewClientWorkaroundForHTML5Anchors(Context context) {
      super(context);
    }

    // protected String anchor;

    public void onReceivedSslError(WebView view, SslErrorHandler handler,
        SslError error) {
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

  class OPrimeWebViewClient extends WebViewClient {
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
        SslError error) {
      handler.proceed();
    }
  }

  @Override
  protected void onDestroy() {
    if (getJavaScriptInterface().mMediaPlayer != null) {
      getJavaScriptInterface().mMediaPlayer.stop();
      getJavaScriptInterface().mMediaPlayer.release();
    }
    if (getJavaScriptInterface().mListenForEndAudioInterval != null
        && !getJavaScriptInterface().mListenForEndAudioInterval.isCancelled()) {
      getJavaScriptInterface().mListenForEndAudioInterval.cancel(true);
      // getJavaScriptInterface().mListenForEndAudioInterval = null;
    }
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (D)
      Log.d(TAG,
          "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  public class HTML5JavaScriptInterface extends JavaScriptInterface {
    HTML5Activity mUIParent;

    private static final long serialVersionUID = 373085850425945181L;

    public HTML5JavaScriptInterface(boolean d, String tag, String outputDir,
        Context context, HTML5Activity UIParent, String assetsPrefix) {
      super(d, tag, outputDir, context, UIParent, assetsPrefix);
    }

    public HTML5JavaScriptInterface(Context context) {
      super(context);
    }

    @Override
    public HTML5Activity getUIParent() {
      return mUIParent;
    }

    @Override
    public void setUIParent(HTML5Activity UIParent) {
      this.mUIParent = UIParent;
    }

  }

}
