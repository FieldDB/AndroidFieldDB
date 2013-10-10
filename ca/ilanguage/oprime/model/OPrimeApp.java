package ca.ilanguage.oprime.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;

public abstract class OPrimeApp extends Application {
  public boolean                  D                  = false;
  /* This is a pointer to the experiment currently underway */
  protected int                   mCurrentExperiment;

  /* This is a pointer to the sub experiment currently underway */
  protected int                   mCurrentSubExperiment;

  /*
   * Customer support
   */
  protected String[]              mDevEmailAddresses = new String[] { "opensource@ilanguage.ca" };

  /*
   * This is an array of experiments that are in memory. it is possible to have
   * an unlimited number of experiments in memory, for example if the first
   * experiment is in English, the second can be in French etc... In many cases
   * there might be only one experiment in the experiments way
   */
  protected ArrayList<Experiment> mExperiments;
  /*
   * Localization of multi-lingual experiments/stimuli/UI
   */
  protected Locale                mLanguage;
  protected String                mLocalCouchDBname  = "oprimesample";

  /*
   * Variables for TouchDB - CouchDB storage if used by the application.
   */
  protected String                mLocalCouchDir     = Config.DEFAULT_OUTPUT_DIRECTORY + "db/couchdb/";

  /*
   * Experiment variables
   */

  // protected boolean mAutoAdvanceStimuliOnTouch = false;
  protected String                mOutputDir         = Config.DEFAULT_OUTPUT_DIRECTORY;
  protected String                mRemoteCouchUrl    = "https://oprimesampleadmin:none@cesine.iriscouch.com/oprimesample";
  public String                   TAG                = Config.TAG;

