package com.github.fielddb.lessons.ui;

import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.fielddb.MainActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class DatumListActivityTest {
  MainActivity mActivity;

  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

  @Test
  @UiThreadTest
  public void onCreate() {
    mActivity = mActivityRule.getActivity();
    assertThat(mActivityRule.getActivity(), notNullValue());
  }

  @Test
  public void onItemSelected() {
  }

  @Test
  public void onItemDeleted() {
  }

}
