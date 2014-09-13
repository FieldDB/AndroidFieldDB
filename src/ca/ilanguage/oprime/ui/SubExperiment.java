package ca.ilanguage.oprime.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.VideoRecorderAsyncTask;
import ca.ilanguage.oprime.model.OPrimeApp;
import ca.ilanguage.oprime.model.Stimulus;
import ca.ilanguage.oprime.model.SubExperimentBlock;
import ca.ilanguage.oprime.model.Touch;

public class SubExperiment extends Activity implements SurfaceHolder.Callback {
  Animation                               animationSlideInRight;

  AnimationListener                       animationSlideInRightListener = new AnimationListener() {

                                                                          @Override
                                                                          public void onAnimationEnd(Animation animation) {
                                                                          }

                                                                          @Override
                                                                          public void onAnimationRepeat(
                                                                              Animation animation) {
                                                                          }

                                                                          @Override
                                                                          public void onAnimationStart(
                                                                              Animation animation) {
                                                                          }
                                                                        };
  protected boolean                       D                             = true;
  protected int                           height                        = 1;
  protected Locale                        language;
  MediaPlayer                             mAudioStimuli;
  protected Boolean                       mAutoAdvanceStimuliOnTouch    = false;
  protected long                          mLastTouchTime                = 0;
  protected Boolean                       mRecording                    = false;
  protected ArrayList<? extends Stimulus> mStimuli;
  protected int                           mStimuliIndex                 = -1;
  protected SubExperimentBlock            mSubExperiment;

  MediaPlayer                             mTouchAudio;

  protected VideoView                     mVideoView                    = null;
  /*
   * Video variables
   */
  protected VideoRecorderAsyncTask        recordVideoTask;
  protected String                        TAG                           = "OPrime SubExperiment";

  protected int                           width                         = 1;

  public void finishSubExperiment() {
    this.mSubExperiment.setDisplayedStimuli(this.mStimuliIndex);
    if (this.mStimuli.size() <= 1) {
      this.mSubExperiment.setDisplayedStimuli(this.mStimuli.size());
    }
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
        Log.d(this.TAG, "Telling recorder asyc to stop. ");
      if (this.recordVideoTask != null) {
        this.recordVideoTask.stopRecording();
      }
    } catch (Exception e) {
      if (this.D)
        Log.d(this.TAG, "Error Telling recorder asyc to stop. ");
      e.printStackTrace();
    }

