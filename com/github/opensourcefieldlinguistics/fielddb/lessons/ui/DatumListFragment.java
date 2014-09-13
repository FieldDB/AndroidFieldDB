package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import org.acra.ACRA;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.R;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.BuildConfig;

/**
 * A list fragment representing a list of Datums. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link DatumDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DatumListFragment extends ListFragment
		implements
			LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

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
		fillData();
		// setListAdapter(new DatumRowArrayAdapter(getActivity(),
		// PlaceholderContent.ITEMS));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
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
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		adapter.getCursor().moveToPosition(position);
		String actualId = adapter.getCursor()
				.getString(
						adapter.getCursor().getColumnIndexOrThrow(
								DatumTable.COLUMN_ID));
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
		getListView().setChoiceMode(
				activateOnItemClick
						? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete :
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
						.getMenuInfo();
				adapter.getCursor().moveToPosition(info.position);
				String actualId = adapter.getCursor().getString(
						adapter.getCursor().getColumnIndexOrThrow(
								DatumTable.COLUMN_ID));
				final Uri uri = Uri.parse(DatumContentProvider.CONTENT_URI
						+ "/" + actualId);
				AlertDialog deleteConfirmationDialog = new AlertDialog.Builder(
						getActivity())
						.setTitle("Are you sure?")
						.setMessage(
								"Are you sure you want to put this "
										+ Config.USER_FRIENDLY_DATA_NAME
										+ " in the trash?")
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										getActivity().getContentResolver()
												.delete(uri, null, null);
										fillData();
										mCallbacks.onItemDeleted(uri);
										dialog.dismiss();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).create();
				deleteConfirmationDialog.show();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.actions_context_select, menu);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	private void fillData() {
		String[] from = new String[]{DatumTable.COLUMN_ORTHOGRAPHY};
		int[] to = new int[]{android.R.id.text1};
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_activated_1, null, from, to,
				0);
		setListAdapter(adapter);
		if (!BuildConfig.DEBUG) ACRA.getErrorReporter().handleException(
				new Exception("*** User load datum list ***"));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {DatumTable.COLUMN_ID,
				DatumTable.COLUMN_ORTHOGRAPHY, DatumTable.COLUMN_TRANSLATION};
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				DatumContentProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// this.getListView().invalidate();
		adapter.swapCursor(data);
		// http://stackoverflow.com/questions/14867324/update-listview-after-update-database-sqlite
		// this.getListView().invalidate();
		// adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actions_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_new :
				Uri newDatum = getActivity().getContentResolver().insert(
						DatumContentProvider.CONTENT_URI, new ContentValues());
				if (newDatum != null) {
					mCallbacks.onItemSelected(newDatum.getLastPathSegment());
				} else {
					if (!BuildConfig.DEBUG) ACRA.getErrorReporter().handleException(
							new Exception(
									"*** Error inserting a datum in DB ***"));
				}
				return true;
			default :
				return super.onOptionsItemSelected(item);
		}
	}
}
