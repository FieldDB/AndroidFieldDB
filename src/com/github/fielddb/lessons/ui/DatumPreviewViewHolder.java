package com.github.fielddb.lessons.ui;

import com.github.fielddb.database.CursorRecyclerViewAdapter;
import com.github.fielddb.Config;
import com.github.fielddb.R;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * https://github.com/devunwired/recyclerview-playground
 * 
 */
public class DatumPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  private int mPosition;
  private Uri mUri;
  private TextView mOrthographyView;
  private TextView mTranslationView;
  private ImageView mIconView;

  private CursorRecyclerViewAdapter mAdapter;

  protected AdapterView.OnItemClickListener mOnItemClickListener;

  public DatumPreviewViewHolder(View itemView, CursorRecyclerViewAdapter adapter) {
    super(itemView);
    itemView.setOnClickListener(this);
    mAdapter = adapter;

    mOrthographyView = (TextView) itemView.findViewById(R.id.orthography);
    mTranslationView = (TextView) itemView.findViewById(R.id.translation);
  }

  @Override
  public void onClick(View v) {
    Log.d(Config.TAG, "TODO open detail view.");
    // mAdapter.onItemHolderClick(this);
  }

  public void removeThisRow() {
    mAdapter.removeItem(mPosition, mUri);
  }

  public void setPosition(int position) {
    this.mPosition = position;
  }

  public void setUri(Uri uri) {
    this.mUri = uri;
  }

  public void setOrthography(CharSequence orthography) {
    this.mOrthographyView.setText(orthography);
  }

  public void setTranslation(CharSequence translation) {
    this.mTranslationView.setText(translation);
  }

  public void setIcon(int iconId) {
    if (mIconView == null) {
      mIconView = (ImageView) itemView.findViewById(R.id.icon);
    }
    this.mIconView.setImageResource(iconId);
  }

  public void setIcon(String filename) {
    if (mIconView == null) {
      mIconView = (ImageView) itemView.findViewById(R.id.icon);
    }
    Log.d(Config.TAG, "TODO set the image thumbnail.");
  }
}