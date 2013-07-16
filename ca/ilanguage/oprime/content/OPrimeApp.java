package ca.ilanguage.oprime.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import ca.ilanguage.oprime.R;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

public abstract class OPrimeApp extends Application {
  public String TAG = "OPrime";
  public boolean D = false;
  public static final String DEFAULT_OUTPUT_DIRECTORY = "/sdcard/OPrime/";
  public static final String SHARED_OUTPUT_DIR = "/sdcard/OPrime";

  // protected boolean mAutoAdvanceStimuliOnTouch = false;
  protected String mOutputDir = DEFAULT_OUTPUT_DIRECTORY;


  /*
   * Customer support
   */
  protected String[] mDevEmailAddresses = new String[] { "opensource@ilanguage.ca" };

  /*
   * Variables for TouchDB - CouchDB storage if used by the application.
   */
  protected String mLocalCouchDir = DEFAULT_OUTPUT_DIRECTORY + "db/couchdb/";
  protected String mLocalCouchDBname = "oprimesample";
  protected String mRemoteCouchUrl = "https://oprimesampleadmin:none@cesine.iriscouch.com/oprimesample";

  /*
   * Localization of multi-lingual experiments/stimuli/UI
   */
  protected Locale mLanguage;

  /*
   * Experiment variables
   */

  /*
   * This is an array of experiments that are in memory. it is possible to have
   * an unlimited number of experiments in memory, for example if the first
   * experiment is in English, the second can be in French etc... In many cases
   * there might be only one experiment in the experiments way
   */
  protected ArrayList<Experiment> mExperiments;
  /* This is a pointer to the sub experiment currently underway */
  protected int mCurrentSubExperiment;
  /* This is a pointer to the experiment currently underway */
  protected int mCurrentExperiment;

  /*
   * Settings variables
   */
  public static final String PREFERENCE_NAME = "OPrimePrefs";

  public static final String PREFERENCE_PARTICIPANT_ID = "participantId";
  public static final String PREFERENCE_PARTICIPANT_FIRSTNAME = "participantfirstname";
  public static final String PREFERENCE_PARTICIPANT_LASTNAME = "participantlastname";
  public static final String PREFERENCE_PARTICIPANT_GENDER = "participantgender";
  public static final String PREFERENCE_PARTICIPANT_BIRTHDATE = "participantbirthdate";
  public static final String PREFERENCE_PARTICIPANT_DETAILS = "participantdetails";
  public static final String PREFERENCE_PARTICIPANT_STARTTIME = "participantstarttime";
  public static final String PREFERENCE_PARTICIPANT_ENDTIME = "participantendtime";
  public static final String PREFERENCE_EXPERIEMENTER_CODE = "experimenterCode";
  public static final String PREFERENCE_EXPERIMENT_LANGUAGE = "experimentlanguage";
  public static final String PREFERENCE_EXPERIMENT_AUTO_ADVANCE_ON_TOUCH = "autoAdvanceStimuliOnTouch";
  public static final String PREFERENCE_PARTICIPANT_LANGUAGES = "participantlangs";
  public static final String PREFERENCE_TESTING_DAY_NUMBER = "testingdaynumber";
  public static final String PREFERENCE_PARTICIPANT_NUMBER_IN_DAY = "participantnumberinday";

  public static final String PREFERENCE_REPLAY_RESULTS_MODE = "replayresults";
  public static final String PREFERENCE_REPLAY_PARTICIPANT_CODE = "replayparticipantcode";

  /**
   * When you override this method, consider calling your setup of directories
   * before the super.onCreate() to be sure that your directories are used, not
   * the default.
   */
  @Override
  public void onCreate() {
    super.onCreate();

    /* To set output dir to be in the root area of sdcard, by defualt experiment results are in the public area of sdcard so researcher can copy them off the tablet */
    // mOutputDir = "file:///sdcard"+this.getFilesDir().getAbsolutePath() +
    // File.separator;

    mLanguage = Locale.getDefault();
    // new File(mOutputDir).mkdirs();
    new File(mOutputDir + "video/").mkdirs();
//    new File(mOutputDir + "audio/").mkdirs();
    new File(mOutputDir + "images/").mkdirs();
    new File(mOutputDir + "touchdata/").mkdirs();

    if (mExperiments == null) {
      mExperiments = new ArrayList<Experiment>();
    }
    if (D)
      Log.d(TAG, "Oncreate of the OPrimeApp ");
  }

  @Override
  public void onLowMemory() {
    if (D)
      Log.d(TAG, "The application is facing low memory, closing...");
    super.onLowMemory();
  }

