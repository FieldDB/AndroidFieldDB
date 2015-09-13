package com.github.fielddb.lessons.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.fielddb.Config;
import com.github.fielddb.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * Building on what we saw in MakeItTalk, now lets make it Listen. Here is some
 * super simple code that uses the VoiceRecognition Intent to recognize what the
 * user says, and then uses Text To Speech to tell the user what it might have
 * heard.
 * 
 * 
 */
public class ListenAndRepeat extends Activity implements OnInitListener {
  private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;
  /**
   * Talk to the user
   */
  private TextToSpeech mTts;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mTts = new TextToSpeech(this, this);
  }

  protected void promptTheUserToTalk() {
    if (isIntentAvailable(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) {
      this.speak(getString(R.string.im_listening));
    } else {
      this.speak(getString(R.string.i_cant_listen));
    }
  }

  /**
   * Fire an intent to start the voice recognition activity.
   */
  private void startVoiceRecognitionActivity() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.im_listening));
    if (isIntentAvailable(intent)) {
      startActivityForResult(intent, RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE);
    } else {
      Log.w(Config.TAG,
          "This device doesn't have speech recognition, maybe its an emulator or a phone from china without google products?");
    }
  }

  /**
   * Handle the results from the voice recognition activity.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
      /*
       * Populate the wordsList with the String values the recognition engine
       * thought it heard, and then Toast them to the user and say them out
       * loud.
       */
      ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      for (int iMightHaveHeardThis = 0; iMightHaveHeardThis < matches.size(); iMightHaveHeardThis++) {

        /* Build a carrierPhrase if you want it to make some sense */
        String carrierPhrase = getString(R.string.i_might_have_heard);
        if (iMightHaveHeardThis > 0) {
          carrierPhrase = getString(R.string.or_maybe);
        }
        carrierPhrase += " " + matches.get(iMightHaveHeardThis) + ".";

        Toast.makeText(this, carrierPhrase, Toast.LENGTH_LONG).show();
        this.speak(carrierPhrase);

        /*
         * Don't go on forever, it there are too many potential matches don't
         * say them all
         */
        if (iMightHaveHeardThis == 2 && matches.size() > 2) {
          this.speak(getString(R.string.there_were_others));
          break;
        }
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onDestroy() {
    if (mTts != null) {
      mTts.stop();
      mTts.shutdown();
    }
    super.onDestroy();
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = mTts.setLanguage(Locale.getDefault());
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e(Config.TAG, "Language is not available.");
        Toast.makeText(
            this,
            "The " + Locale.getDefault().getDisplayLanguage() + " TextToSpeech isn't installed, you can go into the "
                + "\nAndroid's settings in the " + "\nVoice Input and Output menu to turn it on. ", Toast.LENGTH_LONG)
            .show();
      } else {
        // everything is working.
        promptTheUserToTalk();
        startVoiceRecognitionActivity();
      }
    } else {
      Toast.makeText(this, "Sorry, I can't talk to you because " + "I could not initialize TextToSpeech.",
          Toast.LENGTH_LONG).show();
    }
  }

  public boolean speak(String message) {
    if (mTts != null) {
      mTts.speak(message, TextToSpeech.QUEUE_ADD, null);
    } else {
      Toast.makeText(this, "Sorry, I can't speak to you: " + message, Toast.LENGTH_LONG).show();
    }
    return true;
  }

  public boolean isIntentAvailable(String action) {
    final Intent intent = new Intent(action);
    return isIntentAvailable(intent);
  }

  public boolean isIntentAvailable(final Intent intent) {
    final PackageManager packageManager = this.getPackageManager();
    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

}