package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import org.acra.ACRA;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.BuildConfig;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

import android.content.ContentValues;
import android.net.Uri;
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
			ContentValues values = new ContentValues();
			values.put(DatumTable.COLUMN_VALIDATION_STATUS,
					"ToBeChecked,AutomaticallyRecognized");
			Uri newDatum = this.getContentResolver().insert(
					DatumContentProvider.CONTENT_URI, values);
			if (newDatum == null) {
				if (!BuildConfig.DEBUG)
					ACRA.getErrorReporter()
							.handleException(
									new Exception(
											"*** Error inserting a speech recognition datum in DB ***"));
			}
			Bundle arguments = new Bundle();
			arguments.putString(DatumDetailFragment.ARG_ITEM_ID,
					newDatum.getLastPathSegment());
			DatumSpeechRecognitionHypothesesFragment fragment = new DatumSpeechRecognitionHypothesesFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.datum_detail_container, fragment).commit();
		}
	}
}
