package com.github.fielddb.service;

import android.app.Activity;
import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import com.github.fielddb.datacollection.SecureHttpClient;
import com.github.fielddb.lessons.ui.DatumDetailActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


@RequiresDevice
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UploadAudioVideoServiceTest {
  UploadAudioVideoService underTest;

  public UploadAudioVideoServiceTest() {
    super();
  }

  @Test
  public void upload_shouldWork() {
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        Activity activity = new DatumDetailActivity();
        SecureHttpClient.checkAndRequestPermissions(activity, 0);
        underTest = new UploadAudioVideoService();
        assertThat(underTest, notNullValue());
        String response = underTest.upload(Uri.parse("/sdcard/1.raw"));
        assertThat(response, containsString(" something "));
      }
    });
  }
}