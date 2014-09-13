package com.github.opensourcefieldlinguistics.fielddb.android.content;

import java.io.File;

import ca.ilanguage.oprime.offline.content.OPrimeOffineApp;

public class FieldDBApp extends OPrimeOffineApp {


  @Override
  public void onCreate() {
    D = true;
    TAG = "FieldDB";
    super.onCreate();
  }

  @Override
  public void setUpDirectories() {
    mOutputDir = this.getFilesDir().getAbsolutePath() + File.separator;
    mLocalCouchDir = mOutputDir + "db/";
  }

}
