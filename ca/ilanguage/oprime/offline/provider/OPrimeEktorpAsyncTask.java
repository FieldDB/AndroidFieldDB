package ca.ilanguage.oprime.offline.provider;

import org.ektorp.DbAccessException;
import org.ektorp.android.util.EktorpAsyncTask;

import android.util.Log;

public abstract class OPrimeEktorpAsyncTask extends EktorpAsyncTask {
  protected static final String TAG = "OPrime";

  @Override
  protected void onDbAccessException(DbAccessException dbAccessException) {
    Log.e(TAG, "DbAccessException in background", dbAccessException);
  }

}
