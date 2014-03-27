package ca.ilanguage.oprime.ui;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.TakePicture;
import ca.ilanguage.oprime.datacollection.VideoRecorder;

/**
 * An activity representing a list of Experiments. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ExperimentActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ExperimentListFragment} and the item details (if present) is a
 * {@link ExperimentFragment}.
 * <p>
 * This activity also implements the required
 * {@link ExperimentListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ExperimentListActivity extends FragmentActivity implements ExperimentListFragment.Callbacks {

  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet
   * device.
   */
  private boolean mTwoPane;

  /**
   * Turn audio/video data collection
   */
  private void beginDataCollection(String id) {
    Log.d(Config.TAG, "Turning on data collection");

    new File(Config.DEFAULT_OUTPUT_DIRECTORY).mkdirs();

    Intent intent = new Intent(this, VideoRecorder.class);

    intent.putExtra(Config.EXTRA_USE_FRONT_FACING_CAMERA, true);
    intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
    intent.putExtra(Config.EXTRA_PARTICIPANT_ID, "00000");
    intent.putExtra(Config.EXTRA_OUTPUT_DIR, Config.DEFAULT_OUTPUT_DIRECTORY);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME,
        Config.DEFAULT_OUTPUT_DIRECTORY + "/" + id + System.currentTimeMillis() + "_" + ".3gp");
    intent.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION,
        "ParticipantID,FirstName,LastName,WorstLanguage,FirstBat,StartTime,EndTime,ExperimenterID");

    this.startActivity(intent);
  }

  public void launchExperiment(String id) {
    if (this.mTwoPane) {
      // In two-pane mode, show the detail view in this activity by
      // adding or replacing the detail fragment using a
      // fragment transaction.
      Bundle arguments = new Bundle();
      arguments.putString(ExperimentFragment.ARG_ITEM_ID, id);
      ExperimentFragment fragment = new ExperimentFragment();
      fragment.setArguments(arguments);
      this.getSupportFragmentManager().beginTransaction().replace(R.id.experiment_detail_container, fragment)
          .commitAllowingStateLoss();
    } else {
      // In single-pane mode, simply start the detail activity
      // for the selected item ID.
      Intent detailIntent = new Intent(this, ExperimentActivity.class);
      detailIntent.putExtra(ExperimentFragment.ARG_ITEM_ID, id);
      this.startActivityForResult(detailIntent, Config.CODE_EXPERIMENT_COMPLETED);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, requestCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == Config.CODE_EXPERIMENT_COMPLETED) {
        Intent i = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
        this.sendBroadcast(i);
        Intent audio = new Intent(this, AudioRecorder.class);
        this.stopService(audio);
        Log.d(Config.TAG, "Requesting video recording to exit from the activity result.");
      } else if (requestCode == Config.CODE_PICTURE_TAKEN) {
        String pictureFilePath = data.getExtras().getString(Config.EXTRA_RESULT_FILENAME);
        Log.d(Config.TAG, "Saved image as " + pictureFilePath);
      }

    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_experiment_list);

    if (this.findViewById(R.id.experiment_detail_container) != null) {
      // The detail container view will be present only in the
      // large-screen layouts (res/values-large and
      // res/values-sw600dp). If this view is present, then the
      // activity should be in two-pane mode.
      this.mTwoPane = true;

      // In two-pane mode, list items should be given the
      // 'activated' state when touched.
      ((ExperimentListFragment) this.getSupportFragmentManager().findFragmentById(R.id.experiment_list))
          .setActivateOnItemClick(true);
    }

    // TODO: If exposing deep links into your app, handle intents here.
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   * Callback method from {@link ExperimentListFragment.Callbacks} indicating
   * that the item with the given ID was selected.
   */
  @Override
  public void onItemSelected(final String id) {
    if ("Photo".equals(id)) {

      new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/image/").mkdirs();
      Intent intent = new Intent(this, TakePicture.class);
      intent.putExtra(Config.EXTRA_RESULT_FILENAME,
          Config.DEFAULT_OUTPUT_DIRECTORY + "/image/" + System.currentTimeMillis() + ".png");
      this.startActivityForResult(intent, Config.CODE_PICTURE_TAKEN);

    }else if ("Audio".equals(id)) {

      new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/audio/").mkdirs();
      Intent intent = new Intent(this, AudioRecorder.class);
      intent.putExtra(Config.EXTRA_RESULT_FILENAME,
          Config.DEFAULT_OUTPUT_DIRECTORY + "/audio/" + System.currentTimeMillis()+ ".mp3");
      this.startService(intent);
    
    } else {
      this.beginDataCollection(id);

      /*
       * Wait two seconds so that the video activity has time to load the
       * camera. It will continue recording until you exit the video activity.
       */
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          ExperimentListActivity.this.launchExperiment(id);
        }
      }, 3000);
    }

  }

}