    this.finish();
  }

  /**
   * Forces the locale for the duration of the app to the language needed for
   * that version of the Bilingual Aphasia Test. It accepts a variable in the
   * form en or en-US containing just the language code, or the language code
   * followed by a - and the country code.
   * 
   * @param lang
   * @return
   */
  public String forceLocale(String lang) {
    if (lang.equals(Locale.getDefault().getLanguage())) {
      return Locale.getDefault().getDisplayLanguage();
    }

    Configuration config = this.getBaseContext().getResources().getConfiguration();
    Locale locale;
    if (lang.contains("-")) {
      String[] langCountrycode = lang.split("-");
      locale = new Locale(langCountrycode[0], langCountrycode[1]);
    } else {
      locale = new Locale(lang);
    }
    Locale.setDefault(locale);
    config.locale = locale;
    this.getBaseContext().getResources()
        .updateConfiguration(config, this.getBaseContext().getResources().getDisplayMetrics());
    this.language = Locale.getDefault();

    /*
     * Let the user know if the language is not there.
     */
    String availibleLanguages = "en,el,es,es-ES,fr,iu,iw,kn,ru";
    if (availibleLanguages.contains(lang)) {
      // do nothing, this langauge is supported
    } else {
      Toast.makeText(
          this,
          this.language.getDisplayLanguage()
              + " is not supported yet, we have only put ~8 BAT languages in the app. Please contact us to request "
              + this.language.getDisplayLanguage() + " if you need it.", Toast.LENGTH_LONG).show();
    }

    return Locale.getDefault().getDisplayLanguage();
  }

  public void initalizeLayout() {
    this.setContentView(R.layout.fragment_one_image);

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

    this.nextStimuli();
  }

  public void loadDefaults() {
    ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
    ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
    this.mStimuli = ids;
  }

  public void nextStimuli() {
    if (this.mStimuliIndex < 0) {
      this.mStimuliIndex = 0;
    } else {
      this.mStimuliIndex += 1;
    }
    if (this.mStimuliIndex >= this.mStimuli.size()) {
      this.finishSubExperiment();
      return;
    }

    TextView t = (TextView) this.findViewById(R.id.stimuli_number);
    String displayStimuliLabel = this.mStimuli.get(this.mStimuliIndex).getLabel();
    if ("".equals(displayStimuliLabel)) {
      int stimnumber = this.mStimuliIndex + 1;
      int stimtotal = this.mStimuli.size();
      displayStimuliLabel = stimnumber + "/" + stimtotal;
    }
    t.setText(displayStimuliLabel);

    ImageView image = (ImageView) this.findViewById(R.id.onlyimage);
    Drawable d = this.getResources().getDrawable(this.mStimuli.get(this.mStimuliIndex).getImageFileId());
    image.setImageDrawable(d);
    image.startAnimation(this.animationSlideInRight);
    this.mStimuli.get(this.mStimuliIndex).setStartTime(System.currentTimeMillis());

    this.playAudioStimuli();
  }

  /**
   * Requires android:configChanges="orientation|keyboardHidden|screenSize" in
   * the manifest
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (this.D)
      Log.d(this.TAG, "Configuration has changed (rotation). Not redrawing the screen.");
    /*
     * Doing nothing makes the current redraw properly
     */
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.D = ((OPrimeApp) this.getApplication()).D;
    this.TAG = ((OPrimeApp) this.getApplication()).TAG;

    DisplayMetrics displaymetrics = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    this.height = displaymetrics.heightPixels;
    this.width = displaymetrics.widthPixels;
    this.animationSlideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
    this.animationSlideInRight.setDuration(1000);
    this.animationSlideInRight.setAnimationListener(this.animationSlideInRightListener);
    /*
     * Prepare Stimuli
     */
    this.mSubExperiment = (SubExperimentBlock) this.getIntent().getExtras()
        .getSerializable(Config.EXTRA_SUB_EXPERIMENT);
    this.setTitle(this.mSubExperiment.getTitle());
    this.mStimuli = this.mSubExperiment.getStimuli();
    this.mAutoAdvanceStimuliOnTouch = this.mSubExperiment.isAutoAdvanceStimuliOnTouch();
    if (this.mStimuli == null || this.mStimuli.size() == 0) {
      this.loadDefaults();
    }
    /*
     * Prepare touch audio
     */
    this.mTouchAudio = MediaPlayer.create(this.getApplicationContext(), R.raw.gammatone);

    /*
     * Prepare language of Stimuli
     */
    String lang = this.mSubExperiment.getLanguage();
    this.forceLocale(lang);

    this.initalizeLayout();

  }

  @Override
  protected void onDestroy() {
    if (this.mAudioStimuli != null) {
      this.mAudioStimuli.release();
      this.mAudioStimuli = null;
    }
    if (this.mTouchAudio != null) {
      this.mTouchAudio.release();
      this.mTouchAudio = null;
    }

    super.onDestroy();
  }

  public void onExitClick(View v) {
    this.finishSubExperiment();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      this.finishSubExperiment();
    }
    return super.onKeyDown(keyCode, event);

  }

  public void onNextClick(View v) {
    this.nextStimuli();
  }

  public void onPreviousClick(View v) {
    this.previousStimuli();
  }

  @Override
  public boolean onTouchEvent(MotionEvent me) {
    long timeBetweenTouches = System.currentTimeMillis() - this.mLastTouchTime;
    if (timeBetweenTouches < 1000) {
      return super.onTouchEvent(me);
    }
    // if in the top of the screen, ignore touch it was probably
    // an attempt to hit the button
    if (me.getY() < 60) {
      return super.onTouchEvent(me);
    }
    Touch t = new Touch();
    t.x = me.getX();
    t.y = me.getY();
    t.width = this.width;
    t.height = this.height;
    t.time = System.currentTimeMillis();
    this.recordTouchPoint(t, this.mStimuliIndex);
    this.mLastTouchTime = t.time;
    this.playTouch();
    /*
     * Auto advance to the next stimuli after recording the touch point. the
     * user can use teh arrows if they didnt mean to auto advance.
     */
    if (this.mAutoAdvanceStimuliOnTouch) {
      this.nextStimuli();
    }
    return super.onTouchEvent(me);
  }

  public void playAudioStimuli() {
    if (this.mAudioStimuli != null) {
      this.mAudioStimuli.release();
      this.mAudioStimuli = null;
    }
    this.mAudioStimuli = MediaPlayer.create(this.getApplicationContext(), this.mStimuli.get(this.mStimuliIndex)
        .getAudioFileId());
    try {
      this.mAudioStimuli.prepare();
    } catch (IllegalStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.mAudioStimuli.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        if (SubExperiment.this.mStimuliIndex < SubExperiment.this.mStimuli.size()) {
          SubExperiment.this.mStimuli.get(SubExperiment.this.mStimuliIndex).setAudioOffset(System.currentTimeMillis());
        }
        mp.release();
      }
    });
    this.mAudioStimuli.start();

  }

  public void playTouch() {
    this.mTouchAudio.start();

  }

  public void previousStimuli() {
    this.mStimuliIndex -= 1;

    if (this.mStimuliIndex < 0) {
      this.mStimuliIndex = 0;
      return;
    }
    TextView t = (TextView) this.findViewById(R.id.stimuli_number);
    String displayStimuliLabel = this.mStimuli.get(this.mStimuliIndex).getLabel();
    if ("".equals(displayStimuliLabel)) {
      int stimnumber = this.mStimuliIndex + 1;
      int stimtotal = this.mStimuli.size();
      displayStimuliLabel = stimnumber + "/" + stimtotal;
    }
    t.setText(displayStimuliLabel);

    ImageView image = (ImageView) this.findViewById(R.id.onlyimage);
    Drawable d = this.getResources().getDrawable(this.mStimuli.get(this.mStimuliIndex).getImageFileId());
    image.setImageDrawable(d);

    this.playAudioStimuli();

  }

  public void recordStimuliReactionTime(int stimuli) {
    if (this.mStimuliIndex >= this.mStimuli.size()) {
      return;
    }
    long endtime = System.currentTimeMillis();
    this.mStimuli.get(stimuli).setTotalReactionTime(endtime - this.mStimuli.get(stimuli).getStartTime());
    this.mStimuli.get(stimuli).setReactionTimePostOffset(endtime - this.mStimuli.get(stimuli).getAudioOffset());

  }

  public void recordTouchPoint(Touch touch, int stimuli) {
    if (stimuli >= this.mStimuli.size()) {
      return;
    }
    this.mStimuli.get(stimuli).touches.add(touch);
    this.recordStimuliReactionTime(this.mStimuliIndex);
    // Toast.makeText(getApplicationContext(), touch.x + ":" + touch.y,
    // Toast.LENGTH_LONG).show();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (this.D)
      Log.v(this.TAG, "Width x Height = " + width + "x" + height);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (this.mRecording) {
      return;
    }
    if (this.D)
      Log.d(this.TAG, "Preparing to record. ");
    this.recordVideoTask = new VideoRecorderAsyncTask();
    this.recordVideoTask.setContext(this);
    this.recordVideoTask.setParentUI(this);
    this.recordVideoTask.setHolder(holder);
    if (this.D)
      Log.d(this.TAG, "Telling recorder asyc to execute. ");
    this.recordVideoTask.execute();

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }

}
