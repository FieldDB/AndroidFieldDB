package ca.ilanguage.aphasia.anomia.android.activity;

import ca.ilanguage.aphasia.anomia.android.content.AnomiaTrainerApp;
import ca.ilanguage.aphasia.anomia.android.content.LanguageLessonsJavaScriptInterface;
import ca.ilanguage.oprime.activity.HTML5GameActivity;


import android.os.Bundle;

public class AnomiaTrainerActivity extends HTML5GameActivity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  protected void setUpVariables() {
    this.TAG = AnomiaTrainerApp.getTag();
    this.D = AnomiaTrainerApp.isD();
    this.mInitialAppServerUrl = "file:///android_asset/release/lessons_corpus/index.html";
    this.mOutputDir = ((AnomiaTrainerApp) getApplication()).getOutputDir();
    this.mJavaScriptInterface = new LanguageLessonsJavaScriptInterface(D, TAG,
        mOutputDir, getApplicationContext(), this, "release/");
  }
}