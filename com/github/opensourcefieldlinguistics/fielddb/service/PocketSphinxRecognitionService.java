package com.github.opensourcefieldlinguistics.fielddb.service;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognitionService.Callback;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

public class PocketSphinxRecognitionService extends Service implements
        RecognitionListener {

    private SpeechRecognizer recognizer;
    public static final String FREEFORM_SPEECH = "freeform";
    public static final String SMS_SPEECH = "sms";
    public static final String WEB_SEARCH = "websearch";
    public static final String LEGAL_SEARCH = "legalsearch";
    public static final String DIGITS_SEARCH = "digits";
    public static final String FORECAST_SEARCH = "forecast";
    public static final String KWS_SEARCH = "wakeup";
    public static final String MENU_SEARCH = "menu";
    public static final String KEYPHRASE = "okay android";
    public static final String LANGUAGE_MODEL_SMS = "recognizesms";
    public static final String LANGUAGE_MODEL_LEGAL_SEARCH = "recognizelegalsearch";
    public static final int TIME_TO_STOP_LISTENING = 1000;

    protected ArrayList<String> mPreviousPartialHypotheses;
    protected long mLastPartialHypothesisChangeTimestamp;
    protected Boolean mAlreadyWaitingForEndOfSpeech = false;

    public PocketSphinxRecognitionService() {
        super();
    }

    /* TODO this might not be the way to get the onStartListening to be called */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        onStartListening(intent, null);
        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStartListening(intent, null);
        return null;
    }

    // @Override
    protected void onCancel(Callback arg0) {
        recognizer.stop();
        broadcast("recognitionCompleted");
    }

    @Override
    public void onDestroy() {
        onCancel(null);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        onCancel(null);
        return super.onUnbind(intent);
    }

    // @Override
    protected void onStartListening(Intent recognizerIntent, Callback callback) {
        if (recognizer == null) {
            setupRecognizer();
        }

        recognizer.setRecognitionCallback(callback);

        if (recognizerIntent != null && recognizerIntent.getData() != null) {
            Log.d(Config.TAG, "listener called with uri "
                    + recognizerIntent.getData().toString());
        }

        if (recognizerIntent != null) {
            String requestedLanguageModel = recognizerIntent
                    .getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL);
            if (RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    .equals(requestedLanguageModel)) {
                switchSearch(FREEFORM_SPEECH);
            } else if (RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                    .equals(requestedLanguageModel)) {
                switchSearch(WEB_SEARCH);
            } else if (LANGUAGE_MODEL_SMS.equals(requestedLanguageModel)) {
                switchSearch(SMS_SPEECH);
            } else if (LANGUAGE_MODEL_LEGAL_SEARCH
                    .equals(requestedLanguageModel)) {
                switchSearch(LEGAL_SEARCH);
            } else {
                switchSearch(KWS_SEARCH);
                Toast.makeText(
                        getApplicationContext(),
                        "Say: \"Okay Android\" then your choice of: \"sms\", \"web search\" or  \"legal search\"",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            Log.w(Config.TAG,
                    "The intent to start the recognizer was not defined... this is odd.");
        }

    }

    // @Override
    protected void onStopListening(Callback callback) {
        Log.d(Config.TAG, "Stopping speech recognizer listening");
        onCancel(callback);
    }

    private void setupRecognizer() {
        try {
            Assets assets;
            assets = new Assets(getApplicationContext(),
                    Config.DEFAULT_OUTPUT_DIRECTORY);
            File assetDir = assets.syncAssets();
            Log.d(Config.TAG, "Setting up recognizer models");
            File modelsDir = new File(assetDir, "models");
            recognizer = defaultSetup()
                    .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                    .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                    .setRawLogDir(assetDir).setKeywordThreshold(1e-40f)
                    .getRecognizer();
            recognizer.addListener(this);

            // Create keyword-activation search.
            recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
            // Create grammar-based searches.
            File menuGrammar = new File(modelsDir, "grammar/menu.gram");
            recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
            File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
            recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

            File freeformGrammar = new File(modelsDir, "grammar/freeform.gram");
            recognizer.addGrammarSearch(FREEFORM_SPEECH, freeformGrammar);
            File smsGrammar = new File(modelsDir, "grammar/sms.gram");
            recognizer.addGrammarSearch(FREEFORM_SPEECH, smsGrammar);
            File webGrammar = new File(modelsDir, "grammar/websearch.gram");
            recognizer.addGrammarSearch(WEB_SEARCH, webGrammar);
            File legalGrammar = new File(modelsDir, "grammar/legalsearch.gram");
            recognizer.addGrammarSearch(LEGAL_SEARCH, legalGrammar);

            // Create language model search.
            File languageModel = new File(modelsDir, "lm/weather.dmp");
            recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(
                    getApplicationContext(),
                    "Your voice model is not ready, using the default recognition for your system.",
                    Toast.LENGTH_LONG).show();
            return;
        }

    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(Config.TAG, "   Beginning of speech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(Config.TAG,
                "   End of speech: " + mPreviousPartialHypotheses.toString());
        onCancel(null);
        // TODO why?
        // if (DIGITS_SEARCH.equals(recognizer.getSearchName())
        // || FORECAST_SEARCH.equals(recognizer.getSearchName()))
        // switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            switchSearch(MENU_SEARCH);
        } else if (text.equals(DIGITS_SEARCH)) {
            switchSearch(DIGITS_SEARCH);
        } else if (text.equals(FORECAST_SEARCH)) {
            switchSearch(FORECAST_SEARCH);
        } else {
            if (hypothesis != null) {
                broadcast(text);
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.d(Config.TAG, "Hypothesis result recieved");
        if (hypothesis != null) {
            broadcast(hypothesis.getHypstr());
        }
    }

    public void broadcast(String text) {
        if (text == null || "".equals(text)) {
            return;
        }
        Intent i = new Intent(Config.INTENT_PARTIAL_SPEECH_RECOGNITION_RESULT);
        // If the guess is not in the top, insert at the top
        if (mPreviousPartialHypotheses == null) {
            mPreviousPartialHypotheses = new ArrayList<String>();
        }
        if ("recognitionCompleted".equals(text)) {
            text = "";
            i.putExtra(Config.EXTRA_RECOGNITION_COMPLETED, true);
        }
        if (mPreviousPartialHypotheses.size() == 0
                || !mPreviousPartialHypotheses.get(0).equals(text)) {
            mLastPartialHypothesisChangeTimestamp = System.currentTimeMillis();
            if (!"".equals(text)) {
                // mPreviousPartialHypotheses.add(0, text);
                mPreviousPartialHypotheses.add(text);
            }
            Log.d(Config.TAG, "Partial Hypothesis result recieved: " + text);
            /*
             * If it has been a while since the last hypothesis, send all of
             * them as completed
             */
            // if (!mAlreadyWaitingForEndOfSpeech) {
            // mAlreadyWaitingForEndOfSpeech = true;
            // Handler mainHandler = new Handler(getApplicationContext()
            // .getMainLooper());
            // Runnable myRunnable = new Runnable() {
            // @Override
            // public void run() {
            // if (System.currentTimeMillis()
            // - mLastPartialHypothesisChangeTimestamp > TIME_TO_STOP_LISTENING
            // - 100) {
            // Log.d(Config.TAG, "Partial results have stopped.");
            // onCancel(null);
            // } else {
            // Log.d(Config.TAG,
            // "Partial results still seem to be running.");
            // }
            // mAlreadyWaitingForEndOfSpeech = false;
            // }
            // };
            // mainHandler.postDelayed(myRunnable, TIME_TO_STOP_LISTENING);
            // }
        }
        i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS,
                mPreviousPartialHypotheses);
        getApplication().sendBroadcast(i);

    }

}