  @Override
  public void onTerminate() {
    if (D)
      Log.d(TAG, "The application has been terminated, closing...");
    super.onTerminate();
  }

  public ArrayList<String> getSubExperimentTitles() {
    ArrayList<String> titles = new ArrayList<String>();
    for (SubExperimentBlock subexperiment : mExperiments
        .get(mCurrentExperiment).getSubExperiments()) {
      titles.add(subexperiment.getTitle());
    }
    return titles;
  }

  public void createNewExperiment(String languagecode,
      boolean autoAdvanceStimuliOnTouch) {
    forceLocale(languagecode);

    // getString(R.string.experiment_title)
    Experiment expLocalized = new Experiment("Bilingual Aphasia Test" + " - "
        + mLanguage.getDisplayLanguage());

    mExperiments.add(expLocalized);
    mCurrentExperiment = mExperiments.size() - 1;

    mExperiments.get(mCurrentExperiment).setSubExperiments(
        new ArrayList<SubExperimentBlock>());
    String[] subextitles = getResources().getStringArray(
        R.array.subexperiment_titles);
    for (int i = 0; i < subextitles.length; i++) {
      mExperiments
          .get(mCurrentExperiment)
          .getSubExperiments()
          .add(
              new SubExperimentBlock(subextitles[i], languagecode,
                  subextitles[i], null, OPrime.EMPTYSTRING,
                  OPrime.INTENT_START_SUB_EXPERIMENT, true,
                  autoAdvanceStimuliOnTouch));
    }
    addStimuli();
    mCurrentSubExperiment = 0;
    if (D)
      Log.d(TAG, "Created an experiment "
          + mExperiments.get(mCurrentExperiment).getTitle());

  }

