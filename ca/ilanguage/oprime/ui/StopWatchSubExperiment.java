/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.ilanguage.oprime.ui;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.VideoView;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.VideoRecorderAsyncTask;
import ca.ilanguage.oprime.model.OPrimeApp;
import ca.ilanguage.oprime.model.Stimulus;
import ca.ilanguage.oprime.model.SubExperimentBlock;

public class StopWatchSubExperiment extends Activity implements SurfaceHolder.Callback {
  protected static String                 TAG            = "StopWatchSubExperiment";
  protected boolean                       D              = true;
  protected long                          lastPause      = 0;
  protected Chronometer                   mChronometer;
  protected Boolean                       mRecording     = false;
  View.OnClickListener                    mResetListener = new OnClickListener() {
                                                           @Override
                                                           public void onClick(View v) {
                                                             StopWatchSubExperiment.this.mChronometer
                                                                 .setBase(SystemClock.elapsedRealtime());
                                                           }
                                                         };

  View.OnClickListener                    mStartListener = new OnClickListener() {
                                                           @Override
                                                           public void onClick(View v) {
                                                             if (StopWatchSubExperiment.this.lastPause == 0) {
                                                               StopWatchSubExperiment.this.mChronometer
                                                                   .setBase(SystemClock.elapsedRealtime());

                                                             } else {
                                                               StopWatchSubExperiment.this.mChronometer
                                                                   .setBase(StopWatchSubExperiment.this.mChronometer
                                                                       .getBase()
                                                                       + SystemClock.elapsedRealtime()
                                                                       - StopWatchSubExperiment.this.lastPause);
                                                             }

                                                             StopWatchSubExperiment.this.mChronometer.start();
                                                           }
                                                         };
  protected ArrayList<? extends Stimulus> mStimuli;
  View.OnClickListener                    mStopListener  = new OnClickListener() {
                                                           @Override
                                                           public void onClick(View v) {
                                                             StopWatchSubExperiment.this.lastPause = SystemClock
                                                                 .elapsedRealtime();

                                                             StopWatchSubExperiment.this.mChronometer.stop();

                                                           }
                                                         };

  protected SubExperimentBlock            mSubExperiment;

  protected VideoView                     mVideoView     = null;

  /*
   * Video variables
   */
  protected VideoRecorderAsyncTask        recordVideoTask;

  public void finishSubExperiment() {
    this.mSubExperiment.setDisplayedStimuli(this.mStimuli.size());
    this.mSubExperiment.setStimuli(this.mStimuli);
    Intent video = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
    this.sendBroadcast(video);
    Intent audio = new Intent(this, AudioRecorder.class);
    this.stopService(audio);

    this.mSubExperiment.setResultsFileWithoutSuffix(this.getIntent().getExtras()
        .getString(Config.EXTRA_RESULT_FILENAME).replace(".3gp", ""));
    Intent intent = new Intent(Config.INTENT_FINISHED_SUB_EXPERIMENT);
    intent.putExtra(Config.EXTRA_SUB_EXPERIMENT, this.mSubExperiment);
    this.setResult(Config.CODE_EXPERIMENT_COMPLETED, intent);

    try {
      if (this.D)
        Log.d(TAG, "Telling recorder asyc to stop. ");
      if (this.recordVideoTask != null) {
        this.recordVideoTask.stopRecording();
      }
    } catch (Exception e) {
      if (this.D)
        Log.d(TAG, "Error Telling recorder asyc to stop. ");
      e.printStackTrace();
    }
    this.finish();
  }

  /**
   * Requires android:configChanges="orientation|keyboardHidden|screenSize" in
   * the manifest
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (this.D)
      Log.d(TAG, "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.setContentView(R.layout.fragment_stop_watch);
    this.D = ((OPrimeApp) this.getApplication()).D;
    TAG = Config.TAG;
    Button button;

    this.mChronometer = (Chronometer) this.findViewById(R.id.chronometer);

    // Watch for button clicks.
    button = (Button) this.findViewById(R.id.start);
    button.setOnClickListener(this.mStartListener);

    button = (Button) this.findViewById(R.id.stop);
    button.setOnClickListener(this.mStopListener);

    button = (Button) this.findViewById(R.id.reset);
    button.setOnClickListener(this.mResetListener);

    /*
     * Prepare Stimuli
     */
    this.mSubExperiment = (SubExperimentBlock) this.getIntent().getExtras()
        .getSerializable(Config.EXTRA_SUB_EXPERIMENT);
    this.setTitle(this.mSubExperiment.getTitle());
    this.mStimuli = this.mSubExperiment.getStimuli();

    if (this.mStimuli == null || this.mStimuli.size() == 0) {
      ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
      ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
      this.mStimuli = ids;
    }

    TextView t = (TextView) this.findViewById(R.id.stimuli_number);
    String displayStimuliLabel = this.mStimuli.get(0).getLabel();
    if ("".equals(displayStimuliLabel)) {
      int stimnumber = 1;
      int stimtotal = 1;
      displayStimuliLabel = stimnumber + "/" + stimtotal;
    }
    t.setText(displayStimuliLabel);

    /*
     * Set up the video recording
     */
    this.mVideoView = (VideoView) this.findViewById(R.id.videoView);
    final SurfaceHolder holder = this.mVideoView.getHolder();
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
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      this.finishSubExperiment();
    }
    return super.onKeyDown(keyCode, event);

  }

  public void onNextClick(View v) {
    this.finishSubExperiment();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (this.D)
      Log.v(TAG, "Width x Height = " + width + "x" + height);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (this.mRecording) {
      return;
    }
    if (this.D)
      Log.d(TAG, "Preparing to record. ");
    this.recordVideoTask = new VideoRecorderAsyncTask();
    this.recordVideoTask.setContext(this);
    this.recordVideoTask.setParentUI(this);
    this.recordVideoTask.setHolder(holder);
    if (this.D)
      Log.d(TAG, "Telling recorder asyc to execute. ");
    this.recordVideoTask.execute();

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }

}
