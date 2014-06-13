package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import ca.ilanguage.oprime.Config;

import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

public class DatumProductionExperimentFragment extends DatumDetailFragment {

	private int mAudioPromptResource;
	private boolean mIsInstructions = false;
	private long WAIT_TO_RECORD_AFTER_PROMPT_START = 300;

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
			this.prepareSpeechRecognitionButton(rootView);
			this.prepareVideoAndImages(rootView);

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
			} else {
				mImageView.setImageResource(R.drawable.instructions);
			}
			String id = mItem.getId();
			Log.d(Config.TAG, "Prompt for this datum will be " + id);
			if ("instructions".equals(id)) {
				this.mIsInstructions = true;
				mAudioPromptResource = R.raw.instructions;
				mImageView.setImageResource(R.drawable.instructions);
				mSpeechRecognizerFeedback.setVisibility(View.GONE);
				mSpeechRecognizerInstructions.setText("Swipe to begin...");
				playPromptContext();
			} else {
				mAudioPromptResource = R.raw.prompt;
			}

		}

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (this.isVisible() && !this.isPlaying) {
			playPromptContext();
		}
	}

	public void onToggleAudioRecording(View view) {
		this.toggleAudioRecording(null);
	}

	protected void playPromptContext() {
		isPlaying = true;

		Log.d(Config.TAG, "Playing prompting context");
		mAudioPlayer = MediaPlayer.create(getActivity(), mAudioPromptResource);
		if (mAudioPlayer != null) {
			mAudioPlayer
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.release();
						}
					});
			mAudioPlayer
					.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

						@Override
						public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
							Log.d(Config.TAG, "Buffering " + arg1);
						}
					});
			mAudioPlayer
					.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							mp.start();
						}
					});

			if (mSpeechRecognizerInstructions != null && !mIsInstructions) {
				mSpeechRecognizerInstructions.setText("Speak after beep");
			}
		}
		/*
		 * begin recording almost immediately so that the user wont speak too
		 * early
		 */
		Handler mainHandler = new Handler(getActivity().getMainLooper());
		Runnable myRunnable = new Runnable() {

			@Override
			public void run() {
				if (!mIsInstructions) {
					toggleAudioRecording(null);
				}
			}
		};
		mainHandler.postDelayed(myRunnable, WAIT_TO_RECORD_AFTER_PROMPT_START);
	}

}
