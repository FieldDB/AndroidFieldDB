package com.github.fielddb.service;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UploadAudioVideoServiceTest {
  UploadAudioVideoService underTest;

  public UploadAudioVideoServiceTest() {
    super();
  }

  @Rule
  public GrantPermissionRule mRuntimeInternetPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.INTERNET);
  @Rule
  public GrantPermissionRule mRuntimeAccessNetworkStatePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_NETWORK_STATE);
  @Rule
  public GrantPermissionRule mRuntimeAccessWifiStatePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_WIFI_STATE);

//  @Before
//  public void stubAllExternalIntents() {
//    ActivityResult ar = new ActivityResult(Activity.RESULT_OK, null);
//    intending(not(isInternal())).respondWith(ar);
//  }

  @Test
  public void uploadShouldWork() {
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        underTest = new UploadAudioVideoService();
        assertThat(underTest, notNullValue());
        String response = underTest.upload(Uri.parse("/sdcard/1.raw"));
        assertThat(response, containsString(" something "));
      }
    });
  }
}