package com.github.fielddb.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.github.fielddb.BuildConfig;
import com.github.fielddb.Config;
import com.github.fielddb.datacollection.AudioRecorder;
import com.github.fielddb.datacollection.DeviceDetails;
import com.github.fielddb.datacollection.TakePicture;
import com.github.fielddb.datacollection.VideoRecorder;
import com.github.fielddb.model.NonObfuscateable;

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
import android.webkit.WebView;
import android.widget.Toast;

public abstract class JavaScriptInterface implements Serializable, NonObfuscateable {

  protected static class ListenForEndAudioInterval implements MediaPlayer.OnSeekCompleteListener {
    private long endAudioInterval;
    private boolean paused;

    public void setEndAudioInterval(long endAudioInterval) {
      this.endAudioInterval = endAudioInterval;
    }

    public void setPaused(boolean paused) {
      this.paused = paused;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
      if (mediaPlayer == null) {
        return;
      }

      if (BuildConfig.DEBUG) {
        Log.d(Config.TAG, "current audio position... " + mediaPlayer.getCurrentPosition());
      }
      if (!mediaPlayer.isPlaying()) {
        mediaPlayer.start();
        return;
      }
      // mediaPlayer.setOnSeekCompleteListener(null);

      // If the endtime is valid, start an async task to loop until the
      // audio gets to the endtime
      if (endAudioInterval < mediaPlayer.getDuration()) {
        long currentPos = mediaPlayer.getCurrentPosition();
        if (currentPos < this.endAudioInterval && mediaPlayer != null && !paused) {
          // Continue playing
          return;
        }
        if (mediaPlayer != null) {
          mediaPlayer.pause();
          Log.d(Config.TAG, "\tPaused audio at ... " + mediaPlayer.getCurrentPosition());
        } else {
          Log.d(Config.TAG, "\tPaused audio when media player became null ... ");
        }
      }
    }
  }

  protected static class LoadUrlToWebView extends AsyncTask<Void, Void, String> {
    private String mMessage;
    private WebView mWebView;

    @Override
    protected String doInBackground(Void... params) {
      return "";
    }

    @Override
    protected void onPostExecute(String result) {
      if (mWebView != null) {
        if (BuildConfig.DEBUG) {
          Log.d(Config.TAG, "\tPost execute LoadUrlToWebView task. Now trying to send a pubsub message to the webview."
              + this.mMessage);
        }
        mWebView.loadUrl(this.mMessage);
      }
    }

    public void setMessage(String message) {
      this.mMessage = message;
    }
  }

  protected static class WriteStringToFile extends AsyncTask<Void, Void, String> {
    private String contents;
    private String filename;
    private String outputdir;

    @Override
    protected String doInBackground(Void... params) {
      if ("".equals(this.outputdir)) {
        return "File write error: no output dir specified for " + this.filename;
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
        Log.d(Config.TAG, "There was an error writing to the file." + inte.getMessage());
        return "File write error: " + this.filename;
      }

    }

