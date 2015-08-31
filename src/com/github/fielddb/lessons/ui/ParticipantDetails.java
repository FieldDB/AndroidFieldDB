package com.github.fielddb.lessons.ui;

import com.github.fielddb.Config;
import com.github.fielddb.javascript.HTML5GameActivity;
import com.github.fielddb.model.OPrimeApp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

@Deprecated
public class ParticipantDetails extends Activity {

  @Override
  public void onBackPressed() {
    String tag = Config.TAG;
    boolean d = ((OPrimeApp) this.getApplication()).isD();
    try {
      boolean prepareExperiment = this.getIntent().getExtras()
          .getBoolean(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, false);
      if (prepareExperiment) {
      } else {
        Intent i = new Intent(this.getBaseContext(), HTML5GameActivity.class);
        i.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
        i.putExtra(Config.EXTRA_DEBUG_MODE, d);
        i.putExtra(Config.EXTRA_TAG, tag);
        this.startActivity(i);
      }
    } catch (Exception e) {
      Intent i = new Intent(this.getBaseContext(), HTML5GameActivity.class);
      i.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      i.putExtra(Config.EXTRA_DEBUG_MODE, d);
      i.putExtra(Config.EXTRA_TAG, tag);
      this.startActivity(i);
    }
    this.finish();

    super.onBackPressed();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // this.getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
    // this.getPreferenceManager().setSharedPreferencesName(Config.PREFERENCE_NAME);
    //
    // // Load the preferences from an XML resource
    // this.addPreferencesFromResource(R.xml.preferences);

  }

  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      SharedPreferences prefs = this.getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      // editor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);

      editor.commit();
      Toast.makeText(this, "Dialog was closed", Toast.LENGTH_SHORT).show();
    }
  }

}
