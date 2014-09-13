package ca.ilanguage.oprime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.javascript.ExperimentJavaScriptInterface;
import ca.ilanguage.oprime.javascript.JavaScriptInterface;

public class HTML5GameActivity extends HTML5Activity {

  private JavaScriptInterface mJavaScriptInterface;

  @Override
  public JavaScriptInterface getJavaScriptInterface() {
    return this.mJavaScriptInterface;
  }

  protected void initExperiment() {
    this.mWebView.loadUrl(Config.getStartUrl());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Config.CODE_EXPERIMENT_COMPLETED:
      Log.d(Config.TAG, "Deprecated EXPERIMENT_COMPLETED ");
      break;
    case Config.CODE_PREPARE_TRIAL:
      Log.d(Config.TAG, "Deprecated PREPARE_TRIAL ");
      break;
    case Config.CODE_SWITCH_LANGUAGE:
      Log.d(Config.TAG, "Deprecated SWITCH_LANGUAGE ");
      break;
    case Config.CODE_REPLAY_RESULTS:
      Log.d(Config.TAG, "Deprecated REPLAY_RESULTS ");
      break;
    default:
      break;
    }
    super.onActivityResult(requestCode, resultCode, data);

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void prepareWebView() {
    super.prepareWebView();
  }

  @Override
  public void setJavaScriptInterface(JavaScriptInterface javaScriptInterface) {
    this.mJavaScriptInterface = javaScriptInterface;
  }

  @Override
  protected void setUpVariables() {
    this.setJavaScriptInterface(new ExperimentJavaScriptInterface(Config.D, Config.TAG, Config.DEFAULT_OUTPUT_DIRECTORY, this.getApplicationContext(), this, ""));
    if (Config.D)
      Log.d(Config.TAG, "Using the OPrime experiment javascript interface.");
  }
}