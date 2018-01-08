package com.github.fielddb.lessons.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import com.github.fielddb.Config;
import com.github.fielddb.model.Datum;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatumDetailFragmentTest {
  FragmentUtilActivity mActivity;
  DatumDetailFragment mFragment;

  @Rule
  public GrantPermissionRule mRuntimeReadSdcardPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);
//  Can delay start
//  public ActivityTestRule<FragmentUtilActivity> mActivityRule = new ActivityTestRule<>(FragmentUtilActivity.class, false, false);

  @Rule
  public ActivityTestRule<FragmentUtilActivity> mActivityRule = new ActivityTestRule<FragmentUtilActivity>(FragmentUtilActivity.class) {
    @Override
    protected Intent getActivityIntent() {
      Intent intent = new Intent(InstrumentationRegistry.getContext(), FragmentUtilActivity.class);
      intent.putExtra("Key","Value");
      return intent;
    }
  };

  @Before
  public void onCreate() {
    Bundle arguments = new Bundle();
    arguments.putString(DatumDetailFragment.ARG_ITEM_ID, "1");
    mFragment = new DatumDetailFragment();
    assertThat(mFragment, notNullValue());
    mFragment.setArguments(arguments);

//    If you want to delay start using rule(classs, false false)
//    Intent intent = new Intent(InstrumentationRegistry.getContext(), FragmentUtilActivity.class);
//    mActivityRule.launchActivity(intent);

    mActivity = mActivityRule.getActivity();
    assertThat(mActivity, notNullValue());
    mActivity.getSupportFragmentManager().beginTransaction().add(1, mFragment, "Hi").commitAllowingStateLoss();
  }

  @Test
  @UiThreadTest
  public void loadMainVideo() {
    mFragment.setItem(new Datum("კი მაგრამ"));
    mFragment.getItem().setAudioVideoFiles("1.mp3");
    Config.DEFAULT_OUTPUT_DIRECTORY = "/sdcard";
    boolean successful = mFragment.loadMainVideo(false);
    assertTrue(successful);
  }

}