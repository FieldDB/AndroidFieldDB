package ca.ilanguage.oprime.datacollection;

import ca.ilanguage.oprime.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StopAudioRecorder extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.fragment_fixation_stop);
  }

  public void stop(View view) {
    this.onBackPressed();
  }

  @Override
  public void onDestroy() {
    Intent audio = new Intent(this, AudioRecorder.class);
    this.stopService(audio);
    super.onDestroy();
  }
  
}
