package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.service.PocketSphinxRecognitionService;
import com.github.opensourcefieldlinguistics.fielddb.service.UploadAudioVideoService;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

public class DatumSpeechRecognitionHypothesesFragment extends
        DatumProductionExperimentFragment {

    private boolean mHasRecognized;
    private boolean mIsRecognizing;
    private boolean mPerfectMatch;
    private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;
    EditText orthographyEditText;
    EditText hypothesis1EditText;
    EditText hypothesis2EditText;
    EditText hypothesis3EditText;
    EditText hypothesis4EditText;
    EditText hypothesis5EditText;
    TableLayout hypothesesArea;
    protected long WAIT_TO_RECORD_AFTER_PROMPT_START = 10;

    private static final String[] TAGS = new String[] { "WebSearch", "SMS",
            "EMail" };
    private RecognitionReceiver mRecognitionReceiver;

    private HashMap<String, Integer> captions;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions_datum_speech_recognition, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
        case R.id.action_speak:
            playSpeechRecognitionPrompt();
            return true;
        case R.id.action_delete:
            return this.delete();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_datum_speech_recognition_hypotheses,
                container, false);

        captions = new HashMap<String, Integer>();
        captions.put(PocketSphinxRecognitionService.KWS_SEARCH,
                R.string.kws_caption);
        captions.put(PocketSphinxRecognitionService.MENU_SEARCH,
                R.string.menu_caption);
        captions.put(PocketSphinxRecognitionService.DIGITS_SEARCH,
                R.string.digits_caption);
        captions.put(PocketSphinxRecognitionService.FORECAST_SEARCH,
                R.string.forecast_caption);

        if (mItem != null) {
            this.prepareEditTextListeners(rootView);
            this.playSpeechRecognitionPrompt();
            if (this.mRecognitionReceiver == null) {
                this.mRecognitionReceiver = new RecognitionReceiver();
            }

            IntentFilter intentPartialRecognitionResult = new IntentFilter(
                    Config.INTENT_PARTIAL_SPEECH_RECOGNITION_RESULT);
            getActivity().registerReceiver(this.mRecognitionReceiver,
                    intentPartialRecognitionResult);

        }

        return rootView;
    }

    protected void showOrthographyOnly(View rootView) {
        if (mHasRecognized == false) {
            return;
        }
        turnOffRecorder(null);
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

        if (orthographyEditText != null) {
            orthographyEditText.setText(mItem.getOrthography());
            int textLength = mItem.getOrthography().length();
            if (this.mPerfectMatch) {
                orthographyEditText.setSelection(0, textLength);
            } else {
                orthographyEditText.setSelection(textLength, textLength);
            }
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

        orthographyEditText = ((EditText) rootView
                .findViewById(R.id.orthography));

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
                    showOrthographyOnly(rootView);
                    ContentValues values = new ContentValues();
                    values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
                    getActivity().getContentResolver().update(mUri, values,
                            null, null);
                    recordUserEvent("editDatum", "hypothesis1");
                }
            });
            // hypothesis1EditText
            // .setOnFocusChangeListener(new OnFocusChangeListener() {
            // @Override
            // public void onFocusChange(View v, boolean hasFocus) {
            // if (!hasFocus) {
            // return;
            // }
            // showOrthographyOnly(rootView);
            // String currentText = hypothesis1EditText.getText()
            // .toString();
            // mItem.setOrthography(currentText);
            // }
            // });
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
            // hypothesis2EditText
            // .setOnFocusChangeListener(new OnFocusChangeListener() {
            // @Override
            // public void onFocusChange(View v, boolean hasFocus) {
            // if (!hasFocus) {
            // return;
            // }
            // showOrthographyOnly(rootView);
            // String currentText = hypothesis2EditText.getText()
            // .toString();
            // mItem.setOrthography(currentText);
            // }
            // });
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
            // hypothesis3EditText
            // .setOnFocusChangeListener(new OnFocusChangeListener() {
            // @Override
            // public void onFocusChange(View v, boolean hasFocus) {
            // if (!hasFocus) {
            // return;
            // }
            // showOrthographyOnly(rootView);
            // String currentText = hypothesis3EditText.getText()
            // .toString();
            // mItem.setOrthography(currentText);
            // }
            // });
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
            // hypothesis4EditText
            // .setOnFocusChangeListener(new OnFocusChangeListener() {
            // @Override
            // public void onFocusChange(View v, boolean hasFocus) {
            // if (!hasFocus) {
            // return;
            // }
            // showOrthographyOnly(rootView);
            // String currentText = hypothesis4EditText.getText()
            // .toString();
            // mItem.setOrthography(currentText);
            // }
            // });
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
            // hypothesis5EditText
            // .setOnFocusChangeListener(new OnFocusChangeListener() {
            // @Override
            // public void onFocusChange(View v, boolean hasFocus) {
            // if (!hasFocus) {
            // return;
            // }
            // showOrthographyOnly(rootView);
            // String currentText = hypothesis5EditText.getText()
            // .toString();
            // mItem.setOrthography(currentText);
            // }
            // });
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
    }

    @Override
    public boolean toggleAudioRecording(MenuItem item) {
        if (!this.mRecordingAudio) {
            this.mRecordingAudio = true;

            String audioFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
                    + mItem.getBaseFilename() + Config.DEFAULT_AUDIO_EXTENSION;
            this.mAudioFileName = audioFileName;

            String caption = getResources().getString(
                    captions.get(PocketSphinxRecognitionService.KWS_SEARCH));
            hypothesis5EditText.setText(caption);

            hypothesis5EditText.setText(getString(R.string.im_listening));

            Intent intent = new Intent(getActivity(),
                    PocketSphinxRecognitionService.class);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.im_listening));
            getActivity().startService(intent);

