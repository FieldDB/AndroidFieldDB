package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_welcome);

	}

	public void onTrainClick(View view) {
		Intent openTrainer = new Intent(this,
				ProductionExperimentViewPagerFragmentActivity.class);
		startActivity(openTrainer);
	}

	public void onRecognizeClick(View view) {
		Intent openRecognizer = new Intent(this, ListenAndRepeat.class);
		startActivity(openRecognizer);
	}
	
	public void goToWebSite(View view) {
		Intent go = new Intent(Intent.ACTION_VIEW).setData(Uri
				.parse("http://batumi.github.io"));
		startActivity(go);
	}
}
