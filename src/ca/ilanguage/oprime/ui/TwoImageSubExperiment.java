package ca.ilanguage.oprime.ui;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.model.TwoImageStimulus;

public class TwoImageSubExperiment extends SubExperiment {

  @Override
  public void initalizeLayout() {
    this.mStimuliIndex = -1;
    this.setContentView(R.layout.fragment_two_images);

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

  @Override
  public void loadDefaults() {
    ArrayList<TwoImageStimulus> ids = new ArrayList<TwoImageStimulus>();
    ids.add(new TwoImageStimulus(R.drawable.androids_experimenter_kids));
    this.mStimuli = ids;
  }

  @Override
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

    try {
      TextView t = (TextView) this.findViewById(R.id.stimuli_number2);
      String displayStimuliLabel = this.mStimuli.get(this.mStimuliIndex).getLabel();
      if ("".equals(displayStimuliLabel)) {
        int stimnumber = this.mStimuliIndex + 1;
        int stimtotal = this.mStimuli.size();
        displayStimuliLabel = stimnumber + "/" + stimtotal;
      }
      t.setText(displayStimuliLabel);

      ImageView image = (ImageView) this.findViewById(R.id.leftimage);
      Drawable d = this.getResources().getDrawable(
          ((TwoImageStimulus) this.mStimuli.get(this.mStimuliIndex)).getLeftImageFileId());
      image.setImageDrawable(d);
      image.startAnimation(this.animationSlideInRight);

      ImageView rightimage = (ImageView) this.findViewById(R.id.rightimage);
      d = this.getResources().getDrawable(
          ((TwoImageStimulus) this.mStimuli.get(this.mStimuliIndex)).getRightImageFileId());
      rightimage.setImageDrawable(d);
      rightimage.startAnimation(this.animationSlideInRight);
      this.mStimuli.get(this.mStimuliIndex).setStartTime(System.currentTimeMillis());

    } catch (Exception e) {
      Log.e(this.TAG, "Error getting images out." + e.getMessage());

    }
    this.playAudioStimuli();

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public void previousStimuli() {
    this.mStimuliIndex -= 1;

    if (this.mStimuliIndex < 0) {
      this.mStimuliIndex = 0;
      return;
    }
    try {
      TextView t = (TextView) this.findViewById(R.id.stimuli_number2);
      String displayStimuliLabel = this.mStimuli.get(this.mStimuliIndex).getLabel();
      if ("".equals(displayStimuliLabel)) {
        int stimnumber = this.mStimuliIndex + 1;
        int stimtotal = this.mStimuli.size();
        displayStimuliLabel = stimnumber + "/" + stimtotal;
      }
      t.setText(displayStimuliLabel);

      ImageView image = (ImageView) this.findViewById(R.id.leftimage);
      Drawable d = this.getResources().getDrawable(
          ((TwoImageStimulus) this.mStimuli.get(this.mStimuliIndex)).getLeftImageFileId());
      image.setImageDrawable(d);

      ImageView rightimage = (ImageView) this.findViewById(R.id.rightimage);
      d = this.getResources().getDrawable(
          ((TwoImageStimulus) this.mStimuli.get(this.mStimuliIndex)).getRightImageFileId());
      rightimage.setImageDrawable(d);
    } catch (Exception e) {
      Log.e(this.TAG, "Error getting images out." + e.getMessage());
    }
    this.playAudioStimuli();
  }
}
