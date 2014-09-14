package com.github.opensourcefieldlinguistics.fielddb.service;


import java.util.ArrayList;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognitionService.Callback;
import android.speech.RecognizerIntent;
import android.util.Log;

public class PocketSphinxRecognitionService extends Service  {

    public static final String FREEFORM_SPEECH = "freeform";
    public static final String SMS_SPEECH = "sms";
    public static final String WEB_SEARCH = "websearch";
    public static final String LEGAL_SEARCH = "legalsearch";
    public static final String LANGUAGE_MODEL_SMS = "recognizesms";
    public static final String LANGUAGE_MODEL_LEGAL_SEARCH = "recognizelegalsearch";
    public static final String EXTRA_RESULT_AUDIO_FILE = "extra_result_audio_file";
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

    protected void onCancel(Callback arg0) {
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
        
    }

    // @Override
    protected void onStopListening(Callback callback) {
        Log.d(Config.TAG, "Stopping speech recognizer listening");
        onCancel(callback);
    }

    private void setupRecognizer() {
       
    }

    public void onBeginningOfSpeech() {
        Log.d(Config.TAG, "   Beginning of speech");
    }

    public void onEndOfSpeech() {
       
    }

//    @Override
    public void onPartialResult(Object hypothesis) {
       
    }

//    @Override
    public void onResult(Object hypothesis) {
        Log.d(Config.TAG, "Hypothesis result recieved");
        broadcast(hypothesis, true);
    }

    public void broadcast(Object hypothesis, boolean completedResult) {
        String text = "Speech recognition is not included in this app.";
        if (text == null || "".equals(text)) {
            return;
        }
        ArrayList<String> confidences = new ArrayList<String>();
        confidences.add("0");


        Intent i = new Intent(Config.INTENT_PARTIAL_SPEECH_RECOGNITION_RESULT);
        i.putExtra(Config.EXTRA_RECOGNITION_COMPLETED, true);
        ArrayList<String> completedHypothesis = new ArrayList<String>();
        completedHypothesis.add(text);
        i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS,
            completedHypothesis);

        i.putStringArrayListExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES,
                confidences);
        getApplication().sendBroadcast(i);

    }
}
