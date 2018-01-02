package com.github.fielddb.lessons.ui;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.github.fielddb.test.R;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class DatumListActivityTest extends ActivityInstrumentationTestCase2<DatumListActivity> {
  DatumListActivity mActivity;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    getTargetContext().getApplicationContext().setTheme(R.style.AppTheme);

    injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    getInstrumentation().setInTouchMode(false);
    setActivityInitialTouchMode(false);

    mActivity = getActivity();
  }

  public DatumListActivityTest() {
    super(DatumListActivity.class);
  }

  @Test
  public void onCreate() {
    assertThat(mActivity, notNullValue());
  }

  @Test
  public void onItemSelected() {
  }

  @Test
  public void onItemDeleted() {
  }

}