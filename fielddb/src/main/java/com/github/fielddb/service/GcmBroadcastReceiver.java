package com.github.fielddb.service;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

public class GcmBroadcastReceiver extends BroadcastReceiver {
  // Content provider authority
  public static final String AUTHORITY = "com.example.android.datasync.provider";
  // Account type
  public static final String ACCOUNT_TYPE = "com.example.android.datasync";
  // Account
  public static final String ACCOUNT = "default_account";
  // Incoming Intent key for extended data
  public static final String KEY_SYNC_REQUEST = "com.example.android.datasync.KEY_SYNC_REQUEST";

  @Override
  public void onReceive(Context context, Intent intent) {
    // Get a GCM object instance
//    GoogleCloudMessaging gcm =
//        GoogleCloudMessaging.getInstance(context);
//    // Get the type of GCM message
//    String messageType = gcm.getMessageType(intent);
//        /*
//         * Test the message type and examine the message contents.
//         * Since GCM is a general-purpose messaging system, you
//         * may receive normal messages that don't require a sync
//         * adapter run.
//         * The following code tests for a a boolean flag indicating
//         * that the message is requesting a transfer from the device.
//         */
//    if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)
//        &&
//        intent.getBooleanExtra(KEY_SYNC_REQUEST)) {
//            /*
//             * Signal the framework to run your sync adapter. Assume that
//             * app initialization has already created the account.
//             */
//      ContentResolver.requestSync(ACCOUNT, AUTHORITY, null);
//            ...
//    }
  }
}
