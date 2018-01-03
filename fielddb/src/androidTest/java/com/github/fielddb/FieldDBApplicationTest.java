package com.github.fielddb;

import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.containsString;
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

  @Test
  public void mUserIsNull() {
    // If not installed with a consuming app application shoud load and not fail
    if (mContext.mUser == null) {
      assertThat(mContext.mUser, nullValue());
      return;
    }
    // Expect to be co-installed with a consuming app
    assertThat(mContext.mUser, notNullValue());
    assertThat(mContext.mUser.getUsername(), containsString("testinganonymous"));
  }

}
