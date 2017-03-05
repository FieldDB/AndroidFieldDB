package com.github.fielddb.storybook.ui;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.briangriffey.notebook.PageTurnPagerAdapter;
import com.github.fielddb.model.Stimulus;
import com.github.fielddb.R;

public class StoryBookStimuliPagerAdapter extends PageTurnPagerAdapter {
  private ArrayList<Stimulus> mStimuli;
  private Context mContext;

  public StoryBookStimuliPagerAdapter(FragmentManager fm) {
    super(fm);
    // TODO Auto-generated constructor stub
  }

  public ArrayList<Stimulus> getStimuli() {
    return mStimuli;
  }

  public void setStimuli(ArrayList<Stimulus> mStimuli) {
    this.mStimuli = mStimuli;
  }

  @Override
  public Fragment getItem(int i) {
    Fragment fragment = new StimulusPageTurnFragment();
    Bundle args = new Bundle();
    if (this.mStimuli != null && this.mStimuli.size() > i) {
      args.putSerializable(StimulusPageTurnFragment.ARG_STIMULUS, this.mStimuli.get(i));
      args.putInt(StimulusPageTurnFragment.ARG_STIMULUS_INDEX, i);
      if (i == this.mStimuli.size() - 1) {
        args.putBoolean(StimulusPageTurnFragment.ARG_STIMULUS_LAST, true);
      } else {
        args.putBoolean(StimulusPageTurnFragment.ARG_STIMULUS_LAST, false);
      }
      fragment.setArguments(args);

    }
    return fragment;
  }

  @Override
  public int getCount() {
    if (this.mStimuli != null) {
      return this.mStimuli.size();
    } else {
      return 0;
    }
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return "OBJECT " + (position + 1);
  }
}
