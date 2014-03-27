package ca.ilanguage.oprime.datacollection;

import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.VideoView;

/**
 * Example call:
 * 
 * Intent intent = new Intent(this, VideoRecorder.class);
 * 
 * intent.putExtra(Config.EXTRA_USE_FRONT_FACING_CAMERA, true);
 * intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
 * intent.putExtra(Config.EXTRA_PARTICIPANT_ID, "00000");
 * intent.putExtra(Config.EXTRA_OUTPUT_DIR, Config.DEFAULT_OUTPUT_DIRECTORY);
 * intent.putExtra(Config.EXTRA_RESULT_FILENAME, Config.DEFAULT_OUTPUT_DIRECTORY
 * + "/" +id+ System.currentTimeMillis() + "_" + Config.DEFAULT_VIDEO_EXTENSION);
 * intent.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION,
 * "ParticipantID,FirstName,LastName,WorstLanguage,FirstBat,StartTime,EndTime,ExperimenterID"
 * );
 * 
 * startActivityForResult(intent, Config.EXPERIMENT_COMPLETED);
 * 
 * 
 */
public class VideoRecorder extends Activity implements SurfaceHolder.Callback {
  protected VideoRecorderAsyncTask mRecordVideoTask;
  protected Boolean                mRecording = false;
  protected VideoStatusReceiver    mVideoStatusReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.fragment_fixation_video_recorder);
    
    /*
     * Set up the video recording
     */
    VideoView videoView = (VideoView) this.findViewById(R.id.videoView);
    final SurfaceHolder holder = videoView.getHolder();
    holder.addCallback(this);
    int sdk = android.os.Build.VERSION.SDK_INT;
    /*
     * After 11 this is set by default http:stackoverflow.com/questions/9439186
     * /surfaceholder-settype-is-deprecated-but-required
     */
    if (sdk < 11) {
      holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (this.mRecording) {
      Log.d(Config.TAG, "Surface was created but we are already running.");
      return;
    }
    if (Config.D)
      Log.d(Config.TAG, "Preparing to record. ");
    this.mRecordVideoTask = new VideoRecorderAsyncTask();
    this.mRecordVideoTask.setContext(this);
    this.mRecordVideoTask.setParentUI(this);
    this.mRecordVideoTask.setHolder(holder);

    setResult(Activity.RESULT_OK, getIntent());
    
    if (Config.D)
      Log.d(Config.TAG, "Telling recorder asyc to execute. ");
    this.mRecordVideoTask.execute();
    this.mRecording = true;

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(Config.TAG, "surfaceDestroyed. ");
  }

  public boolean finishSubExperiment(boolean fromExternalRequest) {
    try {
      if (Config.D)
        Log.d(Config.TAG, "Telling recorder async to stop. ");
      if (this.mRecordVideoTask != null) {
        this.mRecordVideoTask.stopRecording();
        if (!this.mRecordVideoTask.mRecording) {
          return true;
        }
      } else {
        Log.d(Config.TAG, "Recording task is null, returning already finished. ");
        return true;
      }
    } catch (Exception e) {
      if (Config.D)
        Log.d(Config.TAG, "Error Telling recorder async to stop. ");
      e.printStackTrace();
    }
    if (fromExternalRequest) {
      this.finish();
    }
    return false;
  }

  @Override
  protected void onDestroy() {
    boolean alreadyFinished = finishSubExperiment(false);
    if (!alreadyFinished) {
      Log.d(Config.TAG, "Experiment wasnt finished");
    }
    if (this.mVideoStatusReceiver != null) {
      this.unregisterReceiver(this.mVideoStatusReceiver);
    }
    /* end the audio recording if it was running */
    Intent audio = new Intent(this, AudioRecorder.class);
	this.stopService(audio);
	
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    finishSubExperiment(false);
    super.onBackPressed();
  }

  public class VideoStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Config.INTENT_STOP_VIDEO_RECORDING)) {
        VideoRecorder.this.finish();
      }
    }
  }

  /**
   * Requires android:configChanges="orientation|keyboardHidden|screenSize" in
   * the manifest
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (Config.D)
      Log.d(Config.TAG, "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  @Override
  public void onLowMemory() {
    if (Config.D)
      Log.w(Config.TAG, "Low memory...closing");
    super.onLowMemory();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (this.mVideoStatusReceiver == null) {
      this.mVideoStatusReceiver = new VideoStatusReceiver();
    }

    IntentFilter intentStopped = new IntentFilter(Config.INTENT_STOP_VIDEO_RECORDING);
    this.registerReceiver(this.mVideoStatusReceiver, intentStopped);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.v(Config.TAG, "Width x Height = " + width + "x" + height);
  }
  
  @Override
  public ComponentName startService(Intent service) {
    setResult(Activity.RESULT_OK, service);
	return super.startService(service);
  }

}
