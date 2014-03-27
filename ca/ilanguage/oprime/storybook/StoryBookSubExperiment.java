/*
   Copyright 2011 Harri Smått

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package ca.ilanguage.oprime.storybook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.model.Stimulus;
import ca.ilanguage.oprime.model.Touch;

public class StoryBookSubExperiment extends Activity {

  /**
   * Bitmap provider.
   */
  private class BitmapProvider implements CurlView.BitmapProvider {

    @Override
    public Bitmap getBitmap(int width, int height, int index) {

      Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      b.eraseColor(0xFFFFFFFF);
      Canvas c = new Canvas(b);
      Drawable d = StoryBookSubExperiment.this.getResources().getDrawable(
          StoryBookSubExperiment.this.mStimuli.get(index).getImageFileId());

      int margin = StoryBookSubExperiment.this.mBorderSize;
      int border = StoryBookSubExperiment.this.mBorderSize;
      Rect r = new Rect(margin, margin, width - margin, height - margin);

      int imageWidth = r.width() - (border * 2);
      int imageHeight = imageWidth * d.getIntrinsicHeight() / d.getIntrinsicWidth();
      if (imageHeight > r.height() - (border * 2)) {
        imageHeight = r.height() - (border * 2);
        imageWidth = imageHeight * d.getIntrinsicWidth() / d.getIntrinsicHeight();
      }

      r.left += ((r.width() - imageWidth) / 2) - border;
      r.right = r.left + imageWidth + border + border;
      r.top += ((r.height() - imageHeight) / 2) - border;
      r.bottom = r.top + imageHeight + border + border;

      // Paint p = new Paint();
      // p.setColor(0xFFC0C0C0);
      // c.drawRect(r, p);
      // p.setColor(0xFF0000C0);
      // c.drawText(""+mStimuli.get(index).getLabel(), 50, 40, p);

      r.left += border;
      r.right -= border;
      r.top += border;
      r.bottom -= border;

      d.setBounds(r);
      d.draw(c);

      return b;
    }

    @Override
    public int getBitmapCount() {
      return StoryBookSubExperiment.this.mStimuli.size();
    }

    @Override
    public void playAudioStimuli() {
      if (StoryBookSubExperiment.this.mCurrentStimuliIndex >= StoryBookSubExperiment.this.mStimuli.size()) {
        return;
      }
      int audioStimuliResource = StoryBookSubExperiment.this.mStimuli.get(
          StoryBookSubExperiment.this.mCurrentStimuliIndex).getImageFileId();
      try {
        Thread.sleep(StoryBookSubExperiment.this.mDelayAudioMilisecondsAfterImageStimuli);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      MediaPlayer mediaPlayer = MediaPlayer.create(StoryBookSubExperiment.this.getApplicationContext(),
          audioStimuliResource);
      if (mediaPlayer == null) {
        Log.d("OPrime", "Problem opening the audio stimuli");
        return;
      }
      try {
        mediaPlayer.prepare();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      mediaPlayer.start();
      StoryBookSubExperiment.this.mCurrentStimuliIndex++;
    }

    @Override
    public void playSound() {
      MediaPlayer mediaPlayer = MediaPlayer.create(StoryBookSubExperiment.this.getApplicationContext(), R.raw.ploep);
      try {
        mediaPlayer.prepare();
      } catch (IllegalStateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      mediaPlayer.start();
    }

    @Override
    public void recordTouchPoint(Touch touch, int stimuli) {
      StoryBookSubExperiment.this.mStimuli.get(stimuli).touches.add(touch);
      // Toast.makeText(getApplicationContext(), touch.x + ":" + touch.y,
      // Toast.LENGTH_LONG).show();
    }
  }

  /**
   * CurlView size changed observer.
   */
  private class SizeChangedObserver implements CurlView.SizeChangedObserver {
    @Override
    public void onSizeChanged(int w, int h) {
      if (w > h && StoryBookSubExperiment.this.mShowTwoPageBook) {
        StoryBookSubExperiment.this.mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
        StoryBookSubExperiment.this.mCurlView.setMargins(.1f, .05f, .1f, .05f);
      } else {
        StoryBookSubExperiment.this.mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
        StoryBookSubExperiment.this.mCurlView.setMargins(.1f, .1f, .1f, .1f);
      }
      StoryBookSubExperiment.this.mCurlView.setMargins(.0f, .0f, .0f, .0f);

    }
  }

  private Locale              language;
  private int                 mBorderSize                             = 0;
  private CurlView            mCurlView;
  int                         mCurrentStimuliIndex                    = 0;
  protected int               mDelayAudioMilisecondsAfterImageStimuli = 1000;

  private Boolean             mShowTwoPageBook                        = false;

  private ArrayList<Stimulus> mStimuli;

  /**
   * Forces the locale for the duration of the app to the language needed for
   * that version of the Bilingual Aphasia Test
   * 
   * @param lang
   * @return
   */
  public String forceLocale(String lang) {
    if (lang.equals(Locale.getDefault().getLanguage())) {
      this.language = Locale.getDefault();
      return Locale.getDefault().getDisplayLanguage();
    }
    Configuration config = this.getBaseContext().getResources().getConfiguration();
    Locale locale = new Locale(lang);
    Locale.setDefault(locale);
    config.locale = locale;
    this.getBaseContext().getResources()
        .updateConfiguration(config, this.getBaseContext().getResources().getDisplayMetrics());
    this.language = Locale.getDefault();

    return Locale.getDefault().getDisplayLanguage();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.fragment_page_curl);
    /*
     * Prepare Stimuli
     */
    ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
    ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
    this.mStimuli = (ArrayList<Stimulus>) this.getIntent().getExtras().getSerializable(Config.EXTRA_STIMULI);
    this.mShowTwoPageBook = this.getIntent().getExtras().getBoolean(Config.EXTRA_TWO_PAGE_STORYBOOK, false);
    if (this.mStimuli == null) {
      this.mStimuli = ids;
    }
    /*
     * Prepare language of Stimuli
     */
    String lang = this.getIntent().getExtras().getString(Config.EXTRA_LANGUAGE);
    if (lang == null) {
      lang = Config.ENGLISH;
    }
    this.forceLocale(lang);

    int index = 0;
    if (this.getLastNonConfigurationInstance() != null) {
      index = (Integer) this.getLastNonConfigurationInstance();
    }
    this.mCurlView = (CurlView) this.findViewById(R.id.curl);
    this.mCurlView.setBitmapProvider(new BitmapProvider());
    this.mCurlView.setSizeChangedObserver(new SizeChangedObserver());
    if (this.mShowTwoPageBook) {
      this.mCurlView.setCurrentIndex(index + 1);
    } else {
      this.mCurlView.setCurrentIndex(index);
    }
    this.mCurlView.setBackgroundColor(0xFF202830);
    this.mCurlView.setMargins(.0f, .0f, .0f, .0f);

    /*
     * Set 1 or 2 page view mode
     */
    if (this.mShowTwoPageBook) {
      this.mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
      this.mCurlView.setRenderLeftPage(true);
    } else {
      this.mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
      this.mCurlView.setRenderLeftPage(false);

    }

    // This is something somewhat experimental. Before uncommenting next
    // line, please see method comments in CurlView.
    // mCurlView.setEnableTouchPressure(true);
  }

  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();

  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Intent intent = new Intent(Config.INTENT_FINISHED_SUB_EXPERIMENT);
      intent.putExtra(Config.EXTRA_STIMULI, this.mStimuli);
      this.setResult(Config.CODE_EXPERIMENT_COMPLETED, intent);
      this.finish();
    }
    return super.onKeyDown(keyCode, event);

  }

  @Override
  public void onPause() {
    super.onPause();
    this.mCurlView.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    this.mCurlView.onResume();
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    return this.mCurlView.getCurrentIndex();
  }

}