package ca.ilanguage.oprime.offline.content;

import android.content.Context;
import android.webkit.JavascriptInterface;
import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.content.JavaScriptInterface;
import ca.ilanguage.oprime.offline.activity.HTML5ReplicatingActivity;

public abstract class ReplicatingJavaScriptInterface extends
    JavaScriptInterface {
  private static final long serialVersionUID = -8947624888326897689L;

  public ReplicatingJavaScriptInterface(boolean d, String tag,
      String outputDir, Context context, HTML5ReplicatingActivity UIParent,
      String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
    // TODO Auto-generated constructor stub
  }

  public ReplicatingJavaScriptInterface(Context context) {
    super(context);
  }

  public abstract HTML5ReplicatingActivity getUIParent();

  public abstract void setUIParent(HTML5Activity UIParent);

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
  public int getTouchDBListenerPort(){
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
