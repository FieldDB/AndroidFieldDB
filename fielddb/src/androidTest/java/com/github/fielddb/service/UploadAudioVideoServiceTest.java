package com.github.fielddb.service;

import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
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
  @Rule
  public GrantPermissionRule mRuntimeReadSdcardPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);

  @Test
  public void uploadShouldContactServer() {
    underTest = new UploadAudioVideoService();
    assertThat(underTest, notNullValue());
    String response = underTest.upload(Uri.parse("/sdcard/1.raw"));
    assertThat(response, notNullValue());
    assertThat(response, containsString("\"name\":\"1.raw\","));
    assertThat(response, containsString("\"dbname\":\"username-kartuli\","));
    assertThat(response, containsString("\"size\":38402,"));
  }
}