  protected void addStimuli() {

    /*
     * Sample SubExperiment: Spontaneous speech is a timer task of 5 minutes
     */
    ArrayList<Stimulus> stimuli = new ArrayList<Stimulus>();
    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "5 minutes"));
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(0).setStimuli(stimuli);
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(0)
        .setIntentToCallThisSubExperiment(Config.INTENT_START_STOP_WATCH_SUB_EXPERIMENT);

    stimuli = null;

    /*
     * Sample SubExperiment: 1 image per item
     */
    stimuli = new ArrayList<Stimulus>();

    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "Begin"));
    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "Practice Item 2"));
    stimuli.add(new Stimulus(R.drawable.androids_experimenter_kids, "Item 1"));
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(1).setStimuli(stimuli);
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(1)
        .setIntentToCallThisSubExperiment(Config.INTENT_START_SUB_EXPERIMENT);
    /*
     * Sample SubExperiment: 2 images per item.
     */
    ArrayList<TwoImageStimulus> twostimuli = new ArrayList<TwoImageStimulus>();

    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids, R.drawable.androids_experimenter_kids,
        "Begin"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids, R.drawable.androids_experimenter_kids,
        "Practice Item 1"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids, R.drawable.androids_experimenter_kids,
        "Practice Item 2"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids, R.drawable.androids_experimenter_kids,
        "Item 1"));
    twostimuli.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids, R.drawable.androids_experimenter_kids,
        "Item 2"));
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(2).setStimuli(stimuli);
    this.mExperiments.get(this.mCurrentExperiment).getSubExperiments().get(1)
        .setIntentToCallThisSubExperiment(Config.INTENT_START_TWO_IMAGE_SUB_EXPERIMENT);
  }

  public void createNewExperiment(String languagecode, boolean autoAdvanceStimuliOnTouch) {
    this.forceLocale(languagecode);

    // getString(R.string.experiment_title)
    Experiment expLocalized = new Experiment("Bilingual Aphasia Test" + " - " + this.mLanguage.getDisplayLanguage());

    this.mExperiments.add(expLocalized);
    this.mCurrentExperiment = this.mExperiments.size() - 1;

    this.mExperiments.get(this.mCurrentExperiment).setSubExperiments(new ArrayList<SubExperimentBlock>());
    String[] subextitles = this.getResources().getStringArray(R.array.subexperiment_titles);
    for (String subextitle : subextitles) {
      this.mExperiments
          .get(this.mCurrentExperiment)
          .getSubExperiments()
          .add(
              new SubExperimentBlock(subextitle, languagecode, subextitle, null, Config.EMPTYSTRING,
                  Config.INTENT_START_SUB_EXPERIMENT, true, autoAdvanceStimuliOnTouch));
    }
    this.addStimuli();
    this.mCurrentSubExperiment = 0;
    if (this.D)
      Log.d(this.TAG, "Created an experiment " + this.mExperiments.get(this.mCurrentExperiment).getTitle());

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
    Configuration config = this.getBaseContext().getResources().getConfiguration();
    Locale locale;
    if (lang.contains("-")) {
      String[] langCountrycode = lang.split("-");
      locale = new Locale(langCountrycode[0], langCountrycode[1]);
    } else {
      locale = new Locale(lang);
    }
    Locale.setDefault(locale);
    config.locale = locale;
    this.getBaseContext().getResources()
        .updateConfiguration(config, this.getBaseContext().getResources().getDisplayMetrics());
    this.mLanguage = Locale.getDefault();

    return Locale.getDefault().getDisplayLanguage();
  }

  public int getCurrentSubExperiment() {
    return this.mCurrentSubExperiment;
  }

  public Experiment getExperiment() {
    if (this.mExperiments.size() > 0) {
      return this.mExperiments.get(this.mCurrentExperiment);
    } else {
      Log.e(this.TAG, "There are no experiments... this is probably a bug.");
      return null;
    }
  }

  public Locale getLanguage() {
    return this.mLanguage;
  }

  public String getLocalCouchDBname() {
    return this.mLocalCouchDBname;
  }

  public String getLocalCouchDir() {
    return this.mLocalCouchDir;
  }

  public String getOutputDir() {
    return this.mOutputDir;
  }

  public String getRemoteCouchUrl() {
    return this.mRemoteCouchUrl;
  }

  public ArrayList<SubExperimentBlock> getSubExperiments() {
    return this.mExperiments.get(this.mCurrentExperiment).getSubExperiments();
  }

  public ArrayList<String> getSubExperimentTitles() {
    ArrayList<String> titles = new ArrayList<String>();
    for (SubExperimentBlock subexperiment : this.mExperiments.get(this.mCurrentExperiment).getSubExperiments()) {
      titles.add(subexperiment.getTitle());
    }
    return titles;
  }

  public String getTag() {
    return this.TAG;
  }

  public boolean isD() {
    return this.D;
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
    for (SubExperimentBlock s : this.getExperiment().getSubExperiments()) {
      if (s.isExperimentProbablyComplete()) {
        completed++;
      }
    }
    if (completed == this.getExperiment().getSubExperiments().size()) {
      if (!this.getExperiment().getParticipant().getLanguageCodes().contains(this.mLanguage.getLanguage())) {
        this.getExperiment().getParticipant().getLanguageCodes().add(this.mLanguage.getLanguage());
        this.getExperiment().getParticipant().getLanguages().add(this.mLanguage.getDisplayLanguage());
      }
      return true;
    } else {
      return false;
    }

  }

  /**
   * When you override this method, consider calling your setup of directories
   * before the super.onCreate() to be sure that your directories are used, not
   * the default.
   */
  @Override
  public void onCreate() {
    super.onCreate();

    /*
     * To set output dir to be in the root area of sdcard, by defualt experiment
     * results are in the public area of sdcard so researcher can copy them off
     * the tablet
     */
    // mOutputDir = "file:///sdcard"+this.getFilesDir().getAbsolutePath() +
    // File.separator;

    this.mLanguage = Locale.getDefault();
    // new File(mOutputDir).mkdirs();
    new File(this.mOutputDir + "video/").mkdirs();
    // new File(mOutputDir + "audio/").mkdirs();
    new File(this.mOutputDir + "images/").mkdirs();
    new File(this.mOutputDir + "touchdata/").mkdirs();

    if (this.mExperiments == null) {
      this.mExperiments = new ArrayList<Experiment>();
    }
    if (this.D)
      Log.d(this.TAG, "Oncreate of the OPrimeApp ");
  }

  @Override
  public void onLowMemory() {
    if (this.D)
      Log.d(this.TAG, "The application is facing low memory, closing...");
    super.onLowMemory();
  }

  @Override
  public void onTerminate() {
    if (this.D)
      Log.d(this.TAG, "The application has been terminated, closing...");
    super.onTerminate();
  }

  public void sendDevsAnEmail(String subject, String message) {
    if (subject == null) {
      subject = "Email from " + this.TAG;
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
      Toast.makeText(this, "There are no email clients installed, cannot send an email.", Toast.LENGTH_LONG).show();
    }
  }

  public void setCurrentSubExperiment(int currentSubExperiment) {
    this.mCurrentSubExperiment = currentSubExperiment;
  }

  public void setExperiment(int mCurrentExperiment) {
    this.mCurrentExperiment = mCurrentExperiment;
  }

  public void setLanguage(Locale mLanguage) {
    this.mLanguage = mLanguage;
  }

  // public boolean isAutoAdvanceStimuliOnTouch() {
  // return mAutoAdvanceStimuliOnTouch;
  // }

  // public void setAutoAdvanceStimuliOnTouch(boolean
  // mAutoAdvanceStimuliOnTouch) {
  // this.mAutoAdvanceStimuliOnTouch = mAutoAdvanceStimuliOnTouch;
  // }

  public void setLocalCouchDBname(String mLocalCouchDBname) {
    this.mLocalCouchDBname = mLocalCouchDBname;
  }

  public void setLocalCouchDir(String mLocalCouchDir) {
    this.mLocalCouchDir = mLocalCouchDir;
  }

  public void setOutputDir(String mOutputDir) {
    this.mOutputDir = mOutputDir;
  }

  public void setRemoteCouchUrl(String mRemoteCouchUrl) {
    this.mRemoteCouchUrl = mRemoteCouchUrl;
  }

  public void setSubExperiments(ArrayList<SubExperimentBlock> subExperiments) {
    this.mExperiments.get(this.mCurrentExperiment).setSubExperiments(subExperiments);
  }

  public void writePrivateParticipantToFile() {
    String outfile = this.mOutputDir + "participants_warning_CONFIDENTIAL_do_not_distribute.csv";
    try {
      FileOutputStream out = new FileOutputStream(outfile, true);
      out.write(("\n" + this.getExperiment().getParticipant().toCSVPrivateString()).getBytes());
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      Log.e(this.TAG, "writePrivateParticipantToFile Problem opening outfile.");

    } catch (IOException e) {
      Log.e(this.TAG, "writePrivateParticipantToFile Problem writing outfile.");
    }
  }

}
