package com.github.fielddb.javascript;

import com.github.fielddb.Config;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public abstract class ReplicatingJavaScriptInterface extends JavaScriptInterface {
  private static final long serialVersionUID = -8947624888326897689L;

  public ReplicatingJavaScriptInterface(String outputDir, Context context,
      HTML5ReplicatingActivity UIParent, String assetsPrefix) {
    super(outputDir, context, UIParent, assetsPrefix);
    // TODO Auto-generated constructor stub
  }

  public ReplicatingJavaScriptInterface(Context context) {
    super(context);
  }

  public abstract HTML5ReplicatingActivity getUIParent();

  public abstract void setUIParent(HTML5Activity UIParent);

  @JavascriptInterface
  public void setCredentials(String dbname, String username, String password, String couchDBServerDomain) {
    if (password.contains("@")) {
      Log.d(Config.TAG, "The users password has a @ this wont work with couchdb replication. Refusing to set their password.");
      return;
    }
    getUIParent().setCouchInfoBasedOnUserDb(dbname, username, password, couchDBServerDomain);
  }

  /*
   * This method should be used when the app first starts
   */
  @JavascriptInterface
  public void setCredentialsAndReplicate(String dbname, String username, String password, String couchDBServerDomain) {
    setCredentials(dbname, username, password, couchDBServerDomain);
    turnOnReplication();
  }

  @JavascriptInterface
  public void addSuccessfulOfflineDatabase(String dbname) {
    getUIParent().addSuccessfulOfflineDatabase(dbname);
  }

  @JavascriptInterface
  public String getOfflineDBs() {
    return getUIParent().getOfflineDBs();
  }

  @JavascriptInterface
  public int getTouchDBListenerPort() {
    return getUIParent().getTouchDBListenerPort();
  }

  @JavascriptInterface
  public String getLocalCouchAppURL() {
    return getUIParent().getLocalCouchAppInitialURL();
  }

  @JavascriptInterface
  public String getRemoteServerDomain() {
    return getUIParent().getCompleteURLtoCouchDBServer();
  }

  @JavascriptInterface
  public String getCurrentDBName() {
    return getUIParent().getDatabaseName();
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
