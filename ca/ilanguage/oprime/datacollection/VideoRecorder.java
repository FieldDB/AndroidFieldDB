package ca.ilanguage.oprime.datacollection;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.widget.VideoView;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.model.OPrimeApp;

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

  public class DoTheRecordVideoThing extends AsyncTask<Void, Void, String> {
    SurfaceHolder holder;

    @Override
    protected String doInBackground(Void... params) {
      Log.v(TAG, "DoTheRecordVideoThing doInBackground");
      try {
        VideoRecorder.this.beginRecording(this.holder);
      } catch (Exception e) {
        Log.e(TAG, "Error calling begin recording " + e.toString());
        VideoRecorder.this.beginRecordingAudio();
        return "recording audio instead.";
      }
      return "beginRecording didnt throw an error.";
    }

    public SurfaceHolder getHolder() {
      return this.holder;
    }

    @Override
    protected void onPostExecute(String result) {
      Log.v(TAG, "DoTheRecordVideoThing onPostExecute " + result);
    }

    @Override
    protected void onPreExecute() {
      Log.v(TAG, "DoTheRecordVideoThing onPreExecute");
    }

    public void setHolder(SurfaceHolder holder) {
      this.holder = holder;
    }
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
        VideoRecorder.this.finish();
      }
    }
  }

  private static int          cameraNumberUsed              = -1;
  /* .5 megapixel */
  public static final int     DEFAULT_DEBUGGING_QUALITY     = 500000;

  /* 3 megapixel */
  public static final int     DEFAULT_HIGH_QUALITY          = 3000000;
  public static final String  EXTRA_USE_FRONT_FACING_CAMERA = "usefrontcamera";
  public static final String  EXTRA_VIDEO_QUALITY           = "videoQuality";
  private static final String TAG                           = "VideoRecorder";

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
  public boolean              D                      = true;
  String                      mAudioResultsFile      = "";
  private Camera              mCamera;
  Context                     mContext;
  private Boolean             mRecording             = false;
  private Boolean             mRecordingAudioInstead = false;

  private Boolean             mUseFrontFacingCamera  = false;
  private int                 mVideoQuality          = DEFAULT_HIGH_QUALITY;

  private MediaRecorder       mVideoRecorder         = null;

  private VideoView           mVideoView             = null;

  private VideoStatusReceiver videoStatusReceiver;

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
    if (this.mVideoRecorder != null) {
      if (this.D)
        Log.d(TAG, "mVideoRecorder was not null. ");
      if (this.mRecording) {
        if (this.D)
          Log.d(TAG, "Telling mVideoRecorder to stop. ");
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
        this.finish();
        return;
      }

      // mCamera.setPreviewDisplay(holder); //this line doesn't seem to be
      // necessary
      // Camera.Parameters parameters = mCamera.getParameters();
      // // parameters.setPreviewSize(640, 480);
      // List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
      // Camera.Size previewSize = previewSizes.get(0);
      // Log.d(TAG, "These are supported by the device . " + previewSize.width
      // + " : " + previewSize.height);
      // parameters.setPreviewSize(previewSize.width, previewSize.height);
      // mCamera.setParameters(parameters);

      this.mCamera.startPreview();
      this.mCamera.unlock(); // managed for you after 4.0

      this.mVideoRecorder = new MediaRecorder();
      // mVideoRecorder.reset();
      this.mVideoRecorder.setCamera(this.mCamera);

      /*
       * Media recorder setup is based on Listing 9-6, Hashimi et all 2010
       * values based on best practices and good quality, tested via upload to
       * YouTube and played in QuickTime on Mac Snow Leopard
       */
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

      this.mVideoRecorder.setOutputFile(this.mAudioResultsFile);
      this.mVideoRecorder.setPreviewDisplay(holder.getSurface());
      this.mVideoRecorder.prepare();
      this.mVideoRecorder.start();
      this.mRecording = true;
    } catch (Exception e) {
      if (this.D)
        Log.e(TAG, "There was a problem with the camera " + e.toString());
      this.mRecording = false;
      this.beginRecordingAudio();
      this.finish();

    }
  }

  public void beginRecordingAudio() {
    if (this.mRecordingAudioInstead) {
      return;
    }
    Intent intent;
    intent = new Intent(this, AudioRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.getIntent().getExtras().getString(Config.EXTRA_RESULT_FILENAME));
    this.startService(intent);
  }

  @SuppressWarnings("deprecation")
  public void initalize() {
    /*
     * Get extras from the Experiment Home screen and set up layout depending on
     * extras
     */
    this.setContentView(R.layout.fragment_video_recorder);

    this.mVideoView = (VideoView) this.findViewById(R.id.videoView);
    this.mVideoQuality = this.getIntent().getExtras().getInt(EXTRA_VIDEO_QUALITY, DEFAULT_HIGH_QUALITY);
    /* default is high quality */
    this.mAudioResultsFile = this.getIntent().getExtras().getString(Config.EXTRA_RESULT_FILENAME);
    this.mUseFrontFacingCamera = this.getIntent().getExtras().getBoolean(EXTRA_USE_FRONT_FACING_CAMERA, true);

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
      this.stopRecording();
    } catch (Exception e) {
      if (this.D)
        Log.e(TAG, "Error calling stopRecording.");
      if (this.D)
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.initalize();
  }

  @Override
  protected void onDestroy() {
    try {
      this.stopRecording();
    } catch (Exception e) {
      if (this.D)
        Log.e(TAG, e.toString());
    }
    super.onDestroy();
    if (this.videoStatusReceiver != null) {
      this.unregisterReceiver(this.videoStatusReceiver);
    }
  }

  @Override
  public void onLowMemory() {
    if (this.D)
      Log.w(TAG, "Low memory...closing");
    super.onLowMemory();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (this.videoStatusReceiver == null) {
      this.videoStatusReceiver = new VideoStatusReceiver();
    }
    IntentFilter intentStopped = new IntentFilter(Config.INTENT_STOP_VIDEO_RECORDING);
    this.registerReceiver(this.videoStatusReceiver, intentStopped);

    this.D = ((OPrimeApp) this.getApplication()).D;

  }

  private void stopRecording() throws Exception {
    if (this.mVideoRecorder != null) {
      if (this.mRecording) {
        if (this.D)
          Log.d(TAG, "Telling mVideoRecorder to stop. ");
        this.mVideoRecorder.stop();
      }
      this.mVideoRecorder.release();
      this.mVideoRecorder = null;
      Toast.makeText(this.getApplicationContext(), "Saving.", Toast.LENGTH_LONG).show();
    }
    if (this.mCamera != null) {
      this.mCamera.reconnect();
      this.mCamera.stopPreview();
      this.mCamera.release();
      this.mCamera = null;
    }
    this.mRecording = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.v(TAG, "Width x Height = " + width + "x" + height);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (this.mRecording) {
      return;
    }
    DoTheRecordVideoThing beginRecordingTask = new DoTheRecordVideoThing();
    beginRecordingTask.setHolder(holder);
    beginRecordingTask.execute();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }
}
