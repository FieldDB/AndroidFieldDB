package ca.ilanguage.oprime.javascript;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.model.NonObfuscateable;
import ca.ilanguage.oprime.model.OPrimeApp;
import ca.ilanguage.oprime.ui.HTML5Activity;
import ca.ilanguage.oprime.ui.HTML5GameActivity;

public class ExperimentJavaScriptInterface extends JavaScriptInterface implements NonObfuscateable {
  private static final long serialVersionUID = -8802714328569435146L;

  private HTML5GameActivity mUIParent;

  public ExperimentJavaScriptInterface(boolean d, String tag, String outputDir, Context context,
      HTML5GameActivity UIParent, String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
  }

  @JavascriptInterface
  public String fetchExperimentTitleJS() {
    return this.getApp().getExperiment().getTitle();
  }

  @JavascriptInterface
  public String fetchParticipantCodesJS() {
    return "[the,codes]";
  }

  @JavascriptInterface
  public String fetchSubExperimentsArrayJS() {
    return this.getApp().getSubExperimentTitles().toString();
  }

  public OPrimeApp getApp() {
    return (OPrimeApp) this.mUIParent.getApplication();
  }

  @Override
  public HTML5Activity getUIParent() {
    return this.mUIParent;
  }

  @JavascriptInterface
  public void launchSubExperimentJS(String subex) {
    if (this.D) {
      Log.d(this.TAG, "Launching sub experiment:" + subex);
    }
    final int currentSubExperiment = Integer.parseInt(subex);
    ((HTML5GameActivity) this.getUIParent()).setCurrentSubex(currentSubExperiment);

    String mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_kk_mm",
        new java.util.Date(System.currentTimeMillis()));
    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");

    OPrimeApp app = this.getApp();
    String resultsFile = app.getExperiment().getParticipant().getCode() + "_" + app.getLanguage()
        + currentSubExperiment + "_"
        + app.getSubExperiments().get(currentSubExperiment).getTitle().replaceAll(" ", "_") + "-" + mDateString;

    Intent intent = new Intent(app.getSubExperiments().get(currentSubExperiment).getIntentToCallThisSubExperiment());

    intent.putExtra(Config.EXTRA_SUB_EXPERIMENT, app.getSubExperiments().get(currentSubExperiment));
    intent.putExtra(Config.EXTRA_LANGUAGE, app.getLanguage().getLanguage());
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mOutputDir + "video/" + resultsFile + ".3gp");
    this.getUIParent().startActivityForResult(intent, Config.EXPERIMENT_COMPLETED);

    app.getSubExperiments().get(currentSubExperiment)
        .setResultsFileWithoutSuffix(this.mOutputDir + "video/" + resultsFile);
    if (this.D)
      Log.d(this.TAG, "setResultsFileWithoutSuffix sub experiment:" + resultsFile);

  }

  @JavascriptInterface
  public void setAutoAdvanceJS(String autoadvance) {
    if (autoadvance.equals("1")) {
      ((HTML5GameActivity) this.getUIParent()).setAutoAdvance(true);
    } else {
      ((HTML5GameActivity) this.getUIParent()).setAutoAdvance(false);
    }

  }

  @Override
  public void setUIParent(HTML5Activity UIParent) {
    this.mUIParent = (HTML5GameActivity) UIParent;
  }

  @Deprecated
  @JavascriptInterface
  public void startVideoRecorderWithResult() {
    String mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_kk_mm",
        new java.util.Date(System.currentTimeMillis()));
    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");

    OPrimeApp app = this.getApp();
    int currentSubExperiment = ((HTML5GameActivity) this.getUIParent()).getCurrentSubex();

    String resultsFile = app.getExperiment().getParticipant().getCode() + "_" + app.getLanguage()
        + currentSubExperiment + "_"
        + app.getSubExperiments().get(currentSubExperiment).getTitle().replaceAll(" ", "_") + "-" + mDateString;

    if (this.D) {
      Log.d(this.TAG, "Starting video/audio recording to:" + resultsFile);
    }
    this.startVideoRecorder(resultsFile);

    app.getSubExperiments().get(currentSubExperiment)
        .setResultsFileWithoutSuffix(this.mOutputDir + "video/" + resultsFile);
  }

}