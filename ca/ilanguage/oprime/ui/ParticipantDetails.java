package ca.ilanguage.oprime.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.model.OPrimeApp;

public class ParticipantDetails extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
    getPreferenceManager().setSharedPreferencesName(
        Config.PREFERENCE_NAME);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);

  }

  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      SharedPreferences prefs = getSharedPreferences(Config.PREFERENCE_NAME,
          MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      // editor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);

      editor.commit();
      Toast.makeText(this, "Dialog was closed", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onBackPressed() {
    String tag = Config.TAG;
    boolean d = ((OPrimeApp) this.getApplication()).isD();
    try {
      boolean prepareExperiment = getIntent().getExtras().getBoolean(
          Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, false);
      if (prepareExperiment) {
      } else {
        Intent i = new Intent(getBaseContext(), HTML5GameActivity.class);
        i.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
        i.putExtra(Config.EXTRA_DEBUG_MODE, d);
        i.putExtra(Config.EXTRA_TAG, tag);
        startActivity(i);
      }
    } catch (Exception e) {
      Intent i = new Intent(getBaseContext(), HTML5GameActivity.class);
      i.putExtra(Config.EXTRA_PLEASE_PREPARE_EXPERIMENT, true);
      i.putExtra(Config.EXTRA_DEBUG_MODE, d);
      i.putExtra(Config.EXTRA_TAG, tag);
      startActivity(i);
    }
    finish();

    super.onBackPressed();
  }

}
