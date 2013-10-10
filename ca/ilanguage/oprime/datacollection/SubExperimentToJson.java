package ca.ilanguage.oprime.datacollection;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.model.OPrimeApp;
import ca.ilanguage.oprime.model.SubExperimentBlock;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SubExperimentToJson extends IntentService {
  protected String TAG = "OPrime";
  protected boolean D = true;

  public SubExperimentToJson(String name) {
    super(name);

  }

  public SubExperimentToJson() {
    super("SubExperimentToJson");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    D = ((OPrimeApp) getApplication()).D;
    SubExperimentBlock subex = (SubExperimentBlock) intent.getExtras()
        .getSerializable(Config.EXTRA_SUB_EXPERIMENT);
    String resultsFile = subex.getResultsFileWithoutSuffix().replace("video",
        "touchdata")
        + ".json";
    File outfile = new File(resultsFile);
    try {
      FileOutputStream out = new FileOutputStream(outfile, false);
      out.write(subex.getResultsJson().getBytes());
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      Log.e(TAG, "FileNotFoundException Problem opening outfile.");

    } catch (IOException e) {
      Log.e(TAG, "IOException Problem writing outfile.");
    }
    if (D)
      Log.d(TAG, "Done service.");
  }

}
