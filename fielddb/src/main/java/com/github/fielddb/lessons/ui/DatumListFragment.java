package com.github.fielddb.lessons.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.fielddb.Config;
import com.github.fielddb.database.CursorRecyclerViewAdapter;
import com.github.fielddb.database.DatumContentProvider;
import com.github.fielddb.database.DatumContentProvider.DatumTable;
import com.github.fielddb.BugReporter;
import com.github.fielddb.R;

/**
 * A list fragment representing a list of Datums. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link DatumDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DatumListFragment extends Fragment implements AdapterView.OnItemClickListener {
  private RecyclerView mList;
  private CursorRecyclerViewAdapter mAdapter;

  /**
   * The serialization (saved instance state) Bundle key representing the
   * activated item position. Only used on tablets.
   */
  private static final String STATE_ACTIVATED_POSITION = "activated_position";

  /**
   * The fragment's current callback object, which is notified of list item
   * clicks.
   */
  private Callbacks mCallbacks = sDummyCallbacks;

  /**
   * The current activated item position. Only used on tablets.
   */
  private int mActivatedPosition = ListView.INVALID_POSITION;

  /**
   * A callback interface that all activities containing this fragment must
   * implement. This mechanism allows activities to be notified of item
   * selections.
   */
  public interface Callbacks {
    /**
     * Callback for when an item has been selected.
     */
    public void onItemSelected(String id);

    public void onItemDeleted(Uri uri);

  }

  /**
   * A dummy implementation of the {@link Callbacks} interface that does
   * nothing. Used only when this fragment is not attached to an activity.
   */
  private static Callbacks sDummyCallbacks = new Callbacks() {
    @Override
    public void onItemSelected(String id) {
    }

    @Override
    public void onItemDeleted(Uri uri) {
    }
  };

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DatumListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    // setListAdapter(new DatumRowArrayAdapter(getActivity(),
    // PlaceholderContent.ITEMS));
  }

  @Override
  @Nullable
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    getActivity().getApplicationContext().setTheme(R.style.AppTheme);
    getActivity().setTheme(R.style.AppTheme);

    View rootView = inflater.inflate(R.layout.fragment_datum_list, container, false);
    mList = (RecyclerView) rootView.findViewById(R.id.section_list);
    mList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    // mList.addItemDecoration(new DividerDecoration(getActivity());

    mList.getItemAnimator().setAddDuration(1000);
    mList.getItemAnimator().setChangeDuration(1000);
    mList.getItemAnimator().setMoveDuration(1000);
    mList.getItemAnimator().setRemoveDuration(1000);

    String[] projection = { DatumTable.COLUMN_ID, DatumTable.COLUMN_ORTHOGRAPHY, DatumTable.COLUMN_TRANSLATION,
        DatumTable.COLUMN_IMAGE_FILES };
    CursorLoader loader = new CursorLoader(getActivity(), DatumContentProvider.CONTENT_URI, projection, null, null,
        null);
    Cursor cursor = loader.loadInBackground();

    if (cursor == null) {
      Log.e(Config.TAG, "The cursor is null, maybe your provider is not declared in the AndroidManifest?");
      loader = null;
    }
    mAdapter = new CursorRecyclerViewAdapter(mCallbacks, cursor);
    // mAdapter.setOnItemClickListener(this);
    mList.setAdapter(mAdapter);
    com.github.fielddb.model.Activity.sendActivity("loaded", "datalist");

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    registerForContextMenu(getView());
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Restore the previously serialized activated item position.
    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // Activities containing this fragment must implement its callbacks.
    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }

    mCallbacks = (Callbacks) activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    // Reset the active callbacks interface to the dummy implementation.
    mCallbacks = sDummyCallbacks;
  }

  @Override
  public void onDestroy() {
    mAdapter.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Toast.makeText(getActivity(), "Clicked: " + position + ", index " + mList.indexOfChild(view), Toast.LENGTH_SHORT)
        .show();
    Log.d(Config.TAG, "TODO Test this");
    // Notify the active callbacks interface (the activity, if the
    // fragment is attached to one) that an item has been selected.
    mAdapter.getCursor().moveToPosition(position);
    String actualId = mAdapter.getCursor().getString(mAdapter.getCursor().getColumnIndexOrThrow(DatumTable.COLUMN_ID));
    mCallbacks.onItemSelected(actualId);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mActivatedPosition != ListView.INVALID_POSITION) {
      // Serialize and persist the activated item position.
      outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
    }
  }

  /**
   * Turns on activate-on-click mode. When this mode is on, list items will be
   * given the 'activated' state when touched.
   */
  public void setActivateOnItemClick(boolean activateOnItemClick) {
    // When setting CHOICE_MODE_SINGLE, ListView will automatically
    // give items the 'activated' state when touched.
    // getView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
    // : ListView.CHOICE_MODE_NONE);
  }

  private void setActivatedPosition(int position) {
    // if (position == ListView.INVALID_POSITION) {
    // getView().setItemChecked(mActivatedPosition, false);
    // } else {
    // getView().setItemChecked(position, true);
    // }

    mActivatedPosition = position;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.actions_list, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle item selection
    if (item.getItemId() == R.id.action_new) {
      Uri newDatum = getActivity().getContentResolver().insert(DatumContentProvider.CONTENT_URI, new ContentValues());
      if (newDatum != null) {
        mCallbacks.onItemSelected(newDatum.getLastPathSegment());
        mAdapter.notifyItemInserted(mAdapter.getItemCount() + 1);
      } else {
        BugReporter.sendBugReport("*** Error inserting a datum in DB ***");
      }
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
