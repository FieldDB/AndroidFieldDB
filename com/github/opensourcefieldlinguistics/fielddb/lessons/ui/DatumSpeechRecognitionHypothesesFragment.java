package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.Toast;

import com.github.opensourcefieldlinguistics.fielddb.FieldDBApplication;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

public class DatumSpeechRecognitionHypothesesFragment
		extends
			DatumProductionExperimentFragment {

	private boolean mHasRecognized;
	private boolean mIsRecognizing;
	private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;
	EditText hypothesis1EditText;
	EditText hypothesis2EditText;
	EditText hypothesis3EditText;
	EditText hypothesis4EditText;
	EditText hypothesis5EditText;
	TableLayout hypothesesArea;

	private static final String[] TAGS = new String[]{"WebSearch", "SMS",
			"EMail"};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actions_datum_speech_recognition, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_speak :
				playSpeechRecognitionPrompt();
				return true;
			case R.id.action_delete :
				return this.delete();
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_datum_speech_recognition_hypotheses,
				container, false);

		if (mItem != null) {
			this.prepareEditTextListeners(rootView);
			playSpeechRecognitionPrompt();
		}

		return rootView;
	}

	protected void showOrthographyOnly(View rootView) {
		if (mHasRecognized == false) {
			return;
		}
		TableLayout datumArea = (TableLayout) rootView
				.findViewById(R.id.datumArea);
		if (datumArea != null) {
			datumArea.setVisibility(View.VISIBLE);
		}

		if (hypothesesArea != null) {
			hypothesesArea.setVisibility(View.GONE);
		}

		final EditText contextEditText = ((EditText) rootView
				.findViewById(R.id.context));
		if (contextEditText != null) {
			contextEditText.setText(mItem.getContext());
			contextEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = contextEditText.getText().toString();
					mItem.setContext(currentText);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_CONTEXT, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "context");
				}
			});
		}

		final AutoCompleteTextView tagsEditText = ((AutoCompleteTextView) rootView
				.findViewById(R.id.tags));
		if (tagsEditText != null) {
			ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_dropdown_item_1line,
					TAGS);
			tagsEditText.setAdapter(tagsAdapter);
			tagsEditText.setText(mItem.getTagsString());
			tagsEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = tagsEditText.getText().toString();
					mItem.setTagsFromSting(currentText);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_TAGS, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "tags");
				}
			});
		}

		final EditText orthographyEditText = ((EditText) rootView
				.findViewById(R.id.orthography));
		if (orthographyEditText != null) {
			orthographyEditText.setText(mItem.getOrthography());
			int textLength = mItem.getOrthography().length();
			orthographyEditText.setSelection(textLength, textLength);
			orthographyEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = orthographyEditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					contextEditText.setVisibility(View.VISIBLE);
					tagsEditText.setVisibility(View.VISIBLE);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "orthography");
				}
			});
		}
	}

	protected void prepareEditTextListeners(final View rootView) {
		hypothesesArea = (TableLayout) rootView
				.findViewById(R.id.hypothesesArea);

		hypothesis1EditText = ((EditText) rootView
				.findViewById(R.id.hypothesis1));
		if (hypothesis1EditText != null) {
			// hypothesis1EditText.setText(mItem.getOrthography());
			hypothesis1EditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (mHasRecognized == false) {
						return;
					}
					String currentText = hypothesis1EditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "hypothesis1");
				}
			});
			hypothesis1EditText
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								return;
							}
							showOrthographyOnly(rootView);
							String currentText = hypothesis1EditText.getText()
									.toString();
							mItem.setOrthography(currentText);
						}
					});
		}

		final ImageButton removeHypothesis1Button = (ImageButton) rootView
				.findViewById(R.id.removeHypothesis1);
		if (removeHypothesis1Button != null) {
			removeHypothesis1Button
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeHypothesis1Button.setVisibility(View.GONE);
							hypothesis1EditText.setVisibility(View.GONE);
							recordUserEvent("removeHypothesis",
									"hypothesis1:::"
											+ hypothesis1EditText.getText()
													.toString());

						}
					});
		}

		hypothesis2EditText = ((EditText) rootView
				.findViewById(R.id.hypothesis2));
		if (hypothesis2EditText != null) {
			// hypothesis2EditText.setText(mItem.getOrthography());
			hypothesis2EditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (mHasRecognized == false) {
						return;
					}
					String currentText = hypothesis2EditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					showOrthographyOnly(rootView);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "hypothesis2");
				}
			});
			hypothesis2EditText
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								return;
							}
							showOrthographyOnly(rootView);
							String currentText = hypothesis2EditText.getText()
									.toString();
							mItem.setOrthography(currentText);
						}
					});
		}

		final ImageButton removeHypothesis2Button = (ImageButton) rootView
				.findViewById(R.id.removeHypothesis2);
		if (removeHypothesis2Button != null) {
			removeHypothesis2Button
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeHypothesis2Button.setVisibility(View.GONE);
							hypothesis2EditText.setVisibility(View.GONE);
							recordUserEvent("removeHypothesis",
									"hypothesis2:::"
											+ hypothesis2EditText.getText()
													.toString());

						}
					});
		}

		hypothesis3EditText = ((EditText) rootView
				.findViewById(R.id.hypothesis3));
		if (hypothesis3EditText != null) {
			// hypothesis3EditText.setText(mItem.getOrthography());
			hypothesis3EditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (mHasRecognized == false) {
						return;
					}
					String currentText = hypothesis3EditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					showOrthographyOnly(rootView);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "hypothesis3");
				}
			});
			hypothesis3EditText
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								return;
							}
							showOrthographyOnly(rootView);
							String currentText = hypothesis3EditText.getText()
									.toString();
							mItem.setOrthography(currentText);
						}
					});
		}

		final ImageButton removeHypothesis3Button = (ImageButton) rootView
				.findViewById(R.id.removeHypothesis3);
		if (removeHypothesis3Button != null) {
			removeHypothesis3Button
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeHypothesis3Button.setVisibility(View.GONE);
							hypothesis3EditText.setVisibility(View.GONE);
							recordUserEvent("removeHypothesis",
									"hypothesis3:::"
											+ hypothesis3EditText.getText()
													.toString());

						}
					});
		}

		hypothesis4EditText = ((EditText) rootView
				.findViewById(R.id.hypothesis4));
		if (hypothesis4EditText != null) {
			// hypothesis4EditText.setText(mItem.getOrthography());
			hypothesis4EditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (mHasRecognized == false) {
						return;
					}
					String currentText = hypothesis4EditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					showOrthographyOnly(rootView);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "hypothesis4");
				}
			});
			hypothesis4EditText
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								return;
							}
							showOrthographyOnly(rootView);
							String currentText = hypothesis4EditText.getText()
									.toString();
							mItem.setOrthography(currentText);
						}
					});
		}

		final ImageButton removeHypothesis4Button = (ImageButton) rootView
				.findViewById(R.id.removeHypothesis4);
		if (removeHypothesis4Button != null) {
			removeHypothesis4Button
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeHypothesis4Button.setVisibility(View.GONE);
							hypothesis4EditText.setVisibility(View.GONE);
							recordUserEvent("removeHypothesis",
									"hypothesis4:::"
											+ hypothesis4EditText.getText()
													.toString());

						}
					});
		}

		hypothesis5EditText = ((EditText) rootView
				.findViewById(R.id.hypothesis5));
		if (hypothesis5EditText != null) {
			// hypothesis5EditText.setText(mItem.getOrthography());
			hypothesis5EditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}
				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					if (mHasRecognized == false) {
						return;
					}
					String currentText = hypothesis5EditText.getText()
							.toString();
					mItem.setOrthography(currentText);
					showOrthographyOnly(rootView);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "hypothesis5");
				}
			});
			hypothesis5EditText
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								return;
							}
							showOrthographyOnly(rootView);
							String currentText = hypothesis5EditText.getText()
									.toString();
							mItem.setOrthography(currentText);
						}
					});
		}

		final ImageButton removeHypothesis5Button = (ImageButton) rootView
				.findViewById(R.id.removeHypothesis5);
		if (removeHypothesis5Button != null) {
			removeHypothesis5Button
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeHypothesis5Button.setVisibility(View.GONE);
							hypothesis5EditText.setVisibility(View.GONE);
							recordUserEvent("removeHypothesis",
									"hypothesis5:::"
											+ hypothesis5EditText.getText()
													.toString());

						}
					});
		}

	}
	public void playSpeechRecognitionPrompt() {
		this.mIsRecognizing = true;
		this.mHasRecognized = false;
		mAudioPromptResource = R.raw.im_listening;
		playPromptContext();
		Handler mainHandler = new Handler(getActivity().getMainLooper());
		Runnable myRunnable = new Runnable() {
			@Override
			public void run() {
				startVoiceRecognitionActivity();
			}
		};
		mainHandler.postDelayed(myRunnable, 200);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (this.isVisible() && !this.mIsRecognizing) {
			// playSpeechRecognitionPrompt();
		}
	}

	/**
	 * Fire an intent to start the voice recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		recordUserEvent("recognizeSpeech", "");
		if (isIntentAvailable(getActivity(),
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) {

			recordUserEvent("recognizeSpeech", "defaultEngine:::"
					+ Locale.getDefault().getDisplayLanguage());

			FieldDBApplication app = (FieldDBApplication) getActivity()
					.getApplication();
			app.forceLocale(Config.DATA_IS_ABOUT_LANGUAGE_ISO);

			Toast.makeText(
					getActivity(),
					"Your voice model is not ready, using the default recognition for your system.",
					Toast.LENGTH_LONG).show();

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
					getString(R.string.im_listening));
			startActivityForResult(intent,
					RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE);
		} else {
			Toast.makeText(
					getActivity(),
					"You have no speech recognition engine installed so we cannot provide the default recognizer for you. You must wait for your model to be downloaded.",
					Toast.LENGTH_LONG).show();

			recordUserEvent("recognizeSpeech", "noRecognizerIntent:::"
					+ Locale.getDefault().getDisplayLanguage());
			Handler mainHandler = new Handler(getActivity().getMainLooper());
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					getActivity().finish();
				}
			};
			mainHandler.postDelayed(myRunnable, 200);
		}

	}

	/**
	 * Handle the results from the voice recognition activity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getActivity();
		if (requestCode == RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			turnOffRecorder(null);
			hypothesesArea.setVisibility(View.VISIBLE);
			/*
			 * Populate the wordsList with the String values the recognition
			 * engine thought it heard, and then Toast them to the user and say
			 * them out loud.
			 */
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (hypothesis1EditText != null) {
				if (matches.size() > 0 && matches.get(0) != null) {
					hypothesis1EditText.setText(matches.get(0));
				} else {
					hypothesis1EditText.setVisibility(View.GONE);
				}
				hypothesis1EditText.clearFocus();
			}
			if (hypothesis2EditText != null) {
				if (matches.size() > 1 && matches.get(1) != null) {
					hypothesis2EditText.setText(matches.get(1));
				} else {
					hypothesis2EditText.setVisibility(View.GONE);
				}
				hypothesis2EditText.clearFocus();
			}
			if (hypothesis3EditText != null) {
				if (matches.size() > 2 && matches.get(2) != null) {
					hypothesis3EditText.setText(matches.get(2));
				} else {
					hypothesis3EditText.setVisibility(View.GONE);
				}
				hypothesis3EditText.clearFocus();
			}
			if (hypothesis4EditText != null) {
				if (matches.size() > 3 && matches.get(3) != null) {
					hypothesis4EditText.setText(matches.get(3));
				} else {
					hypothesis4EditText.setVisibility(View.GONE);
				}
				hypothesis4EditText.clearFocus();
			}
			if (hypothesis5EditText != null) {
				if (matches.size() > 4 && matches.get(4) != null) {
					hypothesis5EditText.setText(matches.get(4));
				} else {
					hypothesis5EditText.setVisibility(View.GONE);
				}
				hypothesis5EditText.clearFocus();
			}

			if (matches.size() > 0) {
				this.mHasRecognized = true;
			} else {
				/* make it possible for the user to create a datum anyway. */
				this.mHasRecognized = true;
			}

			if (matches.size() == 1) {
				// Trigger hypothesis 1 to be the orthography
				hypothesis1EditText.setText(matches.get(0));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		turnOffRecorder(null);
		super.onPause();
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

}
