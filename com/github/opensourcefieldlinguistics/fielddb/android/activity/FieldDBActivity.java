package com.github.opensourcefieldlinguistics.fielddb.android.activity;

import android.os.Bundle;

import ca.ilanguage.oprime.activity.HTML5GameActivity;
import ca.ilanguage.oprime.content.JavaScriptInterface;

import com.github.opensourcefieldlinguistics.fielddb.android.content.FieldDBApp;

public class FieldDBActivity extends HTML5GameActivity {
	  /** Called when the activity is first created. */
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  }

	  protected void setUpVariables() {
	    this.TAG = FieldDBApp.getTag();
	    this.D = FieldDBApp.isD();
	    this.mInitialAppServerUrl = "file:///android_asset/release/lessons_corpus/index.html";
	    this.mOutputDir = ((FieldDBApp) getApplication()).getOutputDir();
	    this.mJavaScriptInterface = new JavaScriptInterface(D, TAG,
	        mOutputDir, getApplicationContext(), this, "release/");
	  }
}
