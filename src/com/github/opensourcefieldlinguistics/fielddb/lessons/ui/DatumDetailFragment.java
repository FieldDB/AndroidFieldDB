package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.io.File;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.TakePicture;
import ca.ilanguage.oprime.datacollection.VideoRecorder;

import com.github.opensourcefieldlinguistics.fielddb.content.Datum;
import com.github.opensourcefieldlinguistics.fielddb.content.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.content.PlaceholderContent;
import com.github.opensourcefieldlinguistics.fielddb.content.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
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

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DatumDetailFragment() {
	}

	private String TAG = "FieldDB";
	private boolean mRecordingAudio = false;
	private VideoView mVideoView;
	private ImageView mImageView;
	private MediaController mMediaController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.

//			 public String getPath(Uri uri) {
			
			 String selection = null;
			 String[] selectionArgs = null;
			 String sortOrder = null;
			
			 String[] projection = { DatumTable.COLUMN_MORPHEMES };
			 CursorLoader cursorLoader = new CursorLoader(getActivity(), DatumContentProvider.CONTENT_URI,
			 projection, selection, selectionArgs, sortOrder);
			
			 Cursor cursor = cursorLoader.loadInBackground();
			
			 int column_index =
			 cursor.getColumnIndexOrThrow(DatumTable.COLUMN_MORPHEMES);
			 cursor.moveToFirst();
//			 return cursor.getString(column_index);
//			 }

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
			if (mImageView == null) {
				mImageView = (ImageView) rootView.findViewById(R.id.image_view);
			}
			if (mMediaController == null) {
				mMediaController = new MediaController(getActivity());
				mMediaController.setAnchorView((VideoView) rootView
						.findViewById(R.id.video_view));
				// mMediaController.setPadding(0, 0, 0, 200);
			}
			if (mVideoView == null) {
				mVideoView = (VideoView) rootView.findViewById(R.id.video_view);
				mVideoView.setMediaController(mMediaController);
			}
			this.loadVisuals(false);
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
		case R.id.action_speak:
			if (!this.mRecordingAudio) {
				String audioFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
						+ mItem.getBaseFilename()
						+ Config.DEFAULT_AUDIO_EXTENSION;
				Intent intent;
				intent = new Intent(getActivity(), AudioRecorder.class);
				intent.putExtra(Config.EXTRA_RESULT_FILENAME, audioFileName);
				mItem.addAudioFile(audioFileName.replace(
						Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
				getActivity().startService(intent);
				Log.e(TAG, "Recording audio " + audioFileName);
				this.mRecordingAudio = true;
				item.setIcon(R.drawable.ic_action_stop);
			} else {
				Intent audio = new Intent(getActivity(), AudioRecorder.class);
				getActivity().stopService(audio);
				this.mRecordingAudio = false;
				item.setIcon(R.drawable.ic_action_mic);
			}
			return true;
		case R.id.action_play:
			return this.loadMainVideo(true);
		case R.id.action_videos:
			return this.captureVideo();
		case R.id.action_images:

			return this.captureImage();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadVisuals(boolean playImmediately) {
		loadMainVideo(playImmediately);
	}

	public boolean loadMainVideo(boolean playNow) {
		String fileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getMainAudioVideoFile();
		File audioVideoFile = new File(fileName);
		if (!audioVideoFile.exists()) {
			this.loadMainImage();
			return false;
		}
		mVideoView.setVideoPath(fileName);
		if (fileName.endsWith(Config.DEFAULT_AUDIO_EXTENSION)) {
			loadMainImage();
		} else {
			mVideoView.setBackground(null);
		}
		if (playNow) {
			mVideoView.start();
			mMediaController.setPrevNextListeners(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String filename = mItem.getPrevNextMediaFile("audio",
							mItem.getAudioVideoFiles(), "next");
					if (filename != null) {
						mVideoView.stopPlayback();
						mVideoView.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY
								+ "/" + filename);
						mVideoView.start();
					}
				}
			}, new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String filename = mItem.getPrevNextMediaFile("audio",
							mItem.getAudioVideoFiles(), "prev");
					if (filename != null) {
						mVideoView.stopPlayback();
						mVideoView.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY
								+ "/" + filename);
						mVideoView.start();
					}
				}
			});
		}
		return true;
	}

	private void loadMainImage() {
		File image = new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getMainImageFile());
		if (image.exists()) {
			Bitmap d = new BitmapDrawable(this.getResources(),
					image.getAbsolutePath()).getBitmap();
			int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
			Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
			// mImageView.setImageBitmap(scaled);
			// mImageView.setVisibility(View.VISIBLE);
			// mVideoView.setVisibility(View.GONE);
			mVideoView
					.setBackground(new BitmapDrawable(getResources(), scaled));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (!(resultCode == Activity.RESULT_OK || resultCode == 65596)) {
		// return;
		// }
		String resultFile;
		switch (requestCode) {
		case Config.CODE_EXPERIMENT_COMPLETED:
			if (data != null && data.hasExtra(Config.EXTRA_RESULT_FILENAME)) {
				resultFile = data.getExtras().getString(
						Config.EXTRA_RESULT_FILENAME);
				if (resultFile != null && new File(resultFile).exists()) {
					if (resultFile.endsWith(Config.DEFAULT_AUDIO_EXTENSION)) {
						mItem.addAudioFile(resultFile.replace(
								Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
					} else {
						mItem.addVideoFile(resultFile.replace(
								Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
					}
					this.loadMainVideo(false);
				}
			}
			break;
		case Config.CODE_PICTURE_TAKEN:
			if (data != null && data.hasExtra(Config.EXTRA_RESULT_FILENAME)) {
				resultFile = data.getExtras().getString(
						Config.EXTRA_RESULT_FILENAME);
				if (resultFile != null && new File(resultFile).exists()) {
					mItem.addImageFile(resultFile.replace(
							Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
					this.loadMainImage();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, requestCode, data);
	}

	private boolean captureVideo() {
		String videoFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getBaseFilename() + Config.DEFAULT_VIDEO_EXTENSION;
		Intent intent = new Intent(getActivity(), VideoRecorder.class);
		intent.putExtra(Config.EXTRA_USE_FRONT_FACING_CAMERA, true);
		intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
		intent.putExtra(Config.EXTRA_RESULT_FILENAME, videoFileName);
		intent.putExtra(Config.EXTRA_PARTICIPANT_ID,
				Config.DEFAULT_PARTICIPANT_ID);
		intent.putExtra(Config.EXTRA_OUTPUT_DIR,
				Config.DEFAULT_OUTPUT_DIRECTORY);
		intent.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION, "");
		startActivityForResult(intent, Config.CODE_EXPERIMENT_COMPLETED);
		return true;
	}

	private boolean captureImage() {
		String imageFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getBaseFilename() + Config.DEFAULT_IMAGE_EXTENSION;
		Intent intent = new Intent(getActivity(), TakePicture.class);
		intent.putExtra(Config.EXTRA_RESULT_FILENAME, imageFileName);
		startActivityForResult(intent, Config.CODE_PICTURE_TAKEN);
		return true;
	}
}