    @Override
    protected void onPostExecute(String result) {
      if (BuildConfig.DEBUG) {
        Log.d(Config.TAG, "\t" + result + ": Wrote string to file");
      }
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
  protected String mAssetsPrefix;
  protected String mAudioPlaybackFileUrl;
  protected String mAudioRecordFileUrl;
  protected Context mContext;
  protected int mCurrentAudioPosition;
  protected DeviceDetails mDeviceDetails;
  protected Handler mHandler;
  protected ListenForEndAudioInterval mListenForEndAudioInterval;
  protected LoadUrlToWebView mLoadUrlToWebView;
  protected MediaPlayer mMediaPlayer;
  protected String mOutputDir;

  protected int mRequestedMediaPlayer = 0;

  protected String mTakeAPictureFileUrl;

  /**
   * Can pass in all or none of the parameters. Expects the caller to set the
   * context after initialization. This allows this class to be serialized and
   * sent as an Extra for maximum modularity.
   *
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

  public JavaScriptInterface(String outputDir, Context context, HTML5Activity UIParent, String assetsPrefix) {
    this.mOutputDir = outputDir;
    this.mContext = context;
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "Initializing the Javascript Interface (JSI).");
    this.mAudioPlaybackFileUrl = "";
    this.setUIParent(UIParent);
    this.mAssetsPrefix = assetsPrefix;
    this.mHandler = new Handler();

  }

  public JavaScriptInterface(Context context) {
    this.mContext = context;
    this.mOutputDir = Config.DEFAULT_OUTPUT_DIRECTORY;
    this.mAudioPlaybackFileUrl = "";
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "Initializing the Javascript Interface (JSI).");
    this.mHandler = new Handler();

  }

  /**
   * To be called explicitly in Activity's onDestroy
   */
  public void onDestroy() {
    if (mListenForEndAudioInterval != null) {
      this.mListenForEndAudioInterval.setPaused(true);
      this.mListenForEndAudioInterval = null;
    }
    if (this.mMediaPlayer != null) {
      this.mMediaPlayer.stop();
      this.mMediaPlayer.release();
      this.mMediaPlayer = null;
    }

    this.setUIParent(null);
    this.mDeviceDetails = null;
    this.mContext = null;
    this.mHandler = null;

    if (mLoadUrlToWebView != null) {
      mLoadUrlToWebView.mWebView = null;
      this.mLoadUrlToWebView = null;
    }
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
    if (getUIParent() == null) {
      return;
    }
    // TODO get Connectivity status
    String connectivityType = "WiFi";
    if (mLoadUrlToWebView == null) {
      mLoadUrlToWebView = new LoadUrlToWebView();
      mLoadUrlToWebView.mWebView = getUIParent().mWebView;
    }
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('connectivityType','" + connectivityType + "');");
    mLoadUrlToWebView.execute();
  }

  @JavascriptInterface
  public void getHardwareDetails() {
    if (getUIParent() == null) {
      return;
    }
    if (this.mDeviceDetails == null) {
      this.mDeviceDetails = new DeviceDetails(this.getUIParent());
    }
    String deviceType = this.mDeviceDetails.getCurrentDeviceDetails();

    if (mLoadUrlToWebView == null) {
      mLoadUrlToWebView = new LoadUrlToWebView();
      mLoadUrlToWebView.mWebView = getUIParent().mWebView;
    }
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('hardwareDetails'," + deviceType + ");");
    mLoadUrlToWebView.execute();
  }

  @JavascriptInterface
  public String getOutputDir() {
    return this.mOutputDir;
  }

  public abstract HTML5Activity getUIParent();

  @JavascriptInterface
  public String getVersionJIS() {
    if (getUIParent() == null) {
      return "";
    }
    String versionName;
    try {
      versionName = this.getUIParent().getPackageManager().getPackageInfo(this.getUIParent().getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      Log.d(Config.TAG, "Exception trying to get app version");
      return "";
    }
    return versionName;
  }

  @JavascriptInterface
  public boolean isD() {
    return BuildConfig.DEBUG;
  }

  @JavascriptInterface
  public void openExternalLink(String url) {
    if (getUIParent() == null) {
      return;
    }
    Uri uri = Uri.parse(url);
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    this.getUIParent().startActivity(intent);
  }

  /**
   * FIXME why is this is creating a file.
   *
   * @param url
   * @param filename
   */
  @Deprecated
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
      Log.d(Config.TAG, "Creating a temporary file with the url context");
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
          Log.d(Config.TAG, inte.getMessage());
        }
      }

