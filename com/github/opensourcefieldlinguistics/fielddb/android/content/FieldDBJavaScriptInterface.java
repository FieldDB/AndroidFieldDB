package com.github.opensourcefieldlinguistics.fielddb.android.content;

import com.github.opensourcefieldlinguistics.fielddb.android.activity.FieldDBActivity;

import android.content.Context;
import android.webkit.JavascriptInterface;
import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.content.JavaScriptInterface;
import ca.ilanguage.oprime.content.NonObfuscateable;

public class FieldDBJavaScriptInterface extends JavaScriptInterface implements
    NonObfuscateable {

  private static final long serialVersionUID = -2145096852409425984L;
  FieldDBActivity mUIParent;

  public FieldDBJavaScriptInterface(boolean d, String tag, String outputDir,
      Context context, HTML5Activity UIParent, String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
  }

  public FieldDBJavaScriptInterface(Context context) {
    super(context);
  }

  @Override
  public FieldDBActivity getUIParent() {
    return mUIParent;
  }

  @Override
  public void setUIParent(HTML5Activity UIParent) {
    this.mUIParent = (FieldDBActivity) UIParent;
  }

  @JavascriptInterface
  public void setCredentials(String dbname, String username, String password,
      String couchDBServerDomain) {
    getUIParent().setCouchInfoBasedOnUserDb(dbname, username, password,
        couchDBServerDomain);
  }

  /*
   * This method should be used when the app first starts
   */
  @JavascriptInterface
  public void setCredentialsAndReplicate(String dbname, String username,
      String password, String couchDBServerDomain) {
    setCredentials(dbname, username, password, couchDBServerDomain);
    turnOnReplication();
  }

  @JavascriptInterface
  public String getLocalCouchAppURL() {
    return getUIParent().getLocalCouchAppInitialURL();
  }

  @JavascriptInterface
  public String getRemoteServerDomain() {
    return getUIParent().getRemoteCouchServerDomain();
  }

  @JavascriptInterface
  public void turnOnReplication() {
    getUIParent().beginReplicating();
  }

  @JavascriptInterface
  public void turnOffReplication() {
    getUIParent().stopReplicating();
  }
}
