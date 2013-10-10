package ca.ilanguage.oprime.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.ilanguage.oprime.database.DataCollectionContent;

/**
 * A list fragment representing a list of Experiments. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link ExperimentFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ExperimentListFragment extends ListFragment {

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
  }

  /**
   * A placeholder implementation of the {@link Callbacks} interface that does
   * nothing. Used only when this fragment is not attached to an activity.
   */
  private static Callbacks    sDataCollectionCallbacks = new Callbacks() {
                                                         @Override
                                                         public void onItemSelected(String id) {
                                                         }
                                                       };

  /**
   * The serialization (saved instance state) Bundle key representing the
   * activated item position. Only used on tablets.
   */
  private static final String STATE_ACTIVATED_POSITION = "activated_position";

  /**
   * The current activated item position. Only used on tablets.
   */
  private int                 mActivatedPosition       = ListView.INVALID_POSITION;

  /**
   * The fragment's current callback object, which is notified of list item
   * clicks.
   */
  private Callbacks           mCallbacks               = sDataCollectionCallbacks;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ExperimentListFragment() {
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // Activities containing this fragment must implement its callbacks.
    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }

    this.mCallbacks = (Callbacks) activity;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: replace with a real list adapter.
    this.setListAdapter(new ArrayAdapter<DataCollectionContent.DataCollectionItem>(this.getActivity(),
        android.R.layout.simple_list_item_activated_1, android.R.id.text1, DataCollectionContent.ITEMS));
  }

  @Override
  public void onDetach() {
    super.onDetach();

    // Reset the active callbacks interface to the placeholder implementation.
    this.mCallbacks = sDataCollectionCallbacks;
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    // Notify the active callbacks interface (the activity, if the
    // fragment is attached to one) that an item has been selected.
    this.mCallbacks.onItemSelected(DataCollectionContent.ITEMS.get(position).id);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (this.mActivatedPosition != AdapterView.INVALID_POSITION) {
      // Serialize and persist the activated item position.
      outState.putInt(STATE_ACTIVATED_POSITION, this.mActivatedPosition);
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Restore the previously serialized activated item position.
    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      this.setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
  }

  private void setActivatedPosition(int position) {
    if (position == AdapterView.INVALID_POSITION) {
      this.getListView().setItemChecked(this.mActivatedPosition, false);
    } else {
      this.getListView().setItemChecked(position, true);
    }

    this.mActivatedPosition = position;
  }

  /**
   * Turns on activate-on-click mode. When this mode is on, list items will be
   * given the 'activated' state when touched.
   */
  public void setActivateOnItemClick(boolean activateOnItemClick) {
    // When setting CHOICE_MODE_SINGLE, ListView will automatically
    // give items the 'activated' state when touched.
    this.getListView().setChoiceMode(
        activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
  }
}
