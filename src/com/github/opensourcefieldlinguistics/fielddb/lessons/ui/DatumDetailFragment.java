package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.github.opensourcefieldlinguistics.fielddb.content.Datum;
import com.github.opensourcefieldlinguistics.fielddb.content.PlaceholderContent;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.R;

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
	private Bitmap mPlaceHolderBitmap;

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

			final EditText orthographyEditText = ((EditText) rootView
					.findViewById(R.id.orthography));
			orthographyEditText.setText(mItem.getOrthography());
			orthographyEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = orthographyEditText.getText()
							.toString();
					mItem.setOrthography(currentText);
				}
			});

			final EditText morphemesEditText = ((EditText) rootView
					.findViewById(R.id.morphemes));
			morphemesEditText.setText(mItem.getMorphemes());
			morphemesEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = morphemesEditText.getText().toString();
					mItem.setMorphemes(currentText);
				}
			});

			final EditText glossEditText = ((EditText) rootView
					.findViewById(R.id.gloss));
			glossEditText.setText(mItem.getGloss());
			glossEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = glossEditText.getText().toString();
					mItem.setGloss(currentText);
				}
			});
			((EditText) rootView.findViewById(R.id.gloss)).setText(mItem
					.getGloss());

			final EditText translationEditText = ((EditText) rootView
					.findViewById(R.id.translation));
			translationEditText.setText(mItem.getTranslation());
			translationEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = translationEditText.getText()
							.toString();
					mItem.setTranslation(currentText);
				}
			});
			((EditText) rootView.findViewById(R.id.translation)).setText(mItem
					.getTranslation());

			final EditText contextEditText = ((EditText) rootView
					.findViewById(R.id.context));
			contextEditText.setText(mItem.getContext());
			contextEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					String currentText = contextEditText.getText().toString();
					mItem.setContext(currentText);
				}
			});
			((EditText) rootView.findViewById(R.id.context)).setText(mItem
					.getContext());
			String filename = mItem.getMainImage();
			File image = new File("/sdcard/FieldDB/" + filename);
			if (image.exists()) {
				ImageView iv = (ImageView) rootView
						.findViewById(R.id.image_view);
				// Bitmap d = new BitmapDrawable(this.getResources(),
				// image.getAbsolutePath()).getBitmap();
				// int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
				// Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
				// iv.setImageBitmap(scaled);

				BitmapWorkerTask loadBitMapSafely = new BitmapWorkerTask(iv);
				loadBitMapSafely.setFilename(filename);
				loadBitMapSafely.execute();
			}
		}

		return rootView;
	}

	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId);
		}
	}

	public static boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.data;
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == 0 || bitmapData != data) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	private static Bitmap decodeSampledBitmapFromFile(Resources resources,
			String filename, int width, int height) {

		File image = new File("/sdcard/FieldDB/" + filename);

		Bitmap d = new BitmapDrawable(resources, image.getAbsolutePath())
				.getBitmap();
		int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
		Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);

		return scaled;
	}

	/**
	 * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
	 */
	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int data = 0;
		private String filename = "unknown.jpg";

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(Integer... params) {
			// data = params[0];
			return decodeSampledBitmapFromFile(getResources(), filename, 100,
					100);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
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
