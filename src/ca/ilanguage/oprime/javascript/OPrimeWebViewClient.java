package ca.ilanguage.oprime.javascript;

import ca.ilanguage.oprime.Config;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OPrimeWebViewClient extends WebViewClient {
  public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    handler.proceed();
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String uri) {
    if (Uri.parse(uri).getHost().equals("localhost") || uri.startsWith("file:///android_asset") || uri.contains("pivot88.com:6984/login")) {
      // do not override; let WebView load the page
      Log.d(Config.TAG, "Not overriding " + uri);
      return false;
    }
    Log.d(Config.TAG, "Taking user to a default viewer for " + uri);

    // Otherwise, launch another Activity that can handle this uri (ie let the
    // HTML5 app open a pdf, open an apk or open a csv etc)
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    view.getContext().startActivity(intent);

    return true;
  }


}