  protected void addStimuli() {

    /*
     * Sample SubExperiment: Spontaneous speech is a timer task of 5 minutes
     */
    ArrayList<Stimulus> stimuli = new ArrayList<Stimulus>();
    stimuli
        .add(new Stimulus(R.drawable.androids_experimenter_kids, "5 minutes"));
    mExperiments.get(mCurrentExperiment).getSubExperiments().get(0)
        .setStimuli(stimuli);
    mExperiments
        .get(mCurrentExperiment)
        .getSubExperiments()
        .get(0)
        .setIntentToCallThisSubExperiment(
            OPrime.INTENT_START_STOP_WATCH_SUB_EXPERIMENT);

    stimuli = null;

    /*
     * Sample SubExperiment: 1 image per item
     */
    stimuli = new ArrayList<Stimulus>();

    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "Begin"));
    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids,
        "Practice Item 2"));
    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "Item 1"));
    mExperiments.get(mCurrentExperiment).getSubExperiments().get(1)
        .setStimuli(stimuli);
    mExperiments.get(mCurrentExperiment).getSubExperiments().get(1)
        .setIntentToCallThisSubExperiment(OPrime.INTENT_START_SUB_EXPERIMENT);
    /*
     * Sample SubExperiment: 2 images per item.
     */
    ArrayList<TwoImageStimulus> twostimuli = new ArrayList<TwoImageStimulus>();

    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids,
        R.drawable.androids_experimenter_kids, "Begin"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids,
        R.drawable.androids_experimenter_kids, "Practice Item 1"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids,
        R.drawable.androids_experimenter_kids, "Practice Item 2"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids,
        R.drawable.androids_experimenter_kids, "Item 1"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids,
        R.drawable.androids_experimenter_kids, "Item 2"));
    mExperiments.get(mCurrentExperiment).getSubExperiments().get(2)
        .setStimuli(stimuli);
    mExperiments
        .get(mCurrentExperiment)
        .getSubExperiments()
        .get(1)
        .setIntentToCallThisSubExperiment(
            OPrime.INTENT_START_TWO_IMAGE_SUB_EXPERIMENT);
  }

  /**
   * Runs through the sub experiments, checks to see that each has data, and
   * then determined if the experiment was complete. It is a flexible notion as
   * we expect users to complete an experiment in one or two sittings where the
   * android app has closed and reopened.
   * 
   * @return
   */
  public boolean isExperimentCompleted() {
    int completed = 0;
    for (SubExperimentBlock s : getExperiment().getSubExperiments()) {
      if (s.isExperimentProbablyComplete()) {
        completed++;
      }
    }
    if (completed == getExperiment().getSubExperiments().size()) {
      if (!getExperiment().getParticipant().getLanguageCodes()
          .contains(mLanguage.getLanguage())) {
        getExperiment().getParticipant().getLanguageCodes()
            .add(mLanguage.getLanguage());
        getExperiment().getParticipant().getLanguages()
            .add(mLanguage.getDisplayLanguage());
      }
      return true;
    } else {
      return false;
    }

  }

  public void writePrivateParticipantToFile() {
    String outfile = mOutputDir
        + "participants_warning_CONFIDENTIAL_do_not_distribute.csv";
    try {
      FileOutputStream out = new FileOutputStream(outfile, true);
      out.write(("\n" + getExperiment().getParticipant().toCSVPrivateString())
          .getBytes());
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      Log.e(TAG, "writePrivateParticipantToFile Problem opening outfile.");

    } catch (IOException e) {
      Log.e(TAG, "writePrivateParticipantToFile Problem writing outfile.");
    }
  }

  public Experiment getExperiment() {
    if (mExperiments.size() > 0) {
      return mExperiments.get(mCurrentExperiment);
    } else {
      Log.e(TAG, "There are no experiments... this is probably a bug.");
      return null;
    }
  }

  public void setExperiment(int mCurrentExperiment) {
    this.mCurrentExperiment = mCurrentExperiment;
  }

  public ArrayList<SubExperimentBlock> getSubExperiments() {
    return mExperiments.get(mCurrentExperiment).getSubExperiments();
  }

  public void setSubExperiments(ArrayList<SubExperimentBlock> subExperiments) {
    mExperiments.get(mCurrentExperiment).setSubExperiments(subExperiments);
  }

  public int getCurrentSubExperiment() {
    return mCurrentSubExperiment;
  }

  public void setCurrentSubExperiment(int currentSubExperiment) {
    this.mCurrentSubExperiment = currentSubExperiment;
  }

  /**
   * Forces the locale for the duration of the app to the language needed for
   * that version of the Experiment. It accepts a variable in the form en or
   * en-US containing just the language code, or the language code followed by a
   * - and the co
   * 
   * @param lang
   * @return
   */
  public String forceLocale(String lang) {
    if (lang.equals(Locale.getDefault().getLanguage())) {
      return Locale.getDefault().getDisplayLanguage();
    }
    Configuration config = getBaseContext().getResources().getConfiguration();
    Locale locale;
    if (lang.contains("-")) {
      String[] langCountrycode = lang.split("-");
      locale = new Locale(langCountrycode[0], langCountrycode[1]);
    } else {
      locale = new Locale(lang);
    }
    Locale.setDefault(locale);
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
        getBaseContext().getResources().getDisplayMetrics());
    mLanguage = Locale.getDefault();

    return Locale.getDefault().getDisplayLanguage();
  }

  public void sendDevsAnEmail(String subject, String message) {
    if (subject == null) {
      subject = "Email from " + TAG;
    }
    String[] recipients = this.mDevEmailAddresses;
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("message/rfc822");
    i.putExtra(Intent.EXTRA_EMAIL, recipients);
    i.putExtra(Intent.EXTRA_SUBJECT, subject);
    i.putExtra(Intent.EXTRA_TEXT, message);
    try {
      this.startActivity(Intent.createChooser(i, "Send mail..."));
    } catch (android.content.ActivityNotFoundException ex) {
      Toast.makeText(this,
          "There are no email clients installed, cannot send an email.",
          Toast.LENGTH_LONG).show();
    }
  }

  public boolean isD() {
    return D;
  }

  public String getTag() {
    return TAG;
  }

  public Locale getLanguage() {
    return mLanguage;
  }

  public void setLanguage(Locale mLanguage) {
    this.mLanguage = mLanguage;
  }

  public String getOutputDir() {
    return mOutputDir;
  }

  public void setOutputDir(String mOutputDir) {
    this.mOutputDir = mOutputDir;
  }

  // public boolean isAutoAdvanceStimuliOnTouch() {
  // return mAutoAdvanceStimuliOnTouch;
  // }

  // public void setAutoAdvanceStimuliOnTouch(boolean
  // mAutoAdvanceStimuliOnTouch) {
  // this.mAutoAdvanceStimuliOnTouch = mAutoAdvanceStimuliOnTouch;
  // }

  public String getLocalCouchDir() {
    return mLocalCouchDir;
  }

  public void setLocalCouchDir(String mLocalCouchDir) {
    this.mLocalCouchDir = mLocalCouchDir;
  }

  public String getLocalCouchDBname() {
    return mLocalCouchDBname;
  }

  public void setLocalCouchDBname(String mLocalCouchDBname) {
    this.mLocalCouchDBname = mLocalCouchDBname;
  }

  public String getRemoteCouchUrl() {
    return mRemoteCouchUrl;
  }

  public void setRemoteCouchUrl(String mRemoteCouchUrl) {
    this.mRemoteCouchUrl = mRemoteCouchUrl;
  }

}
