package com.github.fielddb.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.fielddb.Config;

/**
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */
public class CorpusSyncService extends Service {
  // Storage for an instance of the sync adapter
  private static CorpusSyncAdapter sSyncAdapter = null;
  // Object to use as a thread-safe lock
  private static final Object sSyncAdapterLock = new Object();

  /*
   * Instantiate the sync adapter object.
   */
  @Override
  public void onCreate() {
    /*
     * Create the sync adapter as a singleton.
     * Set the sync adapter as syncable
     * Disallow parallel syncs
     */
    synchronized(sSyncAdapterLock) {
      if (sSyncAdapter == null) {
        sSyncAdapter = new CorpusSyncAdapter(getApplicationContext(), true);
      }
      Log.d(Config.TAG, "CorpusSyncService");
    }
  }

  /**
   * Return an object that allows the system to invoke
   * the sync adapter.
   */
  @Override
  public IBinder onBind(Intent intent) {
    /*
     * Get the object that allows external processes
     * to call onPerformSync(). The object is created
     * in the base class code when the SyncAdapter
     * constructors call super()
     */
    return sSyncAdapter.getSyncAdapterBinder();
  }
}
