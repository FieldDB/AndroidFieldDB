package com.github.fielddb.database;

import com.github.fielddb.R;
import com.github.fielddb.lessons.ui.DatumRowViewHolder;
import com.github.fielddb.model.Datum;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<DatumRowViewHolder> {
  // protected AdapterView.OnItemClickListener mOnItemClickListener;
  protected Cursor mCursor;

  public CursorRecyclerViewAdapter(Cursor cursor) {
    super();
    this.mCursor = cursor;
  }

  public void onDestroy() {
    mCursor = null;
  }

  @Override
  public int getItemCount() {
    return 3;
  }

  @Override
  public void onBindViewHolder(DatumRowViewHolder itemHolder, int position) {
    Datum datum = new Datum("Testing " + position);

    itemHolder.setOrthography(datum.getOrthography());
    itemHolder.setTranslation(datum.getTranslation());
    itemHolder.setIcon(datum.getMainImageFile());

    itemHolder.setPosition(position);
    itemHolder.setUri(Uri.withAppendedPath(DatumContentProvider.CONTENT_URI, datum.getId()));
  }

  @Override
  public DatumRowViewHolder onCreateViewHolder(ViewGroup container, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(container.getContext());
    View root = inflater.inflate(R.layout.datum_list_row, container, false);

    return new DatumRowViewHolder(root, this);
  }

  public void removeItem(int position, Uri mUri) {
    notifyItemRemoved(position);
  }

  public Cursor getCursor() {
    return mCursor;
  }
}
