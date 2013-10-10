package ca.ilanguage.oprime.datacollection;

import java.io.IOException;

import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.content.OPrime;
import ca.ilanguage.oprime.content.OPrimeApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.widget.VideoView;

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
 * To call it, the following Extras are available:
 * 
 * Intent intent; intent = new Intent(
 * "ca.ilanguage.oprime.intent.action.START_VIDEO_RECORDER");
 * 
 * intent.putExtra(OPrime.EXTRA_USE_FRONT_FACING_CAMERA, true);
 * intent.putExtra(OPrime.EXTRA_VIDEO_QUALITY,
 * OPrime.DEFAULT_DEBUGGING_QUALITY); will record low quality videos to save
 * space and runtime memory
 * 
 * startActivityForResult(intent, OPrime.EXPERIMENT_COMPLETED);
 */
@Deprecated
public class VideoRecorder extends Activity implements SurfaceHolder.Callback {

  public static final String EXTRA_VIDEO_QUALITY = "videoQuality";
  public static final String EXTRA_USE_FRONT_FACING_CAMERA = "usefrontcamera";
  /* .5 megapixel */
  public static final int DEFAULT_DEBUGGING_QUALITY = 500000;
  /* 3 megapixel */
  public static final int DEFAULT_HIGH_QUALITY = 3000000;

  /*
   * Recording variables
   */
  public boolean D = true;
  private static final String TAG = "VideoRecorder";
  private Boolean mRecording = false;
  private Boolean mRecordingAudioInstead = false;
  private Boolean mUseFrontFacingCamera = false;
  private VideoView mVideoView = null;
  private MediaRecorder mVideoRecorder = null;
  private Camera mCamera;
  private static int cameraNumberUsed = -1;
  Context mContext;
  private int mVideoQuality = DEFAULT_HIGH_QUALITY;

  String mAudioResultsFile = "";
  private VideoStatusReceiver videoStatusReceiver;

  @Override
  protected void onResume() {
    super.onResume();

    if (videoStatusReceiver == null) {
      videoStatusReceiver = new VideoStatusReceiver();
    }
    IntentFilter intentStopped = new IntentFilter(
        Config.INTENT_STOP_VIDEO_RECORDING);
    registerReceiver(videoStatusReceiver, intentStopped);

    D = ((OPrimeApp) getApplication()).D;

  }

  public void beginRecordingAudio() {
    if (mRecordingAudioInstead) {
      return;
    }
    Intent intent;
    intent = new Intent(this, AudioRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, getIntent().getExtras()
        .getString(Config.EXTRA_RESULT_FILENAME));
    startService(intent);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initalize();
  }

  @Override
  public void onLowMemory() {
    if(D) Log.w(TAG, "Low memory...closing");
    super.onLowMemory();
  }

  @SuppressWarnings("deprecation")
  public void initalize() {
    /*
     * Get extras from the Experiment Home screen and set up layout depending on
     * extras
     */
    setContentView(R.layout.video_recorder);

    mVideoView = (VideoView) this.findViewById(R.id.videoView);
    mVideoQuality = getIntent().getExtras().getInt(EXTRA_VIDEO_QUALITY,
        DEFAULT_HIGH_QUALITY);
    /* default is high quality */
    mAudioResultsFile = getIntent().getExtras().getString(
        Config.EXTRA_RESULT_FILENAME);
    mUseFrontFacingCamera = getIntent().getExtras().getBoolean(
        EXTRA_USE_FRONT_FACING_CAMERA, true);

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
    DoTheRecordVideoThing beginRecordingTask = new DoTheRecordVideoThing();
    beginRecordingTask.setHolder(holder);
    beginRecordingTask.execute();
  }

  public class DoTheRecordVideoThing extends AsyncTask<Void, Void, String> {
    SurfaceHolder holder;

    public SurfaceHolder getHolder() {
      return holder;
    }

