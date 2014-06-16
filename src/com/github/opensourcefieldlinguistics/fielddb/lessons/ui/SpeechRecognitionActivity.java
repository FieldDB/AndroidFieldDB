package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SpeechRecognitionActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_speech_recognition);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(DatumDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(DatumDetailFragment.ARG_ITEM_ID));
			DatumSpeechRecognitionHypothesesFragment fragment = new DatumSpeechRecognitionHypothesesFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.datum_detail_container, fragment).commit();
		}
	}
}
