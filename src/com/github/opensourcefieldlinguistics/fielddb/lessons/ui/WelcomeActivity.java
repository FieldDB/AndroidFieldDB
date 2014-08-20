package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.util.ArrayList;

import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
    private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_welcome);

    }

    public void onTrainClick(View view) {
        Intent openTrainer = new Intent(this,
                ProductionExperimentActivity.class);
        startActivity(openTrainer);
    }

    public void onRecognizeClick(View view) {
        // Intent openRecognizer = new Intent(this,
        // SpeechRecognitionActivity.class);
        // startActivity(openRecognizer);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.im_listening));
        startActivityForResult(intent,
                RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            /*
             * Toast the first result to the user and say them out loud.
             */
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String result = "Try again...";
            if (matches.size() > 0 && matches.get(0) != null) {
                result = matches.get(0);
            }
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void goToWebSite(View view) {
        Intent go = new Intent(Intent.ACTION_VIEW).setData(Uri
                .parse("http://batumi.github.io"));
        startActivity(go);
    }
}
