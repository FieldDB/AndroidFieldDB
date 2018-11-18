package com.github.fielddb.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.github.fielddb.Config;

public class CorpusSyncAdapter extends AbstractThreadedSyncAdapter {
  // Global variables
  // Define a variable to contain a content resolver instance
  ContentResolver mContentResolver;

  /**
   * Set up the sync adapter
   */
  public CorpusSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    /*
     * If your app uses a content resolver, get an instance of it
     * from the incoming Context
     */
    mContentResolver = context.getContentResolver();
  }

  /**
   * Set up the sync adapter. This form of the
   * constructor maintains compatibility with Android 3.0
   * and later platform versions
   */
  public CorpusSyncAdapter(
    Context context,
    boolean autoInitialize,
    boolean allowParallelSyncs) {
    super(context, autoInitialize, allowParallelSyncs);
    /*
     * If your app uses a content resolver, get an instance of it
     * from the incoming Context
     */
    mContentResolver = context.getContentResolver();
  }

  /*
   * Specify the code you want to run in the sync adapter. The entire
   * sync adapter runs in a background thread, so you don't have to set
   * up your own background processing.
   */
  @Override
  public void onPerformSync(
    Account account,
    Bundle extras,
    String authority,
    ContentProviderClient provider,
    SyncResult syncResult) {
    String JSONResponse;

    /*
     * Put the data transfer code here.
     */

    Log.d(Config.TAG, "onPerformSync");

  }
}
