package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import ca.ilanguage.oprime.Config;

import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

public class DatumProductionExperimentFragment extends DatumDetailFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_production_stimulus,
				container, false);

		if (mItem != null) {

			final TextView orthographyTextView = ((TextView) rootView
					.findViewById(R.id.orthography));
			orthographyTextView.setText(mItem.getOrthography());

			final TextView contextTextView = ((TextView) rootView
					.findViewById(R.id.context));
			contextTextView.setText(mItem.getContext());

			if (mImageView == null) {
				mImageView = (ImageView) rootView.findViewById(R.id.image_view);
			}
			String tags = mItem.getTagsString();
			if (tags.contains("WebSearch")) {
				mImageView.setImageResource(R.drawable.search_selected);
			} else if (tags.contains("LegalSearch")) {
				mImageView.setImageResource(R.drawable.legal_search_selected);
			} else if (tags.contains("SMS")) {
				mImageView.setImageResource(R.drawable.sms_selected);
			}

			this.playPromptContext();
		}

		return rootView;
	}

	protected void playPromptContext() {
		Log.d(Config.TAG, "Playing prompting context");
	}
}
