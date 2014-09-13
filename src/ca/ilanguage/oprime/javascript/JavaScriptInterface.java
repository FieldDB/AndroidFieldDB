package ca.ilanguage.oprime.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.TakePicture;
import ca.ilanguage.oprime.datacollection.VideoRecorder;
import ca.ilanguage.oprime.model.DeviceDetails;
import ca.ilanguage.oprime.model.NonObfuscateable;
import ca.ilanguage.oprime.ui.HTML5Activity;

public abstract class JavaScriptInterface implements Serializable, NonObfuscateable {

  public class ListenForEndAudioInterval extends AsyncTask<Void, Void, String> {
    private int endAudioInterval;

    @Override
    protected String doInBackground(Void... params) {
      if (JavaScriptInterface.this.mMediaPlayer == null) {
        return "No media playing";
      }

      long currentPos = JavaScriptInterface.this.mMediaPlayer.getCurrentPosition();
      while (currentPos < this.endAudioInterval) {
        try {
          // wait some period
          Thread.sleep(100);
          if (JavaScriptInterface.this.mMediaPlayer == null) {
            return "No media playing";
          }
          currentPos = JavaScriptInterface.this.mMediaPlayer.getCurrentPosition();
        } catch (InterruptedException e) {
          return "Cancelled";
        }
      }
      JavaScriptInterface.this.mMediaPlayer.pause();
      Log.d(JavaScriptInterface.this.TAG, "\tPaused audio at ... " + JavaScriptInterface.this.mMediaPlayer.getCurrentPosition());
      return "End audio interval";
    }

    @Override
    protected void onPostExecute(String result) {
      String currentPosition;
      if (JavaScriptInterface.this.mMediaPlayer == null) {
        currentPosition = "";
      } else {
        currentPosition = "" + JavaScriptInterface.this.mMediaPlayer.getCurrentPosition();
      }
      Log.d(JavaScriptInterface.this.TAG, "\t" + result + ": Stopped listening for audio interval at ... " + currentPosition);
    }

    @Override
    protected void onPreExecute() {
    }

    protected void setEndAudioInterval(int message) {
      this.endAudioInterval = message;
    }
  }

  public class LoadUrlToWebView extends AsyncTask<Void, Void, String> {
    private String mMessage;

    @Override
    protected String doInBackground(Void... params) {

      String result = "";
      return result;
    }

    @Override
    protected void onPostExecute(String result) {
      if (JavaScriptInterface.this.getUIParent() != null && JavaScriptInterface.this.getUIParent().mWebView != null) {
        if (JavaScriptInterface.this.D)
          Log.d(JavaScriptInterface.this.TAG, "\tPost execute LoadUrlToWebView task. Now trying to send a pubsub message to the webview." + this.mMessage);
        JavaScriptInterface.this.getUIParent().mWebView.loadUrl(this.mMessage);
      }
    }

    @Override
    protected void onPreExecute() {
    }

    public void setMessage(String message) {
      this.mMessage = message;
    }
  }

  public class WriteStringToFile extends AsyncTask<Void, Void, String> {
    private String contents;
    private String filename;
    private String outputdir;

    @Override
    protected String doInBackground(Void... params) {
      if ("".equals(this.outputdir)) {
        this.outputdir = JavaScriptInterface.this.mOutputDir;
      }

      (new File(this.outputdir)).mkdirs();

      File outfile = new File(this.outputdir + "/" + this.filename);

      try {
        BufferedWriter buf = new BufferedWriter(new FileWriter(outfile, false));
        buf.append(this.contents);
        buf.newLine();
        buf.close();
        return "File written: " + this.filename;
      } catch (IOException inte) {
        Log.d(JavaScriptInterface.this.TAG, "There was an error writing to the file." + inte.getMessage());
        return "File write error: " + this.filename;
      }

    }

