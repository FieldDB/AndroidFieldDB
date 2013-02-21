package ca.ilanguage.oprime.content;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import ca.ilanguage.oprime.activity.HTML5Activity;
import ca.ilanguage.oprime.activity.HTML5GameActivity;
import ca.ilanguage.oprime.content.JavaScriptInterface;
import ca.ilanguage.oprime.content.OPrime;

public class ExperimentJavaScriptInterface extends JavaScriptInterface
    implements NonObfuscateable {
  private HTML5GameActivity mUIParent;

  private static final long serialVersionUID = -8802714328569435146L;

  public ExperimentJavaScriptInterface(boolean d, String tag, String outputDir,
      Context context, HTML5GameActivity UIParent, String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
  }

  @Override
  public HTML5Activity getUIParent() {
    return mUIParent;
  }

  @Override
  public void setUIParent(HTML5Activity UIParent) {
    this.mUIParent = (HTML5GameActivity) UIParent;
  }

  public OPrimeApp getApp() {
    return (OPrimeApp) mUIParent.getApplication();
  }

  @Deprecated
  @JavascriptInterface
  public void startVideoRecorderWithResult() {
    String mDateString = (String) android.text.format.DateFormat.format(
        "yyyy-MM-dd_kk_mm", new java.util.Date(System.currentTimeMillis()));
    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");

    OPrimeApp app = this.getApp();
    int currentSubExperiment = ((HTML5GameActivity) getUIParent())
        .getCurrentSubex();

    String resultsFile = app.getExperiment().getParticipant().getCode()
        + "_"
        + app.getLanguage()
        + currentSubExperiment
        + "_"
        + app.getSubExperiments().get(currentSubExperiment).getTitle()
            .replaceAll(" ", "_") + "-" + mDateString;

    if (D) {
      Log.d(TAG, "Starting video/audio recording to:" + resultsFile);
    }
    this.startVideoRecorder(resultsFile);

    app.getSubExperiments().get(currentSubExperiment)
        .setResultsFileWithoutSuffix(mOutputDir + "video/" + resultsFile);
  }

  @JavascriptInterface
  public void launchSubExperimentJS(String subex) {
    if (D) {
      Log.d(TAG, "Launching sub experiment:" + subex);
    }
    final int currentSubExperiment = Integer.parseInt(subex);
    ((HTML5GameActivity) getUIParent()).setCurrentSubex(currentSubExperiment);

    String mDateString = (String) android.text.format.DateFormat.format(
        "yyyy-MM-dd_kk_mm", new java.util.Date(System.currentTimeMillis()));
    mDateString = mDateString.replaceAll("/", "-").replaceAll(" ", "-");

    OPrimeApp app = this.getApp();
    String resultsFile = app.getExperiment().getParticipant().getCode()
        + "_"
        + app.getLanguage()
        + currentSubExperiment
        + "_"
        + app.getSubExperiments().get(currentSubExperiment).getTitle()
            .replaceAll(" ", "_") + "-" + mDateString;

    Intent intent = new Intent(app.getSubExperiments()
        .get(currentSubExperiment).getIntentToCallThisSubExperiment());

    intent.putExtra(OPrime.EXTRA_SUB_EXPERIMENT,
        app.getSubExperiments().get(currentSubExperiment));
    intent.putExtra(OPrime.EXTRA_LANGUAGE, app.getLanguage().getLanguage());
    intent.putExtra(OPrime.EXTRA_RESULT_FILENAME, mOutputDir + "video/"
        + resultsFile + ".3gp");
    getUIParent().startActivityForResult(intent, OPrime.EXPERIMENT_COMPLETED);

    app.getSubExperiments().get(currentSubExperiment)
        .setResultsFileWithoutSuffix(mOutputDir + "video/" + resultsFile);
    if (D)
      Log.d(TAG, "setResultsFileWithoutSuffix sub experiment:" + resultsFile);

  }

  @JavascriptInterface
  public String fetchSubExperimentsArrayJS() {
    return this.getApp().getSubExperimentTitles().toString();
  }

  @JavascriptInterface
  public String fetchParticipantCodesJS() {
    return "[the,codes]";
  }

  @JavascriptInterface
  public String fetchExperimentTitleJS() {
    return this.getApp().getExperiment().getTitle();
  }

  @JavascriptInterface
  public void setAutoAdvanceJS(String autoadvance) {
    if (autoadvance.equals("1")) {
      ((HTML5GameActivity) getUIParent()).setAutoAdvance(true);
    } else {
      ((HTML5GameActivity) getUIParent()).setAutoAdvance(false);
    }

  }

}