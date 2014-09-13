package com.github.opensourcefieldlinguistics.fielddb.android.content;

import com.github.opensourcefieldlinguistics.fielddb.android.activity.FieldDBActivity;
import com.github.opensourcefieldlinguistics.javascript.HTML5ReplicatingActivity;
import com.github.opensourcefieldlinguistics.javascript.ReplicatingJavaScriptInterface;

import android.content.Context;
import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.content.NonObfuscateable;

public class FieldDBJavaScriptInterface extends ReplicatingJavaScriptInterface implements
    NonObfuscateable {

  private static final long serialVersionUID = -2145096852409425984L;
  FieldDBActivity mUIParent;

  public FieldDBJavaScriptInterface(boolean d, String tag, String outputDir,
      Context context, HTML5ReplicatingActivity UIParent, String assetsPrefix) {
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

}
