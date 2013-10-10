package ca.ilanguage.oprime.ui;

import java.io.File;
import java.util.Locale;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.SubExperimentToJson;
import ca.ilanguage.oprime.javascript.ExperimentJavaScriptInterface;
import ca.ilanguage.oprime.javascript.JavaScriptInterface;
import ca.ilanguage.oprime.model.OPrimeApp;
import ca.ilanguage.oprime.model.Participant;
import ca.ilanguage.oprime.model.SubExperimentBlock;

public class HTML5GameActivity extends HTML5Activity {

  private OPrimeApp           app;
  protected Boolean           mAutoAdvance  = false;
  protected int               mCurrentSubex = 0;
  private JavaScriptInterface mJavaScriptInterface;

  protected void checkIfNeedToPrepareExperiment(boolean activtySaysToPrepareExperiment) {
    boolean prepareExperiment = this.getIntent().getExtras().getBoolean(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, false);
    if (prepareExperiment || activtySaysToPrepareExperiment) {
      if (this.D) {
        Log.d(this.TAG, "HTML5GameActivity was asked to prepare the experiment.");
      }
      SharedPreferences prefs = this.getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
      String lang = prefs.getString(Config.PREFERENCE_EXPERIMENT_LANGUAGE, "");
      boolean autoAdvanceStimuliOnTouch = prefs.getBoolean(Config.PREFERENCE_EXPERIMENT_AUTO_ADVANCE_ON_TOUCH, false);
      // ((OPrimeApp) this.getApplication())
      // .setAutoAdvanceStimuliOnTouch(autoAdvanceStimuliOnTouch);

      if (this.app.getLanguage().getLanguage().equals(lang) && this.app.getExperiment() != null) {
        // do nothing if they didn't change the language
        if (this.D) {
          Log.d(this.TAG, "The Language has not changed, not preparing the experiment for " + lang);
        }
      } else {
        if (lang == null) {
          lang = this.app.getLanguage().getLanguage();
          if (this.D) {
            Log.d(this.TAG, "The Language was null, setting it to the tablets default language " + lang);
          }
        }
        if (this.D) {
          Log.d(this.TAG, "Preparing the experiment for " + lang);
        }
        this.app.createNewExperiment(lang, autoAdvanceStimuliOnTouch);
        this.initExperiment();
      }
    }
  }

  @Override
  public OPrimeApp getApp() {
    return this.app;
  }

  public Boolean getAutoAdvance() {
    return this.mAutoAdvance;
  }

  public int getCurrentSubex() {
    return this.mCurrentSubex;
  }

  @Override
  public JavaScriptInterface getJavaScriptInterface() {
    return this.mJavaScriptInterface;
  }

  protected void getParticipantDetails(OPrimeApp app) {
    Participant p;
    try {
      p = app.getExperiment().getParticipant();
    } catch (Exception e) {
      p = new Participant();
    }
    SharedPreferences prefs = this.getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
    String firstname = prefs.getString(Config.PREFERENCE_PARTICIPANT_FIRSTNAME, "");
    String lastname = prefs.getString(Config.PREFERENCE_PARTICIPANT_LASTNAME, "");
    String experimenter = prefs.getString(Config.PREFERENCE_EXPERIEMENTER_CODE, "NN");
    String details = prefs.getString(Config.PREFERENCE_PARTICIPANT_DETAILS, "");
    String gender = prefs.getString(Config.PREFERENCE_PARTICIPANT_GENDER, "");
    String birthdate = prefs.getString(Config.PREFERENCE_PARTICIPANT_BIRTHDATE, "");
    String lang = prefs.getString(Config.PREFERENCE_EXPERIMENT_LANGUAGE, "en");
    boolean autoAdvanceStimuliOnTouch = prefs.getBoolean(Config.PREFERENCE_EXPERIMENT_AUTO_ADVANCE_ON_TOUCH, false);
    // ((OPrimeApp) this.getApplication())
    // .setAutoAdvanceStimuliOnTouch(autoAdvanceStimuliOnTouch);
    // String langs =
    // prefs.getString(OPrimeApp.PREFERENCE_PARTICIPANT_LANGUAGES,
    // "");
    String testDayNumber = prefs.getString(Config.PREFERENCE_TESTING_DAY_NUMBER, "1");
    String participantNumberOnDay = prefs.getString(Config.PREFERENCE_PARTICIPANT_NUMBER_IN_DAY, "1");
    /*
     * Build the participant ID and save the start time to the preferences.
     */
    p.setCode(testDayNumber + experimenter + participantNumberOnDay + firstname.substring(0, 1).toUpperCase()
        + lastname.substring(0, 1).toUpperCase());
    p.setFirstname(firstname);
    p.setLastname(lastname);
    p.setExperimenterCode(experimenter);
    p.setGender(gender);
    p.setBirthdate(birthdate);
    p.setDetails(details);

    if (app.getExperiment() == null) {
      app.createNewExperiment(lang, autoAdvanceStimuliOnTouch);
    }
    app.getExperiment().setParticipant(p);
    // Toast.makeText(getApplicationContext(), p.toCSVPrivateString(),
    // Toast.LENGTH_LONG).show();

  }

