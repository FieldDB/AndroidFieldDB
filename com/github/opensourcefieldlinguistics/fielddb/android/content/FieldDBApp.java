package com.github.opensourcefieldlinguistics.fielddb.android.content;

import java.io.File;

import android.util.Log;

import ca.ilanguage.oprime.content.OPrimeApp;;

public class FieldDBApp extends OPrimeApp {

  public static final String PREFERENCE_PREFERENCE_NAME = "fielddbapppreferences";
  public static final String PREFERENCE_USERS_DB_NAME = "usersdbname";
  public static final String PREFERENCE_USERNAME = "username";
  public static final String PREFERENCE_PASSWORD = "password";
  public static final String PREFERENCE_COUCH_SERVER_DOMAIN = "couchServerDomain";

  @Override
  public void onCreate() {
    TAG = "FieldDB";
    D = true;
    
//    mOutputDir = "/sdcard/FieldDB";
    mOutputDir = this.getFilesDir().getAbsolutePath();
    mLocalCouchDir = mOutputDir + "/db/";
    new File(mLocalCouchDir).mkdirs();

    super.onCreate();
    if (D)
      Log.d(TAG, "Oncreate of the application");
  }

  public String getLocalCouchDir() {
    return mLocalCouchDir;
  }

  public void setLocalCouchDir(String mLocalCouchDir) {
    this.mLocalCouchDir = mLocalCouchDir;
  }

}
