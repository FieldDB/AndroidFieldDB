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

package com.github.opensourcefieldlinguistics.fielddb.storybook.ui;

import java.util.ArrayList;
import java.util.Locale;

import com.briangriffey.notebook.PageTurnPageTransformer;
import com.github.opensourcefieldlinguistics.datacollection.VideoRecorder;
import com.github.opensourcefieldlinguistics.fielddb.Config;
import com.github.opensourcefieldlinguistics.fielddb.model.Stimulus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import com.github.opensourcefieldlinguistics.fielddb.R;

public class StoryBookSubExperiment extends VideoRecorder {
  StoryBookStimuliPagerAdapter mPagerAdapter;
  ViewPager mViewPager;

  private Locale language;

  private Boolean mShowTwoPageBook = false;

  private ArrayList<Stimulus> mStimuli;

  /**
   * Forces the locale for the duration of the app to the language needed for
   * that version of the Test
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
    // this.setContentView(R.layout.fragment_page_curl);
    this.mLayout = R.layout.fragment_storybook;
    super.onCreate(savedInstanceState);
    /*
     * Prepare Stimuli
     */
    ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
    ids.add(new Stimulus(R.drawable.speech_bubbles, R.raw.recording_start));
    this.mStimuli = (ArrayList<Stimulus>) this.getIntent().getSerializableExtra(Config.EXTRA_STIMULI);
    this.mShowTwoPageBook = this.getIntent().getBooleanExtra(Config.EXTRA_TWO_PAGE_STORYBOOK, false);
    if (this.mStimuli == null) {
      this.mStimuli = ids;
    }

    /*
     * Prepare language of Stimuli
     */
    String lang = this.getIntent().getStringExtra(Config.EXTRA_LANGUAGE);
    if (lang == null || "".equals(lang)) {
      lang = Config.DEFAULT_LANGUAGE;
    }
    if (Locale.getDefault().getLanguage() != lang) {
      this.forceLocale(lang);
    }

    int index = 0;
    if (this.getLastNonConfigurationInstance() != null) {
      index = (Integer) this.getLastNonConfigurationInstance();
    }
    // this.mStimuli.add(new Stimulus(android.R.drawable.ic_media_previous,
    // R.raw.recording_start));
    if (this.mShowTwoPageBook) {
      if (this.mStimuli.size() % 2 == 1) {
        this.mStimuli.add(new Stimulus(R.drawable.speech_bubbles, R.raw.recording_start));
      }
    }
    /*
     * Set 1 or 2 page view mode
     */
    if (this.mShowTwoPageBook) {
    } else {

    }
    // ViewPager and its adapters use support library
    // fragments, so use getSupportFragmentManager.
    mPagerAdapter = new StoryBookStimuliPagerAdapter(getSupportFragmentManager());
    mPagerAdapter.setStimuli(this.mStimuli);
    mViewPager = (ViewPager) findViewById(R.id.pager);
    PageTurnPageTransformer pageTransformer = new PageTurnPageTransformer();
    pageTransformer.setContext(this);
    mViewPager.setPageTransformer(true, pageTransformer);
    mViewPager.setAdapter(mPagerAdapter);
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
  }

  @Override
  public void onResume() {
    super.onResume();
  }

}