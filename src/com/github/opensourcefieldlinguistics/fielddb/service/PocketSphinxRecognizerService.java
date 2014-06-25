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

public class PocketSphinxRecognizerService extends RecognitionService implements
        RecognitionListener {

    private SpeechRecognizer recognizer;
    public static final String DIGITS_SEARCH = "digits";
    public static final String FORECAST_SEARCH = "forecast";
    public static final String KWS_SEARCH = "wakeup";
    public static final String MENU_SEARCH = "menu";
    public static final String KEYPHRASE = "oh mighty computer";

    public PocketSphinxRecognizerService() {
        super();

    }

    @Override
    protected void onCancel(Callback arg0) {
        recognizer.stop();
    }

    @Override
    protected void onStartListening(Intent arg0, Callback arg1) {
        try {
            Assets assets;
            assets = new Assets(getApplicationContext(),
                    Config.DEFAULT_OUTPUT_DIRECTORY);
            File assetDir = assets.syncAssets();
            setupRecognizer(assetDir);
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
        recognizer.stop();
    }

    private void setupRecognizer(File assetsDir) {
        Log.d(Config.TAG, "Setting up recognizer models");
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-40f)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create grammar-based searches.
        File menuGrammar = new File(modelsDir, "grammar/menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
        File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
        File languageModel = new File(modelsDir, "lm/weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
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
        if (DIGITS_SEARCH.equals(recognizer.getSearchName())
                || FORECAST_SEARCH.equals(recognizer.getSearchName()))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        Log.d(Config.TAG, "Partial Hypothesis result recieved");
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);
        else {
            ArrayList<String> hypotheses = new ArrayList<String>();
            Intent i = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
            if (hypothesis != null) {
                hypotheses.add(hypothesis.getHypstr());
            }
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS,
                    new ArrayList<String>());
            getApplication().sendBroadcast(i);
        }

    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.d(Config.TAG, "Hypothesis result recieved");
        Intent i = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
        ArrayList<String> hypotheses = new ArrayList<String>();
        if (hypothesis != null) {
            hypotheses.add(hypothesis.getHypstr());
        }
        i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS,
                new ArrayList<String>());
        getApplication().sendBroadcast(i);

    }

}
