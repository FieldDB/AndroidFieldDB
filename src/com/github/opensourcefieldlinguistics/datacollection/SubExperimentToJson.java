package com.github.opensourcefieldlinguistics.datacollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.opensourcefieldlinguistics.fielddb.Config;
import com.github.opensourcefieldlinguistics.fielddb.model.OPrimeApp;
import com.github.opensourcefieldlinguistics.fielddb.model.SubExperimentBlock;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SubExperimentToJson extends IntentService {
  protected boolean D   = true;
  protected String  TAG = "OPrime";

  public SubExperimentToJson() {
    super("SubExperimentToJson");
  }

  public SubExperimentToJson(String name) {
    super(name);

  }

  @Override
  protected void onHandleIntent(Intent intent) {
    this.D = ((OPrimeApp) this.getApplication()).D;
    SubExperimentBlock subex = (SubExperimentBlock) intent.getExtras().getSerializable(Config.EXTRA_SUB_EXPERIMENT);
    String resultsFile = subex.getResultsFileWithoutSuffix().replace("video", "touchdata") + ".json";
    File outfile = new File(resultsFile);
    try {
      FileOutputStream out = new FileOutputStream(outfile, false);
      out.write(subex.getResultsJson().getBytes());
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      Log.e(this.TAG, "FileNotFoundException Problem opening outfile.");

    } catch (IOException e) {
      Log.e(this.TAG, "IOException Problem writing outfile.");
    }
    if (this.D)
      Log.d(this.TAG, "Done service.");
  }

}
