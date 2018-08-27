package com.github.fielddb.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.github.fielddb.R;

public class AccountActivity extends FragmentActivity {
  // Content provider scheme
  public static final String SCHEME = "content://";
  // The authority for the sync adapter's content provider
  public static final String AUTHORITY = "com.github.fielddb.default.datum";
  // Path for the content provider table
  public static final String TABLE_PATH = "data_table";
  // An account type, in the form of a domain name
  public static final String ACCOUNT_TYPE = "com.github.fielddb";
  // The account name
  public static final String ACCOUNT = "public";

  Account mAccount;
  ContentResolver mResolver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.setContentView(R.layout.activity_main);

    mAccount = CreateSyncAccount(this);

    Bundle settingsBundle = new Bundle();
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
  }

  /**
   * Create a new dummy account for the sync adapter
   *
   * @param context The application context
   */
  public static Account CreateSyncAccount(Context context) {
    // Create the account type and default account
    Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
    // Get an instance of the Android account manager
    AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
    /*
     * Add the account and account type, no password or user data
     */
    accountManager.addAccountExplicitly(newAccount, "none", null);

    return newAccount;
  }
}