    public void setHolder(SurfaceHolder holder) {
      this.holder = holder;
    }

    @Override
    protected String doInBackground(Void... params) {
      Log.v(TAG, "DoTheRecordVideoThing doInBackground");
      try {
        beginRecording(holder);
      } catch (Exception e) {
        Log.e(TAG, "Error calling begin recording " + e.toString());
        beginRecordingAudio();
        return "recording audio instead.";
      }
      return "beginRecording didnt throw an error.";
    }

    protected void onPreExecute() {
      Log.v(TAG, "DoTheRecordVideoThing onPreExecute");
    }

    protected void onPostExecute(String result) {
      Log.v(TAG, "DoTheRecordVideoThing onPostExecute " + result);
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    Log.v(TAG, "Width x Height = " + width + "x" + height);
  }

  private void stopRecording() throws Exception {
    if (mVideoRecorder != null) {
      if (mRecording) {
        if (D)
          Log.d(TAG, "Telling mVideoRecorder to stop. ");
        mVideoRecorder.stop();
      }
      mVideoRecorder.release();
      mVideoRecorder = null;
      Toast.makeText(getApplicationContext(), "Saving.", Toast.LENGTH_LONG)
          .show();
    }
    if (mCamera != null) {
      mCamera.reconnect();
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
    mRecording = false;
  }

  /*
   * Instead of calling the super, this stops the the recording and then
   * finishes the activity. Without this the camera was experiencing death on
   * Nexus 7 (non-Javadoc)
   * 
   * @see android.app.Activity#onBackPressed()
   */
  @Override
  public void onBackPressed() {
    try {
      stopRecording();
    } catch (Exception e) {
      if (D)
        Log.e(TAG, "Error calling stopRecording.");
      if (D)
        e.printStackTrace();
    }
    finish();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  /*
   * Notes: -Beware of security hazard of running code in this receiver. In this
   * case, only stopping and saving the recording. -Receivers should be
   * registered in the manifest, but this is an inner class so that it can
   * access the member functions of this class so it doesn't need to be
   * registered in the manifest.xml.
   * 
   * http:stackoverflow.com/questions/2463175/how-to-have-android-service-
   * communicate-with-activity http:thinkandroid.wordpress.com/2010/02/02/custom
   * -intents-and-broadcasting-with-receivers/
   * 
   * could pass data in the Intent instead of updating database tables
   * 
   * @author cesine
   */
  public class VideoStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Config.INTENT_STOP_VIDEO_RECORDING)) {
        finish();
      }
    }
  }

  @Override
  protected void onDestroy() {
    try {
      stopRecording();
    } catch (Exception e) {
      if (D)
        Log.e(TAG, e.toString());
    }
    super.onDestroy();
    if (videoStatusReceiver != null) {
      unregisterReceiver(videoStatusReceiver);
    }
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
  private void beginRecording(SurfaceHolder holder) throws IOException {
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
        finish();
        return;
      }

//      mCamera.setPreviewDisplay(holder); //this line doesn't seem to be necessary
//      Camera.Parameters parameters = mCamera.getParameters();
//      // parameters.setPreviewSize(640, 480);
//      List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//      Camera.Size previewSize = previewSizes.get(0);
//      Log.d(TAG, "These are supported by the device . " + previewSize.width
//          + " : " + previewSize.height);
//      parameters.setPreviewSize(previewSize.width, previewSize.height);
//      mCamera.setParameters(parameters);

      mCamera.startPreview();
      mCamera.unlock(); // managed for you after 4.0

      mVideoRecorder = new MediaRecorder();
      // mVideoRecorder.reset();
      mVideoRecorder.setCamera(mCamera);

      /*
       * Media recorder setup is based on Listing 9-6, Hashimi et all 2010
       * values based on best practices and good quality, tested via upload to
       * YouTube and played in QuickTime on Mac Snow Leopard
       */
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
      finish();

    }
  }
}
