package com.github.fielddb.javascript;

import android.content.Context;

public class HTML5JavaScriptInterface extends JavaScriptInterface {
  private static final long serialVersionUID = 373085850425945181L;

  HTML5Activity mUIParent;

  public HTML5JavaScriptInterface(String outputDir, Context context, HTML5Activity UIParent, String assetsPrefix) {
    super(outputDir, context, UIParent, assetsPrefix);
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