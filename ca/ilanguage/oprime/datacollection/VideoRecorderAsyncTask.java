package ca.ilanguage.oprime.datacollection;

import java.io.IOException;

import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.model.OPrimeApp;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoRecorderAsyncTask extends AsyncTask<Void, Void, String> {

  /*
   * Recording variables
   */
  public boolean D = true;
  protected static String TAG = "VideoRecorder";
  protected Boolean mRecording = false;
  protected Boolean mRecordingAudioInstead = false;
  protected MediaRecorder mVideoRecorder = null;
  protected Camera mCamera;
  protected static int cameraNumberUsed = -1;
  protected Context mContext;
  protected SurfaceHolder holder;
  protected Activity mParentUI;

  String mAudioResultsFile = "";

  public SurfaceHolder getHolder() {
    return holder;
  }

  @Override
  protected String doInBackground(Void... params) {
    if (D)
      Log.v(TAG, "DoTheRecordVideoThing doInBackground");
    try {
      beginRecording(holder);
    } catch (Exception e) {
      if (D)
        Log.e(TAG, "Error calling begin recording " + e.toString());
      return "error recording video.";
    }
    return "okay";
  }

  protected void onPreExecute() {
    D = ((OPrimeApp) mParentUI.getApplication()).D;
    if (D)
      Log.v(TAG, " onPreExecute");
    TAG = ((OPrimeApp) mParentUI.getApplication()).TAG;
    mAudioResultsFile = mParentUI.getIntent().getExtras()
        .getString(Config.EXTRA_RESULT_FILENAME);
    if (D)
      Log.d(TAG, "mAudioResultsFile" + mAudioResultsFile);
  }

  protected void onPostExecute(String result) {
    if (D)
      Log.v(TAG, "DoTheRecordVideoThing onPostExecute " + result);
    if (result.startsWith("error")) {
      beginRecordingAudio();
    }
  }

  @SuppressLint("NewApi")
  public static Camera getCameraInstance() throws IOException {
    Camera c = null;
    /*
     * For Android 2.2 and lower, just open any camera
     */
    int sdk = android.os.Build.VERSION.SDK_INT;
    if (sdk < 9) {
      /* HTC Desire video has lines, but audio is okay */
      Log.d(TAG, "Trying any camera.");
      try {
        c = Camera.open();
      } catch (Exception e) {
        Log.d(TAG, "Any Camera failed.");
      }
      if (c == null) {
        throw new IOException();
      }
      return c;
    }

    /*
     * For Android 2.3 and above, try to get the second camera, the downgrade to
     * the first and any after that
     */
    if (c == null) {
      /* Motorola XOOM, Nexus S */
      Log.d(TAG, "Trying camera 1.");
      try {
        c = Camera.open(1);
      } catch (Exception e) {
        Log.d(TAG, "Camera 1 failed.");
      }
      cameraNumberUsed = 1;
      if (c == null) {
        /* Nexus 7 */
        Log.d(TAG, "Trying camera 0.");
        try {
          c = Camera.open(0);
        } catch (Exception e) {
          Log.d(TAG, "Camera 1 failed.");
        }
        cameraNumberUsed = 0;
        if (c == null) {
          Log.d(TAG, "Trying any camera.");
          try {
            c = Camera.open();
          } catch (Exception e) {
            Log.d(TAG, "Any Camera failed.");
          }
          cameraNumberUsed = -1;
          if (c == null) {
            throw new IOException();
          }
        }
      }
    }
    return c;
  }

  /**
   * Uses the surface defined in video_recorder.xml Tested using 2.2 (HTC
   * Desire/Hero phone) -> Use all defaults works, records back facing camera
   * with AMR_NB audio 3.0 (Motorola Xoom tablet) -> Use all defaults doesn't
   * work, works with these specs, might work with others
   * 
   * Doesnt work on Nexus 7 and Nexus S, this hints at why:
   * http://stackoverflow.
   * com/questions/12098298/android-camera-app-passed-null-surface
   * "The only reason why this code didn't work is because the nexus 7 and droid x both don't support the passing of a dummy surfaceview to a camera object."
   * 
   * 
   * The solution was to put this code in an AsyncTask, and use High_Quality
   * video others didnt work on Nexus S and Nexus 7
   * 
   * @param holder
   *          The surfaceholder from the videoview of the layout
   * @throws Exception
   */
  @SuppressLint("NewApi")
  protected void beginRecording(SurfaceHolder holder) throws IOException {
    if (mVideoRecorder != null) {
      if (D)
        Log.d(TAG, "mVideoRecorder was not null. ");
      if (mRecording) {
        if (D)
          Log.d(TAG, "Telling mVideoRecorder to stop. ");
        mVideoRecorder.stop();
      }
      mVideoRecorder.release();
      mVideoRecorder = null;
    }
    if (mCamera != null) {
      mCamera.reconnect();
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
    try {
      mCamera = getCameraInstance();
      if (mCamera == null) {
        if (D)
          Log.e(TAG, "There was a problem opening the camera. ");
        // TODO email devs?
        beginRecordingAudio();
        return;
      }

      mCamera.startPreview();
      mCamera.unlock(); // managed for you after 4.0

      mVideoRecorder = new MediaRecorder();
      mVideoRecorder.setCamera(mCamera);

      mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
      int sdk = android.os.Build.VERSION.SDK_INT;
      if (sdk > 7) {
        if (cameraNumberUsed == -1) {
          if (D)
            Log.e(TAG,
                "This appears to have no camera set, trying another resolution.");
          mVideoRecorder.setProfile(CamcorderProfile
              .get(CamcorderProfile.QUALITY_HIGH));
        } else {
          mVideoRecorder.setProfile(CamcorderProfile.get(cameraNumberUsed,
              CamcorderProfile.QUALITY_HIGH));
        }
      } else {
        if (D)
          Log.e(TAG,
              "This appears to be android 2.1, trying to set the audio and video manually.");
        mVideoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
      }

      mVideoRecorder.setOutputFile(mAudioResultsFile);
      mVideoRecorder.setPreviewDisplay(holder.getSurface());
      mVideoRecorder.prepare();
      mVideoRecorder.start();
      mRecording = true;
    } catch (Exception e) {
      if (D)
        Log.e(TAG, "There was a problem with the camera " + e.toString());
      mRecording = false;
      beginRecordingAudio();
    }
  }

  public void beginRecordingAudio() {
    if (mRecordingAudioInstead) {
      return;
    }
    Intent intent;
    intent = new Intent(mParentUI, AudioRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, mParentUI.getIntent()
        .getExtras().getString(Config.EXTRA_RESULT_FILENAME));
    mParentUI.startService(intent);
    mRecordingAudioInstead = true;
  }

  public void stopRecording() throws Exception {
    if (mVideoRecorder != null) {
      if (mRecording) {
        if (D)
          Log.d(TAG, "Telling mVideoRecorder to stop. ");
        mVideoRecorder.stop();
      }
      mVideoRecorder.release();
      mVideoRecorder = null;
      Toast.makeText(mContext, "Saving.", Toast.LENGTH_LONG).show();
    }
    if (mCamera != null) {
      mCamera.reconnect();
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
    mRecording = false;
  }

  public void setHolder(SurfaceHolder holder) {
    this.holder = holder;
  }

  public Context getContext() {
    return mContext;
  }

  public void setContext(Context mContext) {
    this.mContext = mContext;
  }

  public Activity getParentUI() {
    return mParentUI;
  }

  public void setParentUI(Activity mParentUI) {
    this.mParentUI = mParentUI;
  }

}