    @Override
    protected void onPostExecute(String result) {
      if (JavaScriptInterface.this.D)
        Log.d(JavaScriptInterface.this.TAG, "\t" + result + ": Wrote string to file");
      if (JavaScriptInterface.this.D)
        Toast.makeText(JavaScriptInterface.this.mContext, result, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPreExecute() {
    }

    public void setContents(String contents) {
      this.contents = contents;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public void setOutputdir(String outputdir) {
      this.outputdir = outputdir;
    }
  }

  private static final long serialVersionUID = -4666851545498417224L;
  protected boolean D = true;
  protected String mAssetsPrefix;
  protected String mAudioPlaybackFileUrl;
  protected String mAudioRecordFileUrl;
  protected Context mContext;
  public int mCurrentAudioPosition;
  protected DeviceDetails mDeviceDetails;
  protected Handler mHandler;
  public ListenForEndAudioInterval mListenForEndAudioInterval;
  public MediaPlayer mMediaPlayer;
  protected String mOutputDir;

  protected int mRequestedMediaPlayer = 0;

  protected String mTakeAPictureFileUrl;

  protected String TAG = Config.TAG;

  /**
   * Can pass in all or none of the parameters. Expects the caller to set the
   * context after initialization. This allows this class to be serialized and
   * sent as an Extra for maximum modularity.
   * 
   * @param d
   *          Whether or not the app should log out
   * @param tag
   *          The TAG for the logging
   * @param outputDir
   *          usually on the sdcard where users can see the files, not in the
   *          data dirs (problems opening the files in a webview if you choose
   *          the data dirs)
   * @param context
   *          usually getApplicationContext()
   * @param UIParent
   *          The UI where UI events can happen ie, sending info back to the
   *          webview (usually: this)
   * @param assetsPrefix
   *          the folders deep in the assets dir to get to the base where the
   *          chrome extension is.(example: release/)
   */

  public JavaScriptInterface(boolean d, String tag, String outputDir, Context context, HTML5Activity UIParent, String assetsPrefix) {
    this.D = d;
    this.TAG = tag;
    this.mOutputDir = outputDir;
    this.mContext = context;
    if (this.D)
      Log.d(this.TAG, "Initializing the Javascript Interface (JSI).");
    this.mAudioPlaybackFileUrl = "";
    this.setUIParent(UIParent);
    this.mAssetsPrefix = assetsPrefix;
    this.mHandler = new Handler();

  }

  public JavaScriptInterface(Context context) {
    this.mContext = context;
    this.mOutputDir = Config.DEFAULT_OUTPUT_DIRECTORY;
    this.mAudioPlaybackFileUrl = "";
    if (this.D)
      Log.d(this.TAG, "Initializing the Javascript Interface (JSI).");
    this.mHandler = new Handler();

  }

  @JavascriptInterface
  protected void authenticate(String username, String password) {
    // TODO look in database for user, and then publish result
  }

  @JavascriptInterface
  public String getAssetsPrefix() {
    return this.mAssetsPrefix;
  }

  @JavascriptInterface
  public String getAudioDir() {
    // if its the sdcard, or a web url send that instead
    String outputDir = this.mOutputDir + "audio/";
    new File(outputDir).mkdirs();

    return outputDir;// "file:///android_asset/";
  }

  @JavascriptInterface
  public String getAudioFileUrl() {
    return this.mAudioPlaybackFileUrl;
  }

  @JavascriptInterface
  public void getConnectivityType() {
    // TODO get Connectivity status
    String connectivityType = "WiFi";
    LoadUrlToWebView v = new LoadUrlToWebView();
    v.setMessage("javascript:OPrime.hub.publish('connectivityType','" + connectivityType + "');");
    v.execute();
  }

  @JavascriptInterface
  public void getHardwareDetails() {
    if (this.mDeviceDetails == null) {
      this.mDeviceDetails = new DeviceDetails(this.getUIParent(), this.D, this.TAG);
    }
    String deviceType = this.mDeviceDetails.getCurrentDeviceDetails();

    LoadUrlToWebView v = new LoadUrlToWebView();
    v.setMessage("javascript:OPrime.hub.publish('hardwareDetails'," + deviceType + ");");
    v.execute();
  }

  @JavascriptInterface
  public String getOutputDir() {
    return this.mOutputDir;
  }

  public abstract HTML5Activity getUIParent();

  @JavascriptInterface
  public String getVersionJIS() {
    String versionName;
    try {
      versionName = this.getUIParent().getPackageManager().getPackageInfo(this.getUIParent().getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      Log.d(this.TAG, "Exception trying to get app version");
      return "";
    }
    return versionName;
  }

  @JavascriptInterface
  public boolean isD() {
    return this.D;
  }

  @JavascriptInterface
  public void openExternalLink(String url) {
    Uri uri = Uri.parse(url);
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    this.getUIParent().startActivity(intent);
  }

  @JavascriptInterface
  public void openExternalLink(String url, String filename) {
    if (url == null) {
      return;
    }
    Uri uri = Uri.parse(url);
    String datatype = "text/plain";
    if (url.startsWith("data:text")) {
      url = url.replaceFirst("data:text[^,]*,", "");
      String urlDecoded;
      try {
        urlDecoded = URLDecoder.decode(url, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        urlDecoded = "Error";
      }
      Log.d(this.TAG, "Creating a temporary file with the url context");
      String publicOutputDir = Config.SHARED_OUTPUT_DIR;
      String suffix = url.split(";")[0].replace("data:text/", "");
      datatype = "text/" + suffix;
      if (filename == null) {
        filename = publicOutputDir + System.currentTimeMillis() + "." + suffix;
      } else {
        filename = publicOutputDir + "/" + filename;
      }
      new File(publicOutputDir).mkdirs();
      File outFile = new File(filename);

      if (!outFile.exists()) {
        try {
          outFile.createNewFile();
        } catch (IOException inte) {
          Log.d(this.TAG, inte.getMessage());
        }
      }

      try {
        BufferedWriter buf = new BufferedWriter(new FileWriter(outFile, false));
        buf.append(urlDecoded);
        buf.newLine();
        buf.close();
      } catch (IOException inte) {
        Log.d(this.TAG, inte.getMessage());
      }
      uri = Uri.parse("file://" + filename);
    }
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(datatype);
    intent.putExtra(Intent.EXTRA_STREAM, uri);
    this.getUIParent().startActivity(intent);
  }

  @JavascriptInterface
  public void pauseAudio() {
    if (this.mMediaPlayer != null) {
      if (this.mMediaPlayer.isPlaying()) {
        this.mMediaPlayer.pause();
        this.mCurrentAudioPosition = this.mMediaPlayer.getCurrentPosition();
      }
    }
  }

  @JavascriptInterface
  public void playAudio(String urlstring) {
    urlstring = urlstring.trim();
    if (urlstring == null || "".equals(urlstring.trim())) {
      return;
    }
    if (this.D)
      Log.d(this.TAG, "In the play Audio JSI :" + urlstring + ": playing:" + this.mAudioPlaybackFileUrl + ":");
    if (this.mAudioPlaybackFileUrl.contains(urlstring)) {
      /*
       * Same audio file
       */
      if (this.D)
        Log.d(this.TAG, "Resuming play of the same file :" + this.mAudioPlaybackFileUrl + ":");
      if (this.mMediaPlayer != null) {
        if (this.mMediaPlayer.isPlaying()) {
          this.mMediaPlayer.pause();
          this.mCurrentAudioPosition = this.mMediaPlayer.getCurrentPosition();
          return;
        } else {
          this.mMediaPlayer.seekTo(this.mCurrentAudioPosition);
          this.mMediaPlayer.start();
          return;
        }
      }
    } else {
      /*
       * New audio file
       */
      if (this.D)
        Log.d(this.TAG, "Playing new file from the beginning :" + this.mAudioPlaybackFileUrl + ":");
      if (this.mMediaPlayer != null) {
        if (this.mMediaPlayer.isPlaying()) {
          this.mMediaPlayer.stop();
        }
        this.mMediaPlayer.release();
        this.mMediaPlayer = null;
      }
    }
    this.setAudioFile(urlstring, 0, 0);

  }

  @JavascriptInterface
  public void playIntervalOfAudio(String urlstring, final int startTimeMS, final int endTimeMS) {
    if (this.D)
      Log.d(this.TAG, "In milliseconds from " + startTimeMS + " to " + endTimeMS);
    if (this.mMediaPlayer != null) {
      this.mRequestedMediaPlayer = 0;
      // If the audio is already playing, pause it
      if (this.mMediaPlayer.isPlaying()) {
        this.mMediaPlayer.pause();
      }
      // If there is a background timer waiting for the previous audio interval
      // to finish, kill it
      if (this.mListenForEndAudioInterval != null && !this.mListenForEndAudioInterval.isCancelled()) {
        this.mListenForEndAudioInterval.cancel(true);
        // mListenForEndAudioInterval = null;
      }
      this.mMediaPlayer.seekTo(startTimeMS);
      this.mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
          if (JavaScriptInterface.this.D)
            Log.d(JavaScriptInterface.this.TAG, "current audio position... " + JavaScriptInterface.this.mMediaPlayer.getCurrentPosition());
          JavaScriptInterface.this.mMediaPlayer.start();
          JavaScriptInterface.this.mMediaPlayer.setOnSeekCompleteListener(null);

          // If the endtime is valid, start an async task to loop until the
          // audio gets to the endtime
          if (endTimeMS < JavaScriptInterface.this.mMediaPlayer.getDuration()) {
            JavaScriptInterface.this.mListenForEndAudioInterval = new ListenForEndAudioInterval();
            JavaScriptInterface.this.mListenForEndAudioInterval.setEndAudioInterval(endTimeMS);
            JavaScriptInterface.this.mListenForEndAudioInterval.execute();
          }
        }
      });
    } else {
      if (this.mRequestedMediaPlayer < 2) {
        this.setAudioFile(urlstring, startTimeMS, endTimeMS);
        this.mRequestedMediaPlayer++;
      } else {
        this.showToast("There is a problem starting the media player for this audio: " + urlstring);
      }
    }
  }

  @JavascriptInterface
  public void saveStringToFile(String contents, String filename, String path) {

    WriteStringToFile w = new WriteStringToFile();
    w.setContents(contents);
    w.setFilename(filename);
    w.setOutputdir(path);
    w.execute();

  }

  @JavascriptInterface
  public void setAssetsPrefix(String mAssetsPrefix) {
    this.mAssetsPrefix = mAssetsPrefix;
  }

  /**
   * This is a private method which is used by this class to attach a file to
   * the media player. It is called by either playAudio or playIntervalOfAudio
   * (if its a new audio file or the media player is null).
   * 
   * Preconditions: the mediaplayer is null. IF both cueTo and endAt are 0 then
   * it will play the entire audio
   * 
   * @param urlstring
   *          The file name either on the sdcard, on the web, or in the assets
   *          folder.
   * @param cueTo
   *          The position in milliseconds to play from.
   * @param endAt
   *          The position in milliseconds to end playback. If then the audio
   *          will play completely.
   */
  @JavascriptInterface
  protected void setAudioFile(final String urlstring, final int cueTo, final int endAt) {
    this.mMediaPlayer = new MediaPlayer();
    try {
      if (urlstring.contains("android_asset")) {
        String tempurlstring = urlstring.replace("file:///android_asset/", "");
        this.mAudioPlaybackFileUrl = tempurlstring;
        AssetFileDescriptor afd = this.mContext.getAssets().openFd(this.mAudioPlaybackFileUrl);
        this.mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      } else if (urlstring.contains("sdcard")) {
        this.mAudioPlaybackFileUrl = urlstring;
        this.mMediaPlayer.setDataSource(this.mAudioPlaybackFileUrl);
      } else {
        if (this.D)
          Log.d(this.TAG, "This is what the audiofile looked like:" + urlstring);
        String tempurlstring = urlstring.replaceFirst("/", this.mAssetsPrefix);
        this.mAudioPlaybackFileUrl = tempurlstring;
        if (this.D)
          Log.d(this.TAG, "This is what the audiofile looks like:" + this.mAudioPlaybackFileUrl);

        AssetFileDescriptor afd = this.mContext.getAssets().openFd(this.mAudioPlaybackFileUrl);
        this.mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      }
      this.mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          if (JavaScriptInterface.this.D)
            Log.d(JavaScriptInterface.this.TAG, "Starting to play the audio.");
          if (cueTo == 0 && endAt == 0) {
            JavaScriptInterface.this.mMediaPlayer.start();
          } else {
            JavaScriptInterface.this.playIntervalOfAudio(JavaScriptInterface.this.mAudioPlaybackFileUrl, cueTo, endAt);
          }
        }
      });
      this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          if (JavaScriptInterface.this.D)
            Log.d(JavaScriptInterface.this.TAG, "Audio playback is complete, releasing the audio.");

