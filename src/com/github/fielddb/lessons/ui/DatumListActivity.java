package com.github.fielddb.lessons.ui;

import com.github.fielddb.Config;
import com.github.fielddb.database.DatumContentProvider;
import com.github.fielddb.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * An activity representing a list of Datums. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link DatumDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DatumListFragment} and the item details (if present) is a
 * {@link DatumDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link DatumListFragment.Callbacks} interface to listen for item selections.
 */
public class DatumListActivity extends FragmentActivity implements
        DatumListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Uri mVisibleDatumUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datum_list);

        if (findViewById(R.id.datum_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((DatumListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.datum_list)).setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link DatumListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {

        Bundle arguments = new Bundle();
        mVisibleDatumUri = Uri.parse(DatumContentProvider.CONTENT_URI + "/"
                + id);
        arguments.putParcelable(DatumContentProvider.CONTENT_ITEM_TYPE,
                mVisibleDatumUri);
        arguments.putString(DatumDetailFragment.ARG_ITEM_ID, id);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            DatumDetailFragment fragment = new DatumDetailFragment();
            fragment.mTwoPane = mTwoPane;
            fragment.setArguments(arguments);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.datum_detail_container, fragment, Config.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, DatumDetailActivity.class);
            detailIntent.putExtras(arguments);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onItemDeleted(Uri uri) {
        if (mTwoPane) {
            /* if the deleted item was showing in the fragment, then remove it */
            if (mVisibleDatumUri != null && mVisibleDatumUri.equals(uri)) {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().remove(fm.findFragmentByTag(Config.TAG))
                        .commit();
            }
        }
    }
}
