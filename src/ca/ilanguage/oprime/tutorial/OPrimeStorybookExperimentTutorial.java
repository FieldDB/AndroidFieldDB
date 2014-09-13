package ca.ilanguage.oprime.tutorial;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.storybook.StoryBookSubExperiment;
import ca.ilanguage.oprime.tutorial.R;
import ca.ilanguage.oprime.model.Stimulus;
import ca.ilanguage.oprime.preferences.PreferenceConstants;
import ca.ilanguage.oprime.preferences.SetPreferencesActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class OPrimeStorybookExperimentTutorial extends Activity {
  private String mParticipantId = Config.DEFAULT_PARTICIPANT_ID;
  private String mExperimentTrialHeader = "";
  private Handler mHandlerDelayStimuli = new Handler();
  private Boolean mReplayMode = false;
  private Boolean mReplayBySubExperiments = false;
  private String mCurrentSubExperimentLanguage = "en";
  public long mExperimentLaunch;
  public long mExperimentQuit;
  public static final String OUTPUT_DIRECTORY = "/sdcard/OPrime/video/";

  public ArrayList<String> mSubExperimentParticipantVideos = new ArrayList<String>();
  public ArrayList<String> mParticipantsCodesCompleted = new ArrayList<String>();

  private ImageView mImage;
  private Menu mMenu;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.fragment_video_recorder);

    // mImage = (ImageView) findViewById(R.id.mainimage);
    // mImage.setImageResource(R.drawable.androids_experimenter_kids);
    // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

    if (savedInstanceState == null) {
      Intent setupIntent = new Intent(getBaseContext(),
          SetPreferencesActivity.class);
      startActivityForResult(setupIntent, Config.CODE_PREPARE_TRIAL);

    }

  }

  public boolean onCreateOptionsMenu(Menu menu) {
    // Hold on to this
    mMenu = menu;

    // Inflate the currently selected menu XML resource.
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.home_menu, menu);

    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    // For "Title only": Examples of matching an ID with one assigned in
    // the XML
    case R.id.open_settings:

      Intent i = new Intent(getBaseContext(), SetPreferencesActivity.class);
      startActivity(i);
      return true;

    case R.id.result_folder:
      final boolean fileManagerAvailable = isIntentAvailable(this,
          "org.openintents.action.PICK_FILE");
      if (!fileManagerAvailable) {
        Toast
            .makeText(
                getApplicationContext(),
                "To open and export recorded files or "
                    + "draft data you can install the OI File Manager, "
                    + "it allows you to browse your SDCARD directly on your mobile device.",
                Toast.LENGTH_LONG).show();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
            .parse("market://details?id=org.openintents.filemanager"));
        startActivity(goToMarket);

      } else {
        Intent openResults = new Intent("org.openintents.action.PICK_FILE");
        openResults.setData(Uri.parse("file://" + Config.EXTRA_OUTPUT_DIR));
        startActivity(openResults);
      }
      // Intent intentReplay = new Intent(getBaseContext(),
      // SetPreferencesActivity.class);
      // startActivityForResult(intentReplay, Config.REPLAY_RESULTS);
      return true;
    case R.id.backup_results:
      Intent backupIntent = new Intent(getBaseContext(),
          SetPreferencesActivity.class);
      startActivity(backupIntent);
      return true;
    case R.id.issue_tracker:

      Intent browserIntent = new Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://github.com/BilingualAphasia/AndroidBilingualAphasiaTest/issues/"));
      startActivity(browserIntent);
      return true;
    default:
      // Do nothing

      break;
    }

    return false;
  }

  private void initExperiment() {
    SharedPreferences prefs = getSharedPreferences(
        PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
    String firstname = prefs.getString(
        PreferenceConstants.PREFERENCE_PARTICIPANT_FIRSTNAME, "none");
    String lastname = prefs.getString(
        PreferenceConstants.PREFERENCE_PARTICIPANT_LASTNAME, "nobody");
    String experimenter = prefs.getString(
        PreferenceConstants.PREFERENCE_EXPERIEMENTER_CODE, "AA");
    String testDayNumber = prefs.getString(
        PreferenceConstants.PREFERENCE_TESTING_DAY_NUMBER, "0");
    String participantNumberOnDay = prefs.getString(
        PreferenceConstants.PREFERENCE_PARTICIPANT_NUMBER_IN_DAY, "0");
    mReplayMode = prefs.getBoolean(
        PreferenceConstants.PREFERENCE_REPLAY_RESULTS_MODE, false);
    if (mReplayMode) {
      // findVideosWithSubstring(prefs.getString(PreferenceConstants.PREFERENCE_REPLAY_PARTICIPANT_CODE,
      // ""));
      mReplayBySubExperiments = true;
      findParticipantCodesWithResults();
      findVideosWithSubstring("_");
      // mWebView.loadUrl("file:///android_asset/bilingual_aphasia_test_home.html");
    } else {
      mReplayBySubExperiments = false;
    }
    String mTabletOrPaperFirst = "T";
    String participantGroup = "F"; // participants worst language is
    // English, so they get english first.
    mCurrentSubExperimentLanguage = "fr";

    /*
     * Build the participant ID and save the start time to the preferences.
     */
    mParticipantId = participantGroup + mTabletOrPaperFirst + testDayNumber
        + experimenter + participantNumberOnDay
        + firstname.substring(0, 1).toUpperCase()
        + lastname.substring(0, 1).toUpperCase();
    mExperimentLaunch = System.currentTimeMillis();
    mExperimentTrialHeader = "ParticipantID,FirstName,LastName,WorstLanguage,FirstBat,StartTime,EndTime,ExperimenterID"
        + ":::==="
        + mParticipantId
        + ","
        + firstname
        + ","
        + lastname
        + ","
        + participantGroup
        + ","
        + mTabletOrPaperFirst
        + ","
        + mExperimentLaunch + "," + mExperimentQuit;
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_ID,
        mParticipantId);
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_STARTTIME,
        mExperimentLaunch + "");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_ENDTIME,
        mExperimentLaunch + "");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_GROUP,
        participantGroup + mTabletOrPaperFirst);
    editor.commit();

  }

  public static boolean isIntentAvailable(Context context, String action) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(action);
    List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
        PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  public int findVideosWithSubstring(final String substring) {
    if (substring.length() < 1) {
      return 0;
    }
    mSubExperimentParticipantVideos = new ArrayList<String>();
    File dir = new File(Config.DEFAULT_OUTPUT_DIRECTORY);
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.contains(substring) && name.endsWith("3gp"));
      }
    };
    File[] files = dir.listFiles(filter);
    if (files == null) {
      // Either dir does not exist or is not a directory
      return 0;
    } else {
      for (int k = files.length - 1; k >= 0; k--) {
        mSubExperimentParticipantVideos.add(files[k].toString());
      }
    }

    // Toast.makeText(
    // getApplicationContext(),
    // mSubExperimentParticipantVideos.toString().replaceAll(OUTPUT_DIRECTORY,
    // ""),
    // Toast.LENGTH_LONG).show();
    return mSubExperimentParticipantVideos.size();

  }

  public String findParticipantCodesWithResults() {
    mParticipantsCodesCompleted = null;
    mParticipantsCodesCompleted = new ArrayList<String>();
    File dir = new File(Config.DEFAULT_OUTPUT_DIRECTORY);

    File[] files = dir.listFiles();
    if (files == null) {
      // Either dir does not exist or is not a directory
      return "";
    } else {
      mParticipantsCodesCompleted.add("Play All");
      for (int k = files.length - 1; k >= 0; k--) {
        String file = files[k].toString();
        String[] pieces = file.split("_");
        if (pieces != null && pieces.length > 1) {
          if (mParticipantsCodesCompleted.contains(pieces[1])) {
            // dont add it
          } else {
            mParticipantsCodesCompleted.add(pieces[1]);
          }
        }
      }
    }
    // CharSequence[] temp = mParticipantsCodesCompleted.toArray(new
    // CharSequence[mParticipantsCodesCompleted.size()]);
    // int tempsize = temp.length;

    return mParticipantsCodesCompleted.toString();

  }

  private void launchExperiment() {
    Intent intent;
    intent = new Intent(getApplicationContext(), StoryBookSubExperiment.class);

    /* TODO add extra with the stimuli */
    intent.putExtra(Config.EXTRA_STIMULI, initializeStimuli());
    intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
    startActivity(intent);
  }

  public ArrayList<Stimulus> initializeStimuli() {
    ArrayList<Stimulus> stimuli = new ArrayList<Stimulus>();
    TypedArray imgs = getResources().obtainTypedArray(R.array.image_stimuli);
    TypedArray audio = getResources().obtainTypedArray(R.array.audio_stimuli);
    for (int i = 0; i < imgs.length(); i++) {
      Stimulus s = new Stimulus(imgs.getResourceId(i, -1), audio.getResourceId(
          i, -1));
      stimuli.add(s);
    }
    return stimuli;
  }

  private void startVideoRecorder() {
    final boolean oprimeAvailable = isIntentAvailable(this,
        "ca.ilanguage.oprime.intent.action.START_VIDEO_RECORDING_SERVICE");
    if (!oprimeAvailable) {
      Toast
          .makeText(
              getApplicationContext(),
              "To record participant video you can install the "
                  + "OPrime Android Experimentation App, it allows your tablet to record video "
                  + "in the background and save it to the SDCARD.",
              Toast.LENGTH_LONG).show();
      Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
          .parse("market://details?id=ca.ilanguage.oprime"));
      startActivity(goToMarket);

    } else {
      new File(OUTPUT_DIRECTORY).mkdirs();

      Intent intent;
      intent = new Intent(
          "ca.ilanguage.oprime.intent.action.START_VIDEO_RECORDING_SERVICE");

      intent.putExtra(Config.EXTRA_USE_FRONT_FACING_CAMERA, true);
      intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
      intent.putExtra(Config.EXTRA_PARTICIPANT_ID, mParticipantId);
      intent.putExtra(Config.EXTRA_OUTPUT_DIR, OUTPUT_DIRECTORY);
      intent.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION,
          mExperimentTrialHeader);

      startActivityForResult(intent, Config.CODE_EXPERIMENT_COMPLETED);
    }
  }

  private void stopVideoRecorder() {
    Intent i = new Intent(
        "ca.ilanguage.oprime.intent.action.BROADCAST_STOP_VIDEO_SERVICE");
    sendBroadcast(i);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Config.CODE_EXPERIMENT_COMPLETED:
      break;
    case Config.CODE_PREPARE_TRIAL:
      initExperiment();
      startVideoRecorder();

      /*
       * Wait two seconds so that the video activity has time to load the
       * camera. It will continue recording until you exit the video activity.
       */
      mHandlerDelayStimuli.postDelayed(new Runnable() {
        public void run() {
          launchExperiment();
        }
      }, 2000);
      break;
    default:
      break;
    }
  }

  @Override
  public void onDestroy() {
    // this shouldn't be needed.
    mExperimentQuit = System.currentTimeMillis();
    /*
     * TODO save participant details to outfile
     */

    /*
     * Reset the participant details from the settings so that they wont be
     * saved on the next participant.
     */
    SharedPreferences prefs = getSharedPreferences(
        PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
    String participantNumber = prefs.getString(
        PreferenceConstants.PREFERENCE_PARTICIPANT_NUMBER_IN_DAY, "0");
    int participantInt = Integer.parseInt(participantNumber);
    participantInt++;
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_NUMBER_IN_DAY,
        participantInt + "");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_FIRSTNAME,
        "reset");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_LASTNAME,
        "reset");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_ID, "");
    editor
        .putString(PreferenceConstants.PREFERENCE_PARTICIPANT_STARTTIME, "00");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_ENDTIME, "00");
    editor.putString(PreferenceConstants.PREFERENCE_PARTICIPANT_GROUP, "");

    editor.commit();

    stopVideoRecorder();
    super.onDestroy();

  }

}
