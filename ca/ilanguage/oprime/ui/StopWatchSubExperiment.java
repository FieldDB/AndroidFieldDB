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
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.content.OPrime;
import ca.ilanguage.oprime.content.OPrimeApp;
import ca.ilanguage.oprime.content.Stimulus;
import ca.ilanguage.oprime.content.SubExperimentBlock;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.VideoRecorderAsyncTask;

public class StopWatchSubExperiment extends Activity implements
    SurfaceHolder.Callback {
  protected boolean D = true;
  protected static String TAG = "StopWatchSubExperiment";
  protected Chronometer mChronometer;
  protected long lastPause = 0;
  protected ArrayList<? extends Stimulus> mStimuli;
  protected SubExperimentBlock mSubExperiment;

  /*
   * Video variables
   */
  protected VideoRecorderAsyncTask recordVideoTask;
  protected VideoView mVideoView = null;
  protected Boolean mRecording = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.stop_watch);
    D = ((OPrimeApp) this.getApplication()).D;
    TAG = ((OPrimeApp) this.getApplication()).TAG;
    Button button;

    mChronometer = (Chronometer) findViewById(R.id.chronometer);

    // Watch for button clicks.
    button = (Button) findViewById(R.id.start);
    button.setOnClickListener(mStartListener);

    button = (Button) findViewById(R.id.stop);
    button.setOnClickListener(mStopListener);

    button = (Button) findViewById(R.id.reset);
    button.setOnClickListener(mResetListener);

    /*
     * Prepare Stimuli
     */
    mSubExperiment = (SubExperimentBlock) getIntent().getExtras()
        .getSerializable(Config.EXTRA_SUB_EXPERIMENT);
    this.setTitle(mSubExperiment.getTitle());
    mStimuli = mSubExperiment.getStimuli();

    if (mStimuli == null || mStimuli.size() == 0) {
      ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
      ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
      mStimuli = ids;
    }

    TextView t = (TextView) findViewById(R.id.stimuli_number);
    String displayStimuliLabel = mStimuli.get(0).getLabel();
    if ("".equals(displayStimuliLabel)) {
      int stimnumber = 1;
      int stimtotal = 1;
      displayStimuliLabel = stimnumber + "/" + stimtotal;
    }
    t.setText(displayStimuliLabel);

    /*
     * Set up the video recording
     */
    mVideoView = (VideoView) findViewById(R.id.videoViewStopWatch);
    final SurfaceHolder holder = mVideoView.getHolder();
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

  public void surfaceCreated(SurfaceHolder holder) {
    if (mRecording) {
      return;
    }
    if (D)
      Log.d(TAG, "Preparing to record. ");
    recordVideoTask = new VideoRecorderAsyncTask();
    recordVideoTask.setContext(this);
    recordVideoTask.setParentUI(this);
    recordVideoTask.setHolder(holder);
    if (D)
      Log.d(TAG, "Telling recorder asyc to execute. ");
    recordVideoTask.execute();

  }

  public void surfaceDestroyed(SurfaceHolder holder) {
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    if (D)
      Log.v(TAG, "Width x Height = " + width + "x" + height);
  }

  public void onNextClick(View v) {
    finishSubExperiment();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      finishSubExperiment();
    }
    return super.onKeyDown(keyCode, event);

  }

  /**
   * Requires android:configChanges="orientation|keyboardHidden|screenSize" in
   * the manifest
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (D)
      Log.d(TAG,
          "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  public void finishSubExperiment() {
    mSubExperiment.setDisplayedStimuli(mStimuli.size());
    mSubExperiment.setStimuli(mStimuli);
    Intent video = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
    sendBroadcast(video);
    Intent audio = new Intent(this, AudioRecorder.class);
    stopService(audio);

    mSubExperiment.setResultsFileWithoutSuffix(getIntent().getExtras()
        .getString(Config.EXTRA_RESULT_FILENAME).replace(".3gp", ""));
    Intent intent = new Intent(Config.INTENT_FINISHED_SUB_EXPERIMENT);
    intent.putExtra(Config.EXTRA_SUB_EXPERIMENT, mSubExperiment);
    setResult(Config.EXPERIMENT_COMPLETED, intent);

    try {
      if (D)
        Log.d(TAG, "Telling recorder asyc to stop. ");
      if (recordVideoTask != null) {
        recordVideoTask.stopRecording();
      }
    } catch (Exception e) {
      if (D)
        Log.d(TAG, "Error Telling recorder asyc to stop. ");
      e.printStackTrace();
    }
    finish();
  }

  View.OnClickListener mStartListener = new OnClickListener() {
    public void onClick(View v) {
      if (lastPause == 0) {
        mChronometer.setBase(SystemClock.elapsedRealtime());

      } else {
        mChronometer.setBase(mChronometer.getBase()
            + SystemClock.elapsedRealtime() - lastPause);
      }

      mChronometer.start();
    }
  };

  View.OnClickListener mStopListener = new OnClickListener() {
    public void onClick(View v) {
      lastPause = SystemClock.elapsedRealtime();

      mChronometer.stop();

    }
  };

  View.OnClickListener mResetListener = new OnClickListener() {
    public void onClick(View v) {
      mChronometer.setBase(SystemClock.elapsedRealtime());
    }
  };

}
