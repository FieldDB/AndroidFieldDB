package ca.ilanguage.oprime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import ca.ilanguage.oprime.database.DataCollectionContent;

/**
 * A fragment representing a single Experiment detail screen. This fragment is
 * either contained in a {@link ExperimentListActivity} in two-pane mode (on
 * tablets) or a {@link ExperimentActivity} on handsets.
 */
public class ExperimentFragment extends Fragment {
  /**
   * The fragment argument representing the item ID that this fragment
   * represents.
   */
  public static final String                       ARG_ITEM_ID = "item_id";

  /**
   * The dummy content this fragment is presenting.
   */
  private DataCollectionContent.DataCollectionItem mItem;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ExperimentFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (this.getArguments().containsKey(ARG_ITEM_ID)) {
      // Load the dummy content specified by the fragment
      // arguments. In a real-world scenario, use a Loader
      // to load content from a content provider.
      this.mItem = DataCollectionContent.ITEM_MAP.get(this.getArguments().getString(ARG_ITEM_ID));
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView;
    if (this.mItem == null) {
      rootView = inflater.inflate(R.layout.fragment_experiment_detail, container, false);
      ((TextView) rootView.findViewById(R.id.experiment_detail)).setText("Error");
      return rootView;
    }

    /* Use the layout defined in the experiment itself. */
    rootView = inflater.inflate(mItem.layout_resource, container, false);
    TextView text = ((TextView) rootView.findViewById(R.id.experiment_detail));
    if (text != null) {
      text.setText(mItem.content);
    }

    return rootView;
  }


  @Override
  public void onDestroy() {
    Intent i = new Intent(Config.INTENT_STOP_VIDEO_RECORDING);
    getActivity().sendBroadcast(i);
    
    Log.d(Config.TAG, "Requesting video recording to exit from the destroy of the experiment fragment.");
    super.onDestroy();
  }

}
