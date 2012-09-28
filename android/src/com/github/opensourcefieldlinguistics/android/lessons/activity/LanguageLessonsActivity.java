package com.github.opensourcefieldlinguistics.android.lessons.activity;

import ca.ilanguage.oprime.activity.HTML5GameActivity;

import com.github.opensourcefieldlinguistics.android.lessons.content.LanguageLessonsApp;
import com.github.opensourcefieldlinguistics.android.lessons.content.LanguageLessonsJavaScriptInterface;

import android.os.Bundle;

public class LanguageLessonsActivity extends HTML5GameActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void setUpVariables(){
    	this.TAG = LanguageLessonsApp.getTag();
    	this.D  = LanguageLessonsApp.isD();
    	this.mInitialGameServerUrl = 
    			"file:///android_asset/release/lessons_corpus/index.html";
    	this.mOutputDir = ((LanguageLessonsApp) getApplication())
				.getOutputDir();
    	this.mJavaScriptInterface = new LanguageLessonsJavaScriptInterface(D, TAG, mOutputDir);
    	this.mJavaScriptInterface.setContext(this);
    }
}