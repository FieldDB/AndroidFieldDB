package com.github.fielddb.storybook.ui;

import java.io.IOException;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.fielddb.model.Stimulus;
import com.github.fielddb.model.Touch;
import com.github.fielddb.Config;
import com.github.fielddb.R;

public class StimulusPageTurnFragment extends Fragment {
  public static final String ARG_STIMULUS = "stimulus";
  public static final String ARG_STIMULUS_INDEX = "stimulus_index";
  public static final String ARG_STIMULUS_LAST = "stimulus_last";
  protected Stimulus mStimulus;
  protected int mDelayAudioMilisecondsAfterImageStimuli = 0;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // The last two arguments ensure LayoutParams are inflated
    // properly.
    View rootView = inflater.inflate(R.layout.fragment_page_detail, container, false);
    Bundle args = getArguments();

    this.mStimulus = (Stimulus) args.getSerializable(ARG_STIMULUS);
    ImageView image = (ImageView) rootView.findViewById(R.id.onlyimage);
    Drawable d = this.getResources().getDrawable(mStimulus.getImageFileId());
    image.setImageDrawable(d);
    int itemNumber = args.getInt(ARG_STIMULUS_INDEX);
    String itemString = "";
    if (itemNumber == 0) {
      // itemString = "Changer de page pour commencer ";
    } else if (itemNumber < 5) {
      itemString = "Pratique " + itemNumber;
    } else {
      itemNumber = itemNumber - 4;
      itemString = "Item " + itemNumber;
    }
    playAudioStimuli();
    if (args.getBoolean(ARG_STIMULUS_LAST)) {
      itemString = "Utiliser la fl�che de retour pour quitter";
    }

    ((TextView) rootView.findViewById(R.id.item_number)).setText(itemString);
    return rootView;
  }

  public void playAudioStimuli() {

    int audioStimuliResource = this.mStimulus.getAudioFileId();
    if (mDelayAudioMilisecondsAfterImageStimuli > 0) {
      try {
        Thread.sleep(this.mDelayAudioMilisecondsAfterImageStimuli);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
    }
    MediaPlayer mediaPlayer = MediaPlayer.create(this.getActivity(), audioStimuliResource);
    if (mediaPlayer == null) {
      Log.d(Config.TAG, "Problem opening the audio stimuli");
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
  }

  public void playSound() {
    MediaPlayer mediaPlayer = MediaPlayer.create(this.getActivity(), R.raw.recording_end);
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

  public void recordTouchPoint(Touch touch, int stimuli) {
    this.mStimulus.touches.add(touch);
    // Toast.makeText(getApplicationContext(), touch.x + ":" + touch.y,
    // Toast.LENGTH_LONG).show();
  }
}