          JavaScriptInterface.this.mMediaPlayer.release();
          JavaScriptInterface.this.mMediaPlayer = null;
          // getUIParent().loadUrlToWebView();
          LoadUrlToWebView v = new LoadUrlToWebView();
          v.setMessage("javascript:OPrime.hub.publish('playbackCompleted','" + JavaScriptInterface.this.mAudioPlaybackFileUrl + "');");
          v.execute();
        }
      });
      this.mMediaPlayer.prepareAsync();
    } catch (IllegalArgumentException e) {
      Log.e(this.TAG, "There was a problem with the sound " + e.getMessage());
      e.printStackTrace();
    } catch (IllegalStateException e) {
      Log.e(this.TAG, "There was a problem with the sound, starting anyway" + e.getMessage());
      this.mMediaPlayer.start();// TODO check why this is still here.
    } catch (IOException e) {
      Log.e(this.TAG, "There was a problem with the  sound " + e.getMessage());
      e.printStackTrace();

    } catch (Exception e) {
      Log.e(this.TAG, "There was a problem with the  sound " + e.getMessage());
      e.printStackTrace();
    }
  }

  @JavascriptInterface
  public void setAudioFileUrl(String mAudioPlaybackFileUrl) {
    this.mAudioPlaybackFileUrl = mAudioPlaybackFileUrl;
  }

  @JavascriptInterface
  public void setD(boolean newvalue) {
    Config.D = newvalue;
  }

  public abstract void setUIParent(HTML5Activity UIParent);

  @JavascriptInterface
  public void shareIt(String message) {
    Intent share = new Intent(Intent.ACTION_SEND);
    share.setType("text/plain");
    share.putExtra(Intent.EXTRA_TEXT, message);
    this.mContext.startActivity(Intent.createChooser(share, "Share with"));
  }

  @JavascriptInterface
  public void showToast(String toast) {
    Toast.makeText(this.mContext, toast, Toast.LENGTH_LONG).show();
    if (this.D)
      Log.d(this.TAG, "Showing toast " + toast);
  }

  @JavascriptInterface
  public void startAudioRecordingService(String resultfilename) {
    if ("".equals(resultfilename.trim())) {
      if (this.D)
        Log.d(this.TAG, "The resultfilename in startAudioRecordingService was empty.");
      return;
    }
    new File(this.mOutputDir).mkdirs();
    if (this.mAudioRecordFileUrl != null) {
      return;
    }
    if (this.D)
      Log.d(this.TAG, "This is what the audiofile looked like:" + resultfilename);
    String tempurlstring = "";
    tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:", "");
    this.mAudioRecordFileUrl = this.mOutputDir + tempurlstring;
    if (this.D)
      Log.d(this.TAG, "This is what the audiofile looks like:" + this.mAudioRecordFileUrl);

    Intent intent;
    intent = new Intent(this.mContext, AudioRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mAudioRecordFileUrl);
    this.getUIParent().startService(intent);
    // Publish audio recording started
    LoadUrlToWebView v = new LoadUrlToWebView();
    v.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStarted','" + this.mAudioRecordFileUrl + "');");
    v.execute();
  }

  @Deprecated
  public void startVideoRecorder(String resultsFile) {
    String outputDir = this.mOutputDir + "video/";
    new File(outputDir).mkdirs();

    Intent intent;
    // intent = new Intent(OPrime.INTENT_START_VIDEO_RECORDING);
    intent = new Intent(this.mContext, VideoRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, outputDir + resultsFile + ".3gp");

    this.getUIParent().startActivity(intent);
  }

  @JavascriptInterface
  public void stopAudio() {
    if (this.mMediaPlayer != null) {
      if (this.mMediaPlayer.isPlaying()) {
        this.mMediaPlayer.stop();
      }
      this.mMediaPlayer.release();
      this.mMediaPlayer = null;
    }
  }

  @JavascriptInterface
  public void stopAudioRecordingService(String resultfilename) {
    // TODO could do some checking to see if the same file the HTML5 wants us to
    // stop is similarly named to the one in the Java
    // if(mAudioRecordFileUrl.contains(resultfilename))
    if (this.mAudioRecordFileUrl == null) {
      return;
    }
    Intent audio = new Intent(this.mContext, AudioRecorder.class);
    this.getUIParent().stopService(audio);
    // Publish stopped audio
    LoadUrlToWebView v = new LoadUrlToWebView();
    v.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStopped','" + this.mAudioRecordFileUrl + "');");
    v.execute();
    // TODO add broadcast and listener from audio service to be sure the file
    // works(?)
    LoadUrlToWebView t = new LoadUrlToWebView();
    t.setMessage("javascript:OPrime.hub.publish('audioRecordingCompleted','" + this.mAudioRecordFileUrl + "');");
    t.execute();
    // null out the audio file to be sure this is called once per audio file.
    this.mAudioRecordFileUrl = null;
  }

  @JavascriptInterface
  public void takeAPicture(String resultfilename) {
    new File(this.mOutputDir).mkdirs();

    if (this.D)
      Log.d(this.TAG, "This is what the image file looked like:" + resultfilename);
    String tempurlstring = "";
    tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:", "");
    this.mTakeAPictureFileUrl = this.mOutputDir + tempurlstring;
    if (this.D)
      Log.d(this.TAG, "This is what the image file looks like:" + this.mTakeAPictureFileUrl);

    // Publish picture taking started
    LoadUrlToWebView v = new LoadUrlToWebView();
    v.setMessage("javascript:OPrime.hub.publish('pictureCaptureSucessfullyStarted','" + this.mTakeAPictureFileUrl + "');");
    v.execute();

    Intent intent;
    // intent = new Intent(OPrime.INTENT_TAKE_PICTURE);
    intent = new Intent(this.mContext, TakePicture.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mTakeAPictureFileUrl);
    this.getUIParent().startActivityForResult(intent, Config.CODE_PICTURE_TAKEN);
  }

}