//            Handler mainHandler = new Handler(getActivity().getMainLooper());
//            Runnable myRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    Intent recognize = new Intent(getActivity(),
//                            PocketSphinxRecognitionService.class);
//                    getActivity().stopService(recognize);
//                }
//            };
            // mainHandler.postDelayed(myRunnable, 2000);

            mItem.addAudioFile(audioFileName.replace(
                    Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
            ContentValues values = new ContentValues();
            values.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES,
                    mItem.getMediaFilesAsCSV(mItem.getAudioVideoFiles()));
            getActivity().getContentResolver().update(mUri, values, null, null);
            Log.d(Config.TAG, "Recording audio " + audioFileName);
            if (item != null) {
                item.setIcon(R.drawable.ic_action_stop);
            }
            this.recordUserEvent("recognizeSpeech", audioFileName);

            if (mSpeechRecognizerFeedback != null) {
                mSpeechRecognizerFeedback
                        .setImageResource(R.drawable.speech_recognizer_recognizing);
            }

            if (mSpeechRecognizerInstructions != null) {
                mSpeechRecognizerInstructions.setText("Tap to end");
            }

        } else {
            this.turnOffRecorder(item);
        }
        return true;
    }

    @Override
    protected boolean turnOffRecorder(MenuItem item) {
        if (mAudioFileName == null) {
            return false;
        }
        Intent recognize = new Intent(getActivity(),
                PocketSphinxRecognitionService.class);
        getActivity().stopService(recognize);

        Handler mainHandler = new Handler(getActivity().getMainLooper());
        Runnable launchUploadAudioService = new Runnable() {

            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                Intent uploadAudioFile = new Intent(getActivity(),
                        UploadAudioVideoService.class);
                uploadAudioFile.setData(Uri.parse(mAudioFileName));
                uploadAudioFile.putExtra(Config.EXTRA_PARTICIPANT_ID,
                        Config.CURRENT_USERNAME);
                uploadAudioFile.putExtra(
                        Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION,
                        mDeviceDetails.getCurrentDeviceDetails());
                getActivity().startService(uploadAudioFile);
            }
        };
        mainHandler.postDelayed(launchUploadAudioService, 1000);

        this.mRecordingAudio = false;
        if (item != null) {
            item.setIcon(R.drawable.ic_action_mic);
        }
        this.recordUserEvent("stopAudio", this.mAudioFileName);

        if (mSpeechRecognizerFeedback != null) {
            mSpeechRecognizerFeedback
                    .setImageResource(R.drawable.speech_recognizer_waiting);
        }

        if (mSpeechRecognizerInstructions != null) {
            mSpeechRecognizerInstructions.setText("Tap to speak again");
        }
        if (Config.APP_TYPE.equals("speechrecognition")) {
            autoAdvanceAfterRecordingAudio();
        }
        return true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible() && !this.mIsRecognizing) {
            // playSpeechRecognitionPrompt();
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
            processRecognitionPartialHypothesis(data);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void processRecognitionPartialHypothesis(Intent data) {
        hypothesesArea.setVisibility(View.VISIBLE);
        ArrayList<String> matches = data
                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        if (hypothesis1EditText != null) {
            if (matches.size() > 0 && matches.get(0) != null) {
                hypothesis1EditText.setText(matches.get(0));
                this.mHasRecognized = true;
            } else {
                // hypothesis1EditText.setVisibility(View.GONE);
            }
            // hypothesis1EditText.clearFocus();
        }
        recordUserEvent("recognizedPartialHypotheses", matches.toString());
    }

    public void processRecognitionHypotheses(Intent data) {
        turnOffRecorder(null);
        hypothesesArea.setVisibility(View.VISIBLE);
        /*
         * Populate the wordsList with the String values the recognition engine
         * thought it heard, and then Toast them to the user and say them out
         * loud.
         */
        ArrayList<String> matches = data
                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        if (hypothesis1EditText != null) {
            if (matches.size() > 0 && matches.get(0) != null
                    && !"".equals(matches.get(0))) {
                hypothesis1EditText.setText(matches.get(0));
            } else {
                // hypothesis1EditText.setVisibility(View.GONE);
            }
            // hypothesis1EditText.clearFocus();
        } else {
            Log.w(Config.TAG,
                    "hypothesis1EditText is null, cant show user results!");
        }
        if (hypothesis2EditText != null) {
            if (matches.size() > 1 && matches.get(1) != null
                    && !"".equals(matches.get(1))) {
                hypothesis2EditText.setText(matches.get(1));
            } else {
                // hypothesis2EditText.setVisibility(View.GONE);
            }
            // hypothesis2EditText.clearFocus();
        }
        if (hypothesis3EditText != null) {
            if (matches.size() > 2 && matches.get(2) != null
                    && !"".equals(matches.get(2))) {
                hypothesis3EditText.setText(matches.get(2));
            } else {
                // hypothesis3EditText.setVisibility(View.GONE);
            }
            // hypothesis3EditText.clearFocus();
        }
        if (hypothesis4EditText != null) {
            if (matches.size() > 3 && matches.get(3) != null
                    && !"".equals(matches.get(3))) {
                hypothesis4EditText.setText(matches.get(3));
            } else {
                // hypothesis4EditText.setVisibility(View.GONE);
            }
            // hypothesis4EditText.clearFocus();
        }
        if (hypothesis5EditText != null) {
            if (matches.size() > 4 && matches.get(4) != null
                    && !"".equals(matches.get(4))) {
                hypothesis5EditText.setText(matches.get(4));
            } else {
                // hypothesis5EditText.setVisibility(View.GONE);
            }
            // hypothesis5EditText.clearFocus();
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
            this.mPerfectMatch = true;
        } else if (matches.size() > 1) {
            this.mPerfectMatch = false;
        }
        Log.d(Config.TAG, "showing hypotheses: " + matches.toString());
        recordUserEvent("recognizedHypotheses", matches.toString());
    }

    @Override
    public void onPause() {
        turnOffRecorder(null);
        // if (this.mRecognitionReceiver != null && getActivity() != null) {
        // getActivity().unregisterReceiver(this.mRecognitionReceiver);
        // }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (this.mRecognitionReceiver != null && getActivity() != null) {
            getActivity().unregisterReceiver(this.mRecognitionReceiver);
        }
        super.onDestroy();

    }

    public class RecognitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(Config.EXTRA_RECOGNITION_COMPLETED,
                    false)) {
                processRecognitionHypotheses(intent);
            } else {
                processRecognitionPartialHypothesis(intent);
            }
        }

    }

}
