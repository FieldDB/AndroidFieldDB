package ca.ilanguage.oprime.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class OPrimeLib extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		startActivity(new Intent(this, ExperimentListActivity.class));
		finish();
	}

}
