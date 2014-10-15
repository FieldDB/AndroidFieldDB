package com.github.opensourcefieldlinguistics.fielddb.storybook.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.opensourcefieldlinguistics.fielddb.R;
import com.github.opensourcefieldlinguistics.fielddb.model.Stimulus;

public class StimulusPageTurnFragment extends Fragment {
  public static final String ARG_STIMULUS = "stimulus";
  public static final String ARG_STIMULUS_INDEX = "stimulus_index";
  protected Stimulus mStimulus;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // The last two arguments ensure LayoutParams are inflated
    // properly.
    View rootView = inflater.inflate(R.layout.fragment_page_detail, container, false);
    Bundle args = getArguments();

    mStimulus = (Stimulus) args.getSerializable(ARG_STIMULUS);
    ImageView image = (ImageView) rootView.findViewById(R.id.onlyimage);
    Drawable d = this.getResources().getDrawable(mStimulus.getImageFileId());
    image.setImageDrawable(d);
    int itemNumber = args.getInt(ARG_STIMULUS_INDEX) + 1;
    String itemString = "";
    if (itemNumber < 4) {
      itemString = "Practique " + itemNumber;
    } else {
      itemString = "Item " + itemNumber;
    }
    ((TextView) rootView.findViewById(R.id.item_number)).setText(itemString);
    return rootView;
  }
}
