package com.github.opensourcefieldlinguistics.android.lessons.activity;

import org.apache.cordova.DroidGap;

import android.os.Bundle;

public class CordovaActivity extends DroidGap {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.loadUrl("file:///android_asset/www/lessons_corpus/index.html");
	}

}
