package com.github.fielddb.lessons.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.LinearLayout;

public class  FragmentUtilActivity extends FragmentActivity implements DatumListFragment.Callbacks {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout view = new LinearLayout(this);
    view.setId(1);

    setContentView(view);
  }
  @Override
  public void onItemSelected(String id) {

  }
  @Override
  public void onItemDeleted(Uri uri) {
  }
}