package com.github.fielddb.lessons.ui;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DatumDetailFragmentTest {
  FragmentUtilActivity mActivity;
  DatumDetailFragment mFragment;

  @Rule
  public ActivityTestRule<FragmentUtilActivity> mActivityRule = new ActivityTestRule<>(FragmentUtilActivity.class);

  @Test
  @UiThreadTest
  public void onCreate() {
    mActivity = mActivityRule.getActivity();
    assertThat(mActivity, notNullValue());

    mFragment = new DatumDetailFragment();
    mActivity.getSupportFragmentManager().beginTransaction().add(1, mFragment, "Hi").commit();
    assertThat(mFragment, notNullValue());
  }

  @Test
  public void loadMainVideo() {
  }

}