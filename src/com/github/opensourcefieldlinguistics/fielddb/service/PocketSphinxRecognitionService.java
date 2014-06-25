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
import android.content.Intent;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

public class PocketSphinxRecognitionService extends RecognitionService
        implements RecognitionListener {

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
    protected void onCancel(Callback arg0) {
        recognizer.stop();
    }

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback callback) {
        try {
            if (recognizerIntent != null && recognizerIntent.getData() != null) {
                Log.d(Config.TAG, "listener called with uri "
                        + recognizerIntent.getData().toString());
            }

            Assets assets;
            assets = new Assets(getApplicationContext(),
                    Config.DEFAULT_OUTPUT_DIRECTORY);
            File assetDir = assets.syncAssets();
            setupRecognizer(assetDir, recognizerIntent, callback);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(
                    getApplicationContext(),
                    "Your voice model is not ready, using the default recognition for your system.",
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onStopListening(Callback arg0) {
        Log.d(Config.TAG, "Stopping speech recognizer listening");
        recognizer.stop();
    }

    private void setupRecognizer(File assetsDir, Intent recognizerIntent,
            Callback callback) {

        Log.d(Config.TAG, "Setting up recognizer models");
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-40f)
                .getRecognizer();
        recognizer.addListener(this);
        recognizer.setRecognitionCallback(callback);

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
        Log.d(Config.TAG, "   End of speech");
        // TODO why?
        if (DIGITS_SEARCH.equals(recognizer.getSearchName())
                || FORECAST_SEARCH.equals(recognizer.getSearchName()))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        Log.d(Config.TAG, "Partial Hypothesis result recieved: " + text);
        if (text.equals(KEYPHRASE)) {
            switchSearch(MENU_SEARCH);
        } else if (text.equals(DIGITS_SEARCH)) {
            switchSearch(DIGITS_SEARCH);
        } else if (text.equals(FORECAST_SEARCH)) {
            switchSearch(FORECAST_SEARCH);
        } else {
            if (hypothesis != null) {
                broadcast(hypothesis.getHypstr());
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
        ArrayList<String> hypotheses = new ArrayList<String>();
        if (text == null || "".equals(text)) {
            return;
        }
        Intent i = new Intent(Config.INTENT_PARTIAL_SPEECH_RECOGNITION_RESULT);
        hypotheses.add(text);
        i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, hypotheses);
        getApplication().sendBroadcast(i);

    }

}
