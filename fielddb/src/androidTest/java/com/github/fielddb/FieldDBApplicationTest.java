package com.github.fielddb;

import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FieldDBApplicationTest extends ApplicationTestCase<FieldDBApplication> {
  private FieldDBApplication mContext;

  public FieldDBApplicationTest() {
    super(FieldDBApplication.class);
  }

  @Before
  public void initTargetContext() {
    mContext = (FieldDBApplication) getTargetContext().getApplicationContext();
    assertThat(mContext, notNullValue());
  }

  public void logEnvVar() {
    Map<String, String> env = System.getenv();
    String envString = "";
    for (String envName : env.keySet()) {
      System.out.format("%s=%s%n", envName, env.get(envName));
      envString = envString + envName + ":" + env.get(envName) + "\n";
    }
    assertThat(envString, equalTo(envString.concat("forceoutput")));
  }

  /**
   * If not installed with a consuming app application should load and not fail.
   * Usually the library is developed with a consuming app on the device
   * so mUser should not be null unless running on Travis.
   * <p>
   * If unable to detect travis by set/pass TRAVIS env var in the emulator,
   * can use these two var that appear in Travis as of 2018-01-02 but not on
   * Android Studio generated emulators as a proxy to detect if we are probably on Travis
   */
  public static void assertIsTravisWorkaround() {
    String travis = System.getenv("ANDROID_PROPERTY_WORKSPACE");
    assertThat(travis, equalTo("8,0"));
    travis = System.getenv("LOOP_MOUNTPOINT");
    assertThat(travis, equalTo("/mnt/obb"));
  }

  @Test
  public void mUserIsNull() {
    if (mContext.mUser == null) {
      // String travis = System.getenv("TRAVIS");
      // assertThat(travis, equalTo("true"));
      assertIsTravisWorkaround();
      return;
    }
    // Expect to be co-installed with a consuming app
    assertThat(mContext.mUser, notNullValue());

    if (BuildConfig.DEBUG) {
      assertThat(mContext.mUser.getUsername(), containsString("anonymous"));
    } else {
      assertThat(mContext.mUser.getUsername(), containsString("testinganonymous"));
    }
  }

}
