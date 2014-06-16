package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

public class DatumSpeechRecognitionHypothesesFragment extends DatumDetailFragment {

	private boolean isRecognizing;
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// no menu
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_production_stimulus,
				container, false);

		if (mItem != null) {

		}

		return rootView;
	}
	public void playSpeechRecognitionPrompt(){
		this.isRecognizing = true;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (this.isVisible() && !this.isRecognizing) {
			playSpeechRecognitionPrompt();
		}
	}

}
