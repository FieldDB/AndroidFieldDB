    package ca.ilanguage.oprime.datacollection;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;

@SuppressLint({ "NewApi" })
public class AudioRecorder extends Service {

  public class RecordingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      AudioRecorder.this.saveRecording();
    }

  }

  protected static String     TAG               = "OPrime";
  private RecordingReceiver   mAudioFileUpdateReceiver;
  private int                 mAuBlogIconId     = android.R.drawable.ic_btn_speak_now;
  private String              mAudioResultsFile = "";
  private PendingIntent       mContentIntent;

  private NotificationManager mNM;

  private Notification        mNotification;
  private MediaRecorder       mRecorder;
  private Boolean             mRecordingNow     = false;

  private int                 NOTIFICATION      = 7029;

  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (this.mAudioFileUpdateReceiver == null) {
      this.mAudioFileUpdateReceiver = new RecordingReceiver();
    }
    IntentFilter intentDictRunning = new IntentFilter(Config.INTENT_STOP_AUDIO_RECORDING);
    this.registerReceiver(this.mAudioFileUpdateReceiver, intentDictRunning);

    this.mNM = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
    // The PendingIntent to launch our activity if the user selects this
    // notification
    Intent i = new Intent(this, StopAudioRecorder.class);
    this.mContentIntent = PendingIntent.getActivity(this, 0, i, 0);

    int sdk = android.os.Build.VERSION.SDK_INT;
    Resources res = this.getApplicationContext().getResources();

    if (sdk >= 11) {
      // http://stackoverflow.com/questions/6391870/how-exactly-to-use-notificiation-builder
      Notification.Builder builder = new Notification.Builder(this.getApplicationContext());

      builder.setContentIntent(this.mContentIntent).setSmallIcon(this.mAuBlogIconId)
          .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
          .setTicker(res.getString(R.string.app_name)).setWhen(System.currentTimeMillis()).setAutoCancel(true)
          .setContentTitle(res.getString(R.string.app_name)).setContentText(res.getString(R.string.app_name));
      this.mNotification = builder.getNotification();
    } else {
      this.mNotification = new Notification(this.mAuBlogIconId, res.getString(R.string.app_name),
          System.currentTimeMillis());
      this.mNotification
          .setLatestEventInfo(this, res.getString(R.string.app_name), "Recording...", this.mContentIntent);
      this.mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    }
  }

  @Override
  public void onDestroy() {
    this.saveRecording();
    this.mNM.cancel(this.NOTIFICATION);

    super.onDestroy();
    if (this.mAudioFileUpdateReceiver != null) {
      this.unregisterReceiver(this.mAudioFileUpdateReceiver);
    }

  }

  @Override
  public void onLowMemory() {
    this.saveRecording();
    this.mNM.cancel(this.NOTIFICATION);

    super.onLowMemory();
    if (this.mAudioFileUpdateReceiver != null) {
      this.unregisterReceiver(this.mAudioFileUpdateReceiver);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    String state = Environment.getExternalStorageState();
    if (!Environment.MEDIA_MOUNTED.equals(state)) {
      Log.d(Config.TAG, "SDCARD was not mounted: " + state);
//      return 0;
    }
    this.startForeground(startId, this.mNotification);
    /*
     * get data from extras bundle, store it in the member variables
     */
    try {
      this.mAudioResultsFile = intent.getExtras().getString(Config.EXTRA_RESULT_FILENAME);

    } catch (Exception e) {
      // Toast.makeText(SRTGeneratorActivity.this,
      // "Error "+e,Toast.LENGTH_LONG).show();
    }
    if (this.mAudioResultsFile == null) {
      this.mAudioResultsFile = Config.DEFAULT_OUTPUT_DIRECTORY + "/audio/" + System.currentTimeMillis() +  Config.DEFAULT_AUDIO_EXTENSION;
    }
    this.mAudioResultsFile = this.mAudioResultsFile.replace(Config.DEFAULT_VIDEO_EXTENSION, Config.DEFAULT_AUDIO_EXTENSION);
    
    Uri uri = Uri.parse(mAudioResultsFile);
    String fileName = uri.getLastPathSegment();
    if(fileName != null){
    	String parentDir = mAudioResultsFile.replaceAll(fileName+"$", "");
    	(new File(parentDir)).mkdirs();
    }
    /*
     * turn on the recorder
     */
    this.mRecordingNow = true;
    this.mRecorder = new MediaRecorder();
    try {
      // http://www.benmccann.com/dev-blog/android-audio-recording-tutorial/
      this.mRecordingNow = true;
      this.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      this.mRecorder.setAudioChannels(1); // mono
      this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      int sdk = android.os.Build.VERSION.SDK_INT;
      // gingerbread and up can have wide band ie 16,000 hz recordings (much
      // better for transcription)
      if (sdk >= 10) {
        this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        this.mRecorder.setAudioSamplingRate(16000);
      } else {
        // other devices will have to use narrow band, ie 8,000 hz (same quality
        // as a phone call)
        this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      }
      this.mRecorder.setOutputFile(this.mAudioResultsFile);
      this.mRecorder.prepare();
      this.mRecorder.start();
    } catch (IllegalStateException e) {
      Log.d(TAG, "IllegalStateException in starting audio recorder");
    } catch (IOException e) {
       Log.d(TAG, "IOException in starting audio recorder");
       e.printStackTrace();
    } catch (RuntimeException e) {
       Log.d(TAG, "RuntimeException in starting audio recorder");
       e.printStackTrace();
    } catch (Exception e) {
        Log.d(TAG, "Exception in starting audio recorder");
        e.printStackTrace();
     }

    // autofilled by eclipsereturn super.onStartCommand(intent, flags, startId);
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  private void saveRecording() {
    String appendToContent = "";
    if (this.mRecorder != null) {
      /*
       * if the recorder is running, save everything essentially simulating a
       * click on the save button in the UI
       */
      if (this.mRecordingNow == true) {
        /*
         * Save recording
         */
        this.mRecordingNow = false;
        try {
          this.mRecorder.stop();
          this.mRecorder.release();
          Log.d(TAG, "Turned off the audio recorder.");
        } catch (Exception e) {
          // Do nothing
          Log.d(TAG, "There was an error when off the audio recorder.");
        }
        this.mRecorder = null;

      } else {
        // this should not run
        this.mRecorder.release(); // this is called in the stop save recording
        this.mRecorder = null;
      }
    } else{
      Log.d(TAG, "The audio recorder was null, didnt turn it off.");
    }
  }

}