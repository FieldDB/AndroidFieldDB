package com.github.fielddb.database;

import com.github.fielddb.R;
import com.github.fielddb.lessons.ui.DatumListFragment.Callbacks;
import com.github.fielddb.lessons.ui.DatumPreviewViewHolder;
import com.github.fielddb.model.Datum;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<DatumPreviewViewHolder> {
  // protected AdapterView.OnItemClickListener mOnItemClickListener;
  protected Cursor mCursor;
  protected Callbacks mClickCallbacks;

  public CursorRecyclerViewAdapter(Callbacks clickCallbacks, Cursor cursor) {
    super();
    this.mCursor = cursor;
    this.mClickCallbacks = clickCallbacks;
  }

  public void onDestroy() {
    mCursor = null;
  }

  @Override
  public int getItemCount() {
    if (mCursor == null) {
      return 0;
    }
    return 3;
  }

  @Override
  public void onBindViewHolder(DatumPreviewViewHolder itemHolder, int position) {
    mCursor.moveToPosition(position);
    // TODO consider not using the whole Datum model for the previews
    Datum datum = new Datum(mCursor);

    itemHolder.setOrthography(datum.getOrthography());
    itemHolder.setTranslation(datum.getTranslation());
    itemHolder.setIcon(datum.getMainImageFile());

    itemHolder.setPosition(position);
    itemHolder.setUri(Uri.withAppendedPath(DatumContentProvider.CONTENT_URI, datum.getId()));
  }

  @Override
  public DatumPreviewViewHolder onCreateViewHolder(ViewGroup container, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(container.getContext());
    View root = inflater.inflate(R.layout.datum_list_row, container, false);

    return new DatumPreviewViewHolder(root, this);
  }

  public void onItemHolderClick(String id) {
    mClickCallbacks.onItemSelected(id);
  }

  public void onItemHolderContextClick(DatumPreviewViewHolder item) {

  }

  public void removeItem(int position, Uri mUri) {
    notifyItemRemoved(position);
  }

  public Cursor getCursor() {
    return mCursor;
  }
}
