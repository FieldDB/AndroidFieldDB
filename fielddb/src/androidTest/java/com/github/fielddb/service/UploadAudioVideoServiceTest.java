package com.github.fielddb.service;

import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.GrantPermissionRule;

import com.github.fielddb.FieldDBApplication;
import com.github.fielddb.FieldDBApplicationTest;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

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

  @Before
  public void setUp() {
    underTest = new UploadAudioVideoService();
    assertThat(underTest, notNullValue());
  }

  @Test
  public void uploadShouldContactServer() {
    FieldDBApplication app = (FieldDBApplication) getTargetContext().getApplicationContext();
    String response = underTest.upload(Uri.parse("/sdcard/1.raw"));
    System.out.println(response);
    assertThat(response, notNullValue());
    // If device is offline assert that we are on Travis
    if (response.contains("Failed to execute request 8342")) {
      FieldDBApplicationTest.assertIsTravisWorkaround();
      return;
    }

    // "scriptVersion": "v1.102.2"
    assertThat(response, containsString("\"name\":\"1.raw\","));
    assertThat(response, containsString("\"dbname\":\"" + app.getUser().getUsername() + "-kartuli\","));
    assertThat(response, containsString("\"size\":38402,"));
    assertThat(response, containsString("\"checksum\":\"c4554b54a7c1e30da9f4d63cef41bab1a693a88a\","));
    assertThat(response, containsString("\"articulationRate\":\"3.18\","));
  }

  @Test
  public void shouldProcessSuccessJsonResponse() {
    String response = "{\"status\":200,\"files\":[{\"size\":38402,\"name\":\"1.raw\",\"type\":null,\"mtime\":\"2018-01-03T11:16:02.522Z\",\"fileBaseName\":\"1\",\"praatAudioExtension\":\".mp3\",\"script\":\"Syllables\",\"dbname\":\"testinganonymouskartulispeechrecognition1514829577300-kartuli\",\"checksum\":\"c4554b54a7c1e30da9f4d63cef41bab1a693a88a\",\"uploadInfo\":\"matches\",\"uploadStatus\":304,\"resultStatus\":304,\"resultInfo\":\"matches\",\"syllablesAndUtterances\":{\"fileBaseName\":\"1\",\"syllableCount\":\"4\",\"pauseCount\":\"0\",\"totalDuration\":\"1.26\",\"speakingTotalDuration\":\"1.26\",\"speakingRate\":\"3.18\",\"articulationRate\":\"3.18\",\"averageSylableDuration\":\"0.314\",\"scriptVersion\":\"v1.102.2\",\"minimum_duration\":0.6,\"maximum_intensity\":59,\"minimum_pitch\":100,\"time_step\":0,\"window_size\":20,\"margin\":0.1},\"textGridInfo\":\"regenerated\",\"textGridStatus\":200,\"webResultStatus\":304,\"webResultInfo\":\"matches\",\"serviceVersion\":\"3.16.13\"}]}\n";
    JsonObject json = underTest.processUploadResponse(response);
    assertThat(json, notNullValue());
    assertThat(underTest.getUserFriendlyErrorMessage(), nullValue());
  }

  @Test
  public void shouldProcessConnectionErrorJsonResponse() {
    String response = "{\"status\": 0, \"userFriendlyErrors\": [\"Failed to execute request 8342.\"]}";
    JsonObject json = underTest.processUploadResponse(response);
    assertThat(json, notNullValue());
    assertThat(underTest.getUserFriendlyErrorMessage(), equalTo("Failed to execute request 8342."));
  }

  @Test
  public void shouldProcessServerErrorJsonResponse() {
    String response = "{\"status\":403,\"userFriendlyErrors\":[\"Forbidden you are not permitted to upload files.\"]}\n";
    JsonObject json = underTest.processUploadResponse(response);
    assertThat(json, notNullValue());
    assertThat(underTest.getUserFriendlyErrorMessage(), equalTo("Forbidden you are not permitted to upload files."));
  }

  @Test
  public void shouldProcessMissingFilesJsonResponse() {
    String response = "{\"status\": 200 }";
    JsonObject json = underTest.processUploadResponse(response);
    assertThat(json, notNullValue());
    assertThat(underTest.getUserFriendlyErrorMessage(), equalTo("The server response is very strange, please report this."));
  }
}