      try {
        BufferedWriter buf = new BufferedWriter(new FileWriter(outFile, false));
        buf.append(urlDecoded);
        buf.newLine();
        buf.close();
      } catch (IOException inte) {
        Log.d(Config.TAG, inte.getMessage());
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
    if (getUIParent() == null) {
      return;
    }
    urlstring = urlstring.trim();
    if (urlstring == null || "".equals(urlstring.trim())) {
      return;
    }
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "In the play Audio JSI :" + urlstring + ": playing:" + this.mAudioPlaybackFileUrl + ":");
    if (this.mAudioPlaybackFileUrl.contains(urlstring)) {
      /*
       * Same audio file
       */
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "Resuming play of the same file :" + this.mAudioPlaybackFileUrl + ":");
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
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "Playing new file from the beginning :" + this.mAudioPlaybackFileUrl + ":");
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
    if (getUIParent() == null) {
      return;
    }
    Log.w(Config.TAG, "TODO playIntervalOfAudio needs to be re-verified to avoid memory leaks");
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "In milliseconds from " + startTimeMS + " to " + endTimeMS);
    if (this.mMediaPlayer != null) {
      this.mRequestedMediaPlayer = 0;
      // If the audio is already playing, pause it
      if (this.mMediaPlayer.isPlaying()) {
        this.mMediaPlayer.pause();
      }
      // If there is a another interval timer waiting for the previous audio
      // interval
      // to finish, kill it
      if (this.mListenForEndAudioInterval != null) {
        this.mListenForEndAudioInterval.setPaused(true);
        mListenForEndAudioInterval = null;
      }
      this.mListenForEndAudioInterval = new ListenForEndAudioInterval();
      this.mListenForEndAudioInterval.setEndAudioInterval(endTimeMS);
      this.mListenForEndAudioInterval.setPaused(false);
      this.mMediaPlayer.setOnSeekCompleteListener(this.mListenForEndAudioInterval);

      this.mMediaPlayer.seekTo(startTimeMS);
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
    if (getUIParent() == null) {
      return;
    }
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
    if (getUIParent() == null) {
      return;
    }
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
        if (BuildConfig.DEBUG)
          Log.d(Config.TAG, "This is what the audiofile looked like:" + urlstring);
        String tempurlstring = urlstring.replaceFirst("/", this.mAssetsPrefix);
        this.mAudioPlaybackFileUrl = tempurlstring;
        if (BuildConfig.DEBUG)
          Log.d(Config.TAG, "This is what the audiofile looks like:" + this.mAudioPlaybackFileUrl);

        AssetFileDescriptor afd = this.mContext.getAssets().openFd(this.mAudioPlaybackFileUrl);
        this.mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      }

      OnPreparedPlay onPrepared = new OnPreparedPlay();
      if (cueTo == 0 && endAt == 0) {
        this.mMediaPlayer.setOnPreparedListener(onPrepared);
      } else {
        this.playIntervalOfAudio(this.mAudioPlaybackFileUrl, cueTo, endAt);
      }

      OnCompletedPublish onCompleted = new OnCompletedPublish();
      onCompleted.mAudioPlaybackFileUrl = this.mAudioPlaybackFileUrl;
      onCompleted.mLoadUrlToWebView = mLoadUrlToWebView;
      this.mMediaPlayer.setOnCompletionListener(new OnCompletedPublish());

      this.mMediaPlayer.prepareAsync();
    } catch (IllegalArgumentException e) {
      Log.e(Config.TAG, "There was a problem with the sound " + e.getMessage());
      e.printStackTrace();
    } catch (IllegalStateException e) {
      Log.e(Config.TAG, "There was a problem with the sound, starting anyway" + e.getMessage());
      this.mMediaPlayer.start();// TODO check why this is still here.
    } catch (IOException e) {
      Log.e(Config.TAG, "There was a problem with the  sound " + e.getMessage());
      e.printStackTrace();

    } catch (Exception e) {
      Log.e(Config.TAG, "There was a problem with the  sound " + e.getMessage());
      e.printStackTrace();
    }
  }

  protected static class OnPreparedPlay implements MediaPlayer.OnPreparedListener {
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
      if (BuildConfig.DEBUG) {
        Log.d(Config.TAG, "Starting to play the audio.");
      }
      mediaPlayer.start();
    }
  }

  protected static class OnCompletedPublish implements MediaPlayer.OnCompletionListener {
    String mAudioPlaybackFileUrl;
    LoadUrlToWebView mLoadUrlToWebView;

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
      if (BuildConfig.DEBUG) {
        Log.d(Config.TAG, "Audio playback is complete, releasing the audio.");
      }

      mediaPlayer.release();
      mediaPlayer = null;
      if (mLoadUrlToWebView == null) {
        return;
      }
      // getUIParent().loadUrlToWebView();
      mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('playbackCompleted','" + mAudioPlaybackFileUrl
          + "');");
      mLoadUrlToWebView.execute();
    }
  }

  @JavascriptInterface
  public void setAudioFileUrl(String mAudioPlaybackFileUrl) {
    this.mAudioPlaybackFileUrl = mAudioPlaybackFileUrl;
  }

  @JavascriptInterface
  public void setD(boolean newvalue) {
    Log.d(Config.TAG, "cannot override debug value");
  }

  public abstract void setUIParent(HTML5Activity UIParent);

  @JavascriptInterface
  public void shareIt(String message) {
    if (getUIParent() == null) {
      return;
    }
    Intent share = new Intent(Intent.ACTION_SEND);
    share.setType("text/plain");
    share.putExtra(Intent.EXTRA_TEXT, message);
    this.mContext.startActivity(Intent.createChooser(share, "Share with"));
  }

  @JavascriptInterface
  public void showToast(String toast) {
    if (getUIParent() == null) {
      return;
    }
    Toast.makeText(this.mContext, toast, Toast.LENGTH_LONG).show();
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "Showing toast " + toast);
  }

  @JavascriptInterface
  public void startAudioRecordingService(String resultfilename) {
    if (getUIParent() == null) {
      return;
    }
    if ("".equals(resultfilename.trim())) {
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "The resultfilename in startAudioRecordingService was empty.");
      return;
    }
    new File(this.mOutputDir).mkdirs();
    if (this.mAudioRecordFileUrl != null) {
      return;
    }
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "This is what the audiofile looked like:" + resultfilename);
    String tempurlstring = "";
    tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:", "");
    this.mAudioRecordFileUrl = this.mOutputDir + tempurlstring;
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "This is what the audiofile looks like:" + this.mAudioRecordFileUrl);

    Intent intent;
    intent = new Intent(this.mContext, AudioRecorder.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mAudioRecordFileUrl);
    this.getUIParent().startService(intent);
    // Publish audio recording started
    if (mLoadUrlToWebView == null) {
      mLoadUrlToWebView = new LoadUrlToWebView();
      mLoadUrlToWebView.mWebView = getUIParent().mWebView;
    }
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStarted','"
        + this.mAudioRecordFileUrl + "');");
    mLoadUrlToWebView.execute();
  }

  @Deprecated
  public void startVideoRecorder(String resultsFile) {
    if (getUIParent() == null) {
      return;
    }
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
    if (getUIParent() == null) {
      return;
    }
    // TODO could do some checking to see if the same file the HTML5 wants us to
    // stop is similarly named to the one in the Java
    // if(mAudioRecordFileUrl.contains(resultfilename))
    if (this.mAudioRecordFileUrl == null) {
      return;
    }
    Intent audio = new Intent(this.mContext, AudioRecorder.class);
    this.getUIParent().stopService(audio);
    // Publish stopped audio
    if (mLoadUrlToWebView == null) {
      mLoadUrlToWebView = new LoadUrlToWebView();
      mLoadUrlToWebView.mWebView = getUIParent().mWebView;
    }
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStopped','"
        + this.mAudioRecordFileUrl + "');");
    mLoadUrlToWebView.execute();
    // TODO add broadcast and listener from audio service to be sure the file
    // works(?)
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('audioRecordingCompleted','" + this.mAudioRecordFileUrl
        + "');");
    mLoadUrlToWebView.execute();
    // null out the audio file to be sure this is called once per audio file.
    this.mAudioRecordFileUrl = null;
  }

  @JavascriptInterface
  public void takeAPicture(String resultfilename) {
    if (getUIParent() == null) {
      return;
    }
    new File(this.mOutputDir).mkdirs();

    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "This is what the image file looked like:" + resultfilename);
    String tempurlstring = "";
    tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:", "");
    this.mTakeAPictureFileUrl = this.mOutputDir + tempurlstring;
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "This is what the image file looks like:" + this.mTakeAPictureFileUrl);

    // Publish picture taking started
    if (mLoadUrlToWebView == null) {
      mLoadUrlToWebView = new LoadUrlToWebView();
      mLoadUrlToWebView.mWebView = getUIParent().mWebView;
    }
    mLoadUrlToWebView.setMessage("javascript:OPrime.hub.publish('pictureCaptureSucessfullyStarted','"
        + this.mTakeAPictureFileUrl + "');");
    mLoadUrlToWebView.execute();

    Intent intent;
    // intent = new Intent(OPrime.INTENT_TAKE_PICTURE);
    intent = new Intent(this.mContext, TakePicture.class);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, this.mTakeAPictureFileUrl);
    this.getUIParent().startActivityForResult(intent, Config.CODE_PICTURE_TAKEN);
  }

}
