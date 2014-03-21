package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.github.opensourcefieldlinguistics.fielddb.content.Datum;
import com.github.opensourcefieldlinguistics.fielddb.content.PlaceholderContent;
import com.github.opensourcefieldlinguistics.fielddb.lessons.R;

/**
 * A fragment representing a single Datum detail screen. This fragment is either
 * contained in a {@link DatumListActivity} in two-pane mode (on tablets) or a
 * {@link DatumDetailActivity} on handsets.
 */
public class DatumDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Datum mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DatumDetailFragment() {
	}

	private ShareActionProvider mShareActionProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.

			// public String getPath(Uri uri) {
			//
			// String selection = null;
			// String[] selectionArgs = null;
			// String sortOrder = null;
			//
			// String[] projection = { MediaColumns.DATA };
			// CursorLoader cursorLoader = new CursorLoader(this, uri,
			// projection, selection, selectionArgs, sortOrder);
			//
			// Cursor cursor = cursorLoader.loadInBackground();
			//
			// int column_index =
			// cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			// cursor.moveToFirst();
			// return cursor.getString(column_index);
			// }

			mItem = PlaceholderContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_datum_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.orthography)).setText(mItem
					.getOrthography());
			((TextView) rootView.findViewById(R.id.morphemes)).setText(mItem
					.getMorphemes());
			((TextView) rootView.findViewById(R.id.gloss)).setText(mItem
					.getGloss());
			((TextView) rootView.findViewById(R.id.translation)).setText(mItem
					.getTranslation());
			((TextView) rootView.findViewById(R.id.context)).setText(mItem
					.getContext());
			File image = new File("/sdcard/FieldDB/" + mItem.getMainImage());
			if (image.exists()) {
				ImageView iv = (ImageView) rootView
						.findViewById(R.id.image_view);
				Bitmap d = new BitmapDrawable(this.getResources(),
						image.getAbsolutePath()).getBitmap();
				int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
				Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
				iv.setImageBitmap(scaled);
			}
		}

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actions_lessons, menu);
		// // https://developer.android.com/guide/topics/ui/actionbar.html
		// MenuItem searchItem = menu.findItem(R.id.action_search);
		// SearchView searchView = (SearchView) MenuItemCompat
		// .getActionView(searchItem);
		// // Configure the search info and add any event listeners

		// // Set up ShareActionProvider's default share intent
		// https://developer.android.com/guide/topics/ui/actionbar.html
		// MenuItem shareItem = menu.findItem(R.id.action_share);
		// mShareActionProvider = (ShareActionProvider) MenuItemCompat
		// .getActionProvider(shareItem);
		// mShareActionProvider.setShareIntent(getDefaultIntent());
	}

	/**
	 * Defines a default (dummy) share intent to initialize the action provider.
	 * However, as soon as the actual content to be used in the intent is known
	 * or changes, you must update the share intent by again calling
	 * mShareActionProvider.setShareIntent()
	 */
	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		return intent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
