package ca.ilanguage.oprime.javascript;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * Source http://code.google.com/p/android/issues/attachmentText?id=17535&aid=175350100000&name=AssetIncludeWorkaround.java&token=2Xxhmu5Rbyi1ARW-ryk5L5MvaZU%3A1354052830422
 * 
 * for sdk 11-15 
 */
public class AssetIncludeWorkaroundWebViewClient extends WebViewClient {
  private boolean D = false;
  private Context mContext;
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

  public AssetIncludeWorkaroundWebViewClient(Context context) {
    this.mContext = context;
  }

  private InputStream inputStreamForAndroidResource(String url) {
    final String ANDROID_ASSET = "file:///android_asset/";

    if (url.startsWith(ANDROID_ASSET)) {
      url = url.replaceFirst(ANDROID_ASSET, "");
      try {
        AssetManager assets = this.mContext.getAssets();
        Uri uri = Uri.parse(url);
        if (this.D)
          Log.d("OPrime", "The URL was in the assets. (removed the assets and sending the contents of the file)? to the browser. " + url);

        return assets.open(uri.getPath(), AssetManager.ACCESS_STREAMING);
      } catch (IOException e) {
        if (this.D)
          Log.d("OPrime", "The URL was in the assets. But there was an IOException when opening it. " + url);

      }
    } else {
      if (this.D)
        Log.d("OPrime", "The URL was not the assets. Not performing any action, letting the WebView handle it normally. " + url);

    }
    return null;
  }

  @SuppressLint("NewApi")
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    if (this.D)
      Log.d("OPrime", "Intercepting an URL requestin the webview " + url);
    InputStream stream = this.inputStreamForAndroidResource(url);
    if (stream != null) {
      return new WebResourceResponse(null, null, stream);
    }
    return super.shouldInterceptRequest(view, url);
  }

}