  protected void initExperiment() {
    this.getParticipantDetails(this.app);
    this.mWebView.loadUrl("file:///android_asset/index.html");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (this.app == null) {
      this.app = this.getApp();
    }
    switch (requestCode) {
    case Config.EXPERIMENT_COMPLETED:
      if (data != null) {
        SubExperimentBlock completedExp = (SubExperimentBlock) data.getExtras().getSerializable(
            Config.EXTRA_SUB_EXPERIMENT);
        this.app.getSubExperiments().set(this.mCurrentSubex, completedExp);

        Intent i = new Intent(this, SubExperimentToJson.class);
        i.putExtra(Config.EXTRA_SUB_EXPERIMENT, this.app.getSubExperiments().get(this.mCurrentSubex));
        this.startService(i);
        this.app
            .getExperiment()
            .getParticipant()
            .setStatus(
                this.app.getExperiment().getParticipant().getStatus() + ":::" + completedExp.getTitle() + " in "
                    + (new Locale(completedExp.getLanguage())).getDisplayLanguage() + " --- "
                    + completedExp.getDisplayedStimuli() + "/" + completedExp.getStimuli().size() + " Completed ");
        this.trackCompletedExperiment(completedExp);

        this.app.writePrivateParticipantToFile();

        String intentAfterSubExperiment = this.app.getExperiment().getSubExperiments().get(this.mCurrentSubex)
            .getIntentToCallAfterThisSubExperiment();
        if (!"".equals(intentAfterSubExperiment)) {
          Intent takepicture = new Intent(intentAfterSubExperiment);
          takepicture.putExtra(Config.EXTRA_RESULT_FILENAME,
              completedExp.getResultsFileWithoutSuffix().replace("video", "images") + ".jpg");
          this.startActivity(takepicture);
        }
      }
      this.stopVideoRecorder();
      if (this.mAutoAdvance) {
        this.mCurrentSubex++;
        if (this.mCurrentSubex >= this.app.getExperiment().getSubExperiments().size()) {
          Toast.makeText(this.getApplicationContext(), "Experiment completed!", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(this, "Sub-experiment complete. ", Toast.LENGTH_LONG).show();
          this.mWebView.loadUrl("javascript:getPositionAsButton(0,0," + this.mCurrentSubex + ")");
        }
      }
      break;
    case Config.PREPARE_TRIAL:
      // initExperiment();
      this.checkIfNeedToPrepareExperiment(true);
      break;
    case Config.SWITCH_LANGUAGE:
      this.checkIfNeedToPrepareExperiment(true);
      // SharedPreferences prefs =
      // getSharedPreferences(OPrimeApp.PREFERENCE_NAME,
      // MODE_PRIVATE);
      // String lang = prefs.getString(OPrimeApp.PREFERENCE_EXPERIMENT_LANGUAGE,
      // "en");
      // boolean autoAdvanceStimuliOnTouch = prefs.getBoolean(
      // OPrimeApp.PREFERENCE_EXPERIMENT_AUTO_ADVANCE_ON_TOUCH, false);
      // // ((OPrimeApp) this.getApplication())
      // // .setAutoAdvanceStimuliOnTouch(autoAdvanceStimuliOnTouch);
      //
      // if (lang.equals(app.getLanguage().getLanguage())) {
      // // do nothing if they didn't change the language
      // } else {
      // app.createNewExperiment(lang, autoAdvanceStimuliOnTouch);
      // initExperiment();
      // }
      break;
    case Config.REPLAY_RESULTS:
      break;
    default:
      break;
    }
    super.onActivityResult(requestCode, resultCode, data);

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.setApp(this.getApplication());
    super.onCreate(savedInstanceState);

    String outputDir = this.mOutputDir + "video/";
    new File(outputDir).mkdirs();

  }

  @Override
  protected void prepareWebView() {
    super.prepareWebView();
    this.checkIfNeedToPrepareExperiment(false);
  }

  public void setApp(Application app) {
    this.app = (OPrimeApp) app;
  }

  public void setAutoAdvance(Boolean mAutoAdvance) {
    this.mAutoAdvance = mAutoAdvance;
  }

  public void setCurrentSubex(int mCurrentSubex) {
    this.mCurrentSubex = mCurrentSubex;
  }

  @Override
  public void setJavaScriptInterface(JavaScriptInterface javaScriptInterface) {
    this.mJavaScriptInterface = javaScriptInterface;
  }

  @Override
  protected void setUpVariables() {
    this.D = this.getApp().isD();
    this.mOutputDir = this.getApp().getOutputDir();
    this.mInitialAppServerUrl = "file:///android_asset/index.html";// "http://192.168.0.180:3001/";
    this.setJavaScriptInterface(new ExperimentJavaScriptInterface(this.D, this.TAG, this.mOutputDir, this
        .getApplicationContext(), this, ""));
    if (this.D)
      Log.d(this.TAG, "Using the OPrime experiment javascript interface.");

    // this.app = (OPrimeApp) getApplication();
  }

  protected void stopVideoRecorder() {
    Intent i = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
    this.sendBroadcast(i);
    Intent audio = new Intent(this, AudioRecorder.class);
    this.stopService(audio);
  }

  protected void trackCompletedExperiment(SubExperimentBlock completedExp) {
    // Place holder to be overriden by experiments if needed
  }

}