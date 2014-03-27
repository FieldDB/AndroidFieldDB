package ca.ilanguage.oprime.datacollection;

import java.io.File;
import java.io.IOException;

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
import ca.ilanguage.oprime.Config;


/**
 * Android video recorder with "no" preview (the preview is a 1x1 pixel which
 * simulates an unobtrusive recording led). Based on Pro Android 2 2010 (Hashimi
 * et al) source code in Listing 9-6.
 * 
 * Also demonstrates how to use the front-facing and back-facing cameras. A
 * calling Intent can pass an Extra to use the front facing camera if available.
 * 
 * Suitable use cases: A: eye gaze tracking library to let users use eyes as a
 * mouse to navigate a web page B: use tablet camera(s) to replace video camera
 * in lab experiments (psycholingusitics or other experiments)
 * 
 * Video is recording is controlled in two ways: 1. Video starts and stops with
 * the activity 2. Video starts and stops on any touch
 * 
 * To control recording in other ways see the try blocks of the onTouchEvent
 * 
 * To incorporate into project add these features and permissions to
 * manifest.xml:
 * 
 * <uses-feature android:name="android.hardware.camera"/> <uses-feature
 * android:name="android.hardware.camera.autofocus"/>
 * 
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.CAMERA" /> <uses-permission
 * android:name="android.permission.RECORD_AUDIO" />
 * 
 * Tested Date: October 2 2011 with manifest.xml <uses-sdk
 * android:minSdkVersion="8" android:targetSdkVersion="11"/>
 * 
 */
public class VideoRecorderAsyncTask extends AsyncTask<Void, Void, String> {

  protected static int    cameraNumberUsed = -1;
  protected static String TAG              = "VideoRecorder";

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

  /*
   * Recording variables
   */
  public boolean          D                      = true;
  protected SurfaceHolder holder;
  String                  mVideoResultsFile      = "";
  protected Camera        mCamera;
  protected Context       mContext;
  protected Activity      mParentUI;
  protected Boolean       mRecording             = false;

  protected Boolean       mRecordingAudioInstead = false;

  protected MediaRecorder mVideoRecorder         = null;

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
    if (this.mVideoRecorder != null) {
      if (this.D)
        Log.d(TAG, "mVideoRecorder was not null. ");
      if (this.mRecording) {
        if (this.D)
          Log.d(TAG, "Telling mVideoRecorder to stop before we start. ");
        this.mVideoRecorder.stop();
      }
      this.mVideoRecorder.release();
      this.mVideoRecorder = null;
    }
    if (this.mCamera != null) {
      this.mCamera.reconnect();
      this.mCamera.stopPreview();
      this.mCamera.release();
      this.mCamera = null;
    }
    try {
      this.mCamera = getCameraInstance();
      if (this.mCamera == null) {
        if (this.D)
          Log.e(TAG, "There was a problem opening the camera. ");
        // TODO email devs?
        this.beginRecordingAudio();
        return;
      }

      this.mCamera.startPreview();
      this.mCamera.unlock(); // managed for you after 4.0

      this.mVideoRecorder = new MediaRecorder();
      this.mVideoRecorder.setCamera(this.mCamera);

      this.mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      this.mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
      int sdk = android.os.Build.VERSION.SDK_INT;
      if (sdk > 7) {
        if (cameraNumberUsed == -1) {
          if (this.D)
            Log.e(TAG, "This appears to have no camera set, trying another resolution.");
          this.mVideoRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        } else {
          this.mVideoRecorder.setProfile(CamcorderProfile.get(cameraNumberUsed, CamcorderProfile.QUALITY_HIGH));
        }
      } else {
        if (this.D)
          Log.e(TAG, "This appears to be android 2.1, trying to set the audio and video manually.");
        this.mVideoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        this.mVideoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        this.mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
      }

      this.mVideoRecorder.setOutputFile(this.mVideoResultsFile);
      this.mVideoRecorder.setPreviewDisplay(holder.getSurface());
      
      try{
        this.mVideoRecorder.prepare();
      }catch(Exception e){
        Log.d(TAG, "There was a problem preparing the video recorder.");
      }
      Thread.sleep(500);
      this.mVideoRecorder.start();
      this.mRecording = true;
    } catch (Exception e) {
      if (this.D)
        Log.e(TAG, "There was a problem with the camera " + e.toString());
      this.mRecording = false;
      this.beginRecordingAudio();
    }
  }

  public void beginRecordingAudio() {
    if (this.mRecordingAudioInstead) {
      return;
    }
    Intent intent;
    intent = new Intent(this.mParentUI, AudioRecorder.class);
    mVideoResultsFile = mVideoResultsFile.replace(Config.DEFAULT_VIDEO_EXTENSION, Config.DEFAULT_AUDIO_EXTENSION);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, mVideoResultsFile);
    this.mParentUI.startService(intent);
    this.mRecordingAudioInstead = true;
    Log.e(TAG, "Recording audio instead " + this.mVideoResultsFile);
  }

  @Override
  protected String doInBackground(Void... params) {
    if (this.D)
      Log.v(TAG, " doInBackground");
    try {
      this.beginRecording(this.holder);
    } catch (Exception e) {
      if (this.D)
        Log.e(TAG, "Error calling begin recording " + e.toString());
      return "error recording video.";
    }
    return "okay";
  }

  public Context getContext() {
    return this.mContext;
  }

  public SurfaceHolder getHolder() {
    return this.holder;
  }

  public Activity getParentUI() {
    return this.mParentUI;
  }

  @Override
  protected void onPostExecute(String result) {
    if (this.D)
      Log.v(TAG, " onPostExecute " + result);
    if (result.startsWith("error")) {
      this.beginRecordingAudio();
    }
  }

  @Override
  protected void onPreExecute() {
    this.D = Config.D;
    if (this.D)
      Log.v(TAG, " onPreExecute");
    TAG = Config.TAG;
    this.mVideoResultsFile = this.mParentUI.getIntent().getExtras().getString(Config.EXTRA_RESULT_FILENAME);
    if(mVideoResultsFile == null){
      mVideoResultsFile = Config.DEFAULT_OUTPUT_DIRECTORY + "/video/Unnamed_result_file_" + System.currentTimeMillis()
          + "_" + Config.DEFAULT_VIDEO_EXTENSION;
    }
    (new File(mVideoResultsFile).getParentFile()).mkdirs();
    
    if (this.D)
      Log.d(TAG, "mAudioResultsFile" + this.mVideoResultsFile);
  }

  public void setContext(Context mContext) {
    this.mContext = mContext;
  }

  public void setHolder(SurfaceHolder holder) {
    this.holder = holder;
  }

  public void setParentUI(Activity mParentUI) {
    this.mParentUI = mParentUI;
  }

  public void stopRecording() throws Exception {
    if (this.mVideoRecorder != null) {
      if (this.mRecording) {
        if (this.D)
          Log.d(TAG, "We are recording. Telling mVideoRecorder to stop. ");
      }
      this.mVideoRecorder.stop();
      this.mVideoRecorder.release();
      this.mVideoRecorder = null;
      Toast.makeText(this.mContext, "Saving.", Toast.LENGTH_LONG).show();
    }
    if (this.mCamera != null) {
      this.mCamera.reconnect();
      this.mCamera.stopPreview();
      this.mCamera.release();
      this.mCamera = null;
    }
    this.mRecording = false;
  }

}
