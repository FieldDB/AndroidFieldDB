package ca.ilanguage.oprime.offline.content;

import java.io.File;

import android.app.Application;
import android.util.Log;

public abstract class OPrimeOffineApp extends Application {
  protected String TAG = "OPrimeApp";
  protected boolean D = false;

  protected String mOutputDir = "/sdcard/OPrime/";
  protected String[] mDevEmailAddresses = new String[] { "opensource@lingsync.org" };

  
  protected String mLocalCouchDir = "db/";

  @Override
  public void onCreate() {
    setUpDirectories();
    (new File(mLocalCouchDir)).mkdirs();

    super.onCreate();
    if (D)
      Log.d(TAG, "Oncreate of the OPrimeOfflineApp ");
  }

  /**
   * Example:
   * 
   * mOutputDir = this.getFilesDir().getAbsolutePath() + File.separator;
   * mLocalCouchDir = mOutputDir + "db/";
   */
  protected abstract void setUpDirectories();

  public String getOutputDir() {
    return mOutputDir;
  }

  protected void setOutputDir(String mOutputDir) {
    this.mOutputDir = mOutputDir;
  }

  public boolean isD() {
    return D;
  }

  public String getTag() {
    return TAG;
  }

  public String getLocalCouchDir() {
    return mLocalCouchDir;
  }

  protected void setLocalCouchDir(String mLocalCouchDir) {
    this.mLocalCouchDir = mLocalCouchDir;
  }

  public String[] getDevEmailAddresses() {
    return mDevEmailAddresses;
  }

  protected void setDevEmailAddresses(String[] mDevEmailAddresses) {
    this.mDevEmailAddresses = mDevEmailAddresses;
  }
}
