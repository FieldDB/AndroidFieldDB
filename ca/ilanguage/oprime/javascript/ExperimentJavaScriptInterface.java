package ca.ilanguage.oprime.javascript;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.model.Experiment;
import ca.ilanguage.oprime.model.NonObfuscateable;
import ca.ilanguage.oprime.ui.HTML5Activity;
import ca.ilanguage.oprime.ui.HTML5GameActivity;

public class ExperimentJavaScriptInterface extends JavaScriptInterface implements NonObfuscateable {
  private static final long serialVersionUID = -8802714328569435146L;
  private HTML5GameActivity mUIParent;
  protected WebView mWebView; 
  protected Experiment mExperiment;
  protected Boolean mAutoAdvance = false;
  protected int mCurrentSubex = 0;
  
  public ExperimentJavaScriptInterface(boolean d, String tag, String outputDir, Context context, HTML5GameActivity UIParent, String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
  }

  @JavascriptInterface
  @Deprecated
  public String fetchExperimentTitleJS() {
    return "";
  }

  @JavascriptInterface
  public String fetchParticipantCodesJS() {
    return "[the,codes]";
  }

  @JavascriptInterface
  @Deprecated
  public String fetchSubExperimentsArrayJS() {
    return "";
  }

  @Override
  public HTML5Activity getUIParent() {
    return this.mUIParent;
  }

  @JavascriptInterface
  @Deprecated
  public void launchSubExperimentJS(String subex) {
    if (this.D) {
      Log.d(Config.TAG, "Launching sub experiment:" + subex);
    }
    Log.w(Config.TAG, "TODO Update Launching sub experiment:" + subex);

    
//    final int currentSubExperiment = Integer.parseInt(subex);
//    this.setCurrentSubex(currentSubExperiment);
//
//    String mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_kk_mm", new java.util.Date(System.currentTimeMillis()));
//    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");
//
//    OPrimeApp app = this.getApp();
//    String resultsFile = app.getExperiment().getParticipant().getCode() + "_" + app.getLanguage() + currentSubExperiment + "_"
//        + app.getSubExperiments().get(currentSubExperiment).getTitle().replaceAll(" ", "_") + "-" + mDateString;
//
//    Intent intent = new Intent(app.getSubExperiments().get(currentSubExperiment).getIntentToCallThisSubExperiment());
//
//    intent.putExtra(Config.EXTRA_SUB_EXPERIMENT, app.getSubExperiments().get(currentSubExperiment));
//    intent.putExtra(Config.EXTRA_LANGUAGE, app.getLanguage().getLanguage());
//    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mOutputDir + "video/" + resultsFile + ".3gp");
//    this.getUIParent().startActivityForResult(intent, Config.EXPERIMENT_COMPLETED);
//
//    app.getSubExperiments().get(currentSubExperiment).setResultsFileWithoutSuffix(this.mOutputDir + "video/" + resultsFile);
//    if (this.D)
//      Log.d(Config.TAG, "setResultsFileWithoutSuffix sub experiment:" + resultsFile);

  }

  @JavascriptInterface
  @Deprecated
  public void setAutoAdvanceJS(String autoadvance) {
    if (autoadvance.equals("1")) {
      this.mAutoAdvance = true;
    } else {
      this.mAutoAdvance = false;
    }

  }

  @Override
  public void setUIParent(HTML5Activity UIParent) {
    this.mUIParent = (HTML5GameActivity) UIParent;
  }

  @Deprecated
  @JavascriptInterface
  public void startVideoRecorderWithResult() {
    String mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_kk_mm", new java.util.Date(System.currentTimeMillis()));
    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");

    int currentSubExperiment = this.mCurrentSubex;

    String resultsFile = mExperiment.getParticipant().getCode() + "_" + mExperiment.getLanguage() + currentSubExperiment + "_"
        + mExperiment.getSubExperiments().get(currentSubExperiment).getTitle().replaceAll(" ", "_") + "-" + mDateString;

    if (this.D) {
      Log.d(Config.TAG, "Starting video/audio recording to:" + resultsFile);
    }
    this.startVideoRecorder(resultsFile);

    mExperiment.getSubExperiments().get(currentSubExperiment).setResultsFileWithoutSuffix(this.mOutputDir + "video/" + resultsFile);
  }

}