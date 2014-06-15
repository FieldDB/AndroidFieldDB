package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.acra.ACRA;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import ca.ilanguage.oprime.datacollection.AudioRecorder;
import ca.ilanguage.oprime.datacollection.TakePicture;
import ca.ilanguage.oprime.datacollection.VideoRecorder;
import ca.ilanguage.oprime.model.DeviceDetails;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.service.UploadAudioVideoService;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.BuildConfig;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;
import com.github.opensourcefieldlinguistics.fielddb.model.Datum;

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

	public static final String ARG_TOTAL_DATUM_IN_LIST = "total_datum_count_in_list";

	/**
	 * The content this fragment is presenting.
	 */
	protected Datum mItem;
	protected Uri mUri;
	public boolean mTwoPane = false;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DatumDetailFragment() {
	}

	protected boolean mRecordingAudio = false;
	protected VideoView mVideoView;
	protected ImageView mImageView;
	protected MediaController mMediaController;
	protected MediaPlayer mAudioPlayer;
	protected DeviceDetails mDeviceDetails;
	protected HashMap<String, Integer> mDatumEditCounts;
	protected ImageButton mSpeechRecognizerFeedback;
	protected TextView mSpeechRecognizerInstructions;
	protected boolean isPlaying = false;

	protected ViewPager mDatumPager;

	protected int mLastDatumIndex;

	private String mAudioFileName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (this.mDeviceDetails == null) {
			this.mDeviceDetails = new DeviceDetails(getActivity(), Config.D,
					Config.TAG);
		}

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			String id = getArguments().getString(ARG_ITEM_ID);
			this.mLastDatumIndex = getArguments().getInt(
					ARG_TOTAL_DATUM_IN_LIST);
			Log.d(Config.TAG, "Will get id " + id);
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			String[] datumProjection = {DatumTable.COLUMN_ORTHOGRAPHY,
					DatumTable.COLUMN_UTTERANCE, DatumTable.COLUMN_MORPHEMES,
					DatumTable.COLUMN_GLOSS, DatumTable.COLUMN_TRANSLATION,
					DatumTable.COLUMN_CONTEXT, DatumTable.COLUMN_IMAGE_FILES,
					DatumTable.COLUMN_AUDIO_VIDEO_FILES, DatumTable.COLUMN_TAGS};
			mUri = Uri.withAppendedPath(DatumContentProvider.CONTENT_URI, id);
			CursorLoader cursorLoader = new CursorLoader(getActivity(), mUri,
					datumProjection, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				Datum datum = new Datum(
						cursor.getString(cursor
								.getColumnIndexOrThrow(DatumTable.COLUMN_ORTHOGRAPHY)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(DatumTable.COLUMN_MORPHEMES)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(DatumTable.COLUMN_GLOSS)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(DatumTable.COLUMN_TRANSLATION)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(DatumTable.COLUMN_CONTEXT)));
				datum.setId(id);
				datum.setUtterance(cursor.getString(cursor
						.getColumnIndexOrThrow(DatumTable.COLUMN_UTTERANCE)));
				datum.addMediaFiles(cursor.getString(cursor
						.getColumnIndexOrThrow(DatumTable.COLUMN_IMAGE_FILES)));
				datum.addMediaFiles((cursor.getString(cursor
						.getColumnIndexOrThrow(DatumTable.COLUMN_AUDIO_VIDEO_FILES))));

				datum.setTagsFromSting(cursor.getString(cursor
						.getColumnIndexOrThrow(DatumTable.COLUMN_TAGS)));
				cursor.close();

				mItem = datum;
				this.recordUserEvent("loadDatum", mUri.getLastPathSegment());
				if (!BuildConfig.DEBUG)
					ACRA.getErrorReporter().putCustomData("urlString",
							mUri.toString());

			}

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_datum_detail,
				container, false);
		if (Config.APP_TYPE.equals("speechrec")) {
			rootView = inflater.inflate(R.layout.fragment_datum_detail_simple,
					container, false);
		}

		if (mItem != null) {
			this.prepareEditTextListeners(rootView);
			this.prepareVideoAndImages(rootView);
			this.prepareSpeechRecognitionButton(rootView);
		}

		return rootView;
	}

	protected void prepareEditTextListeners(View rootView) {
		final EditText orthographyEditText = ((EditText) rootView
				.findViewById(R.id.orthography));
		if (orthographyEditText != null) {
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
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "orthography");
				}
			});
		}

		final EditText morphemesEditText = ((EditText) rootView
				.findViewById(R.id.morphemes));
		if (morphemesEditText != null) {

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
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_MORPHEMES, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "morphemes");
				}
			});
		}

		final EditText glossEditText = ((EditText) rootView
				.findViewById(R.id.gloss));
		if (glossEditText != null) {
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
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_GLOSS, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "gloss");
				}
			});
		}

		final EditText translationEditText = ((EditText) rootView
				.findViewById(R.id.translation));
		if (translationEditText != null) {
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
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_TRANSLATION, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "translation");
				}
			});
		}

		final EditText contextEditText = ((EditText) rootView
				.findViewById(R.id.context));
		if (contextEditText != null) {
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
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_CONTEXT, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "context");
				}
			});
		}

		final EditText tagsEditText = ((EditText) rootView
				.findViewById(R.id.tags));
		if (tagsEditText != null) {
			tagsEditText.setText(mItem.getTagsString());
			tagsEditText.addTextChangedListener(new TextWatcher() {

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
					String currentText = tagsEditText.getText().toString();
					mItem.setTagsFromSting(currentText);
					ContentValues values = new ContentValues();
					values.put(DatumTable.COLUMN_TAGS, currentText);
					getActivity().getContentResolver().update(mUri, values,
							null, null);
					recordUserEvent("editDatum", "tags");
				}
			});
		}
	}

	protected void prepareVideoAndImages(View rootView) {
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
			if (mVideoView != null) {
				mVideoView.setMediaController(mMediaController);
			}
		}
		this.loadVisuals(false);
	}

	protected void prepareSpeechRecognitionButton(View rootView) {

		mSpeechRecognizerFeedback = (ImageButton) rootView
				.findViewById(R.id.speech_recognizer_feedback);
		if (mSpeechRecognizerFeedback != null) {
			mSpeechRecognizerFeedback
					.setImageResource(R.drawable.speech_recognizer_listening);
			mSpeechRecognizerFeedback
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							toggleAudioRecording(null);
						}
					});
		}

		mSpeechRecognizerInstructions = (TextView) rootView
				.findViewById(R.id.speech_recognizer_instructions);
		if (mSpeechRecognizerInstructions != null) {
			mSpeechRecognizerInstructions.setText("");
		}

		this.mDatumPager = (ViewPager) getActivity().findViewById(
				R.id.viewpager);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actions_datum, menu);
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
	protected Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		return intent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_speak :
				return this.toggleAudioRecording(item);
			case R.id.action_play :
				return this.loadMainVideo(true);
			case R.id.action_videos :
				return this.captureVideo();
			case R.id.action_images :
				return this.captureImage();
			case R.id.action_delete :
				return this.delete();
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	public void onToggleAudioRecording(View view) {
		this.toggleAudioRecording(null);
	}

	public boolean toggleAudioRecording(MenuItem item) {
		if (!this.mRecordingAudio) {
			String audioFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
					+ mItem.getBaseFilename() + Config.DEFAULT_AUDIO_EXTENSION;
			this.mAudioFileName = audioFileName;
			Intent intent;
			intent = new Intent(getActivity(), AudioRecorder.class);
			intent.putExtra(Config.EXTRA_RESULT_FILENAME, audioFileName);
			mItem.addAudioFile(audioFileName.replace(
					Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
			getActivity().startService(intent);
			ContentValues values = new ContentValues();
			values.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES,
					mItem.getMediaFilesAsCSV(mItem.getAudioVideoFiles()));
			getActivity().getContentResolver().update(mUri, values, null, null);
			Log.d(Config.TAG, "Recording audio " + audioFileName);
			this.mRecordingAudio = true;
			if (item != null) {
				item.setIcon(R.drawable.ic_action_stop);
			}
			this.recordUserEvent("captureAudio", audioFileName);

			if (mSpeechRecognizerFeedback != null) {
				mSpeechRecognizerFeedback
						.setImageResource(R.drawable.speech_recognizer_recognizing);
			}

			if (mSpeechRecognizerInstructions != null) {
				mSpeechRecognizerInstructions.setText("Tap to end");
			}

		} else {
			Intent audio = new Intent(getActivity(), AudioRecorder.class);
			getActivity().stopService(audio);

			Handler mainHandler = new Handler(getActivity().getMainLooper());
			Runnable launchUploadAudioService = new Runnable() {

				@Override
				public void run() {
					Intent uploadAudioFile = new Intent(getActivity(),
							UploadAudioVideoService.class);
					uploadAudioFile.setData(Uri.parse(mAudioFileName));
					uploadAudioFile.putExtra(Config.EXTRA_PARTICIPANT_ID,
							Config.CURRENT_USERNAME);
					uploadAudioFile.putExtra(
							Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION,
							mDeviceDetails.getCurrentDeviceDetails());
					getActivity().startService(uploadAudioFile);
				}
			};
			mainHandler.postDelayed(launchUploadAudioService, 1000);

			this.mRecordingAudio = false;
			if (item != null) {
				item.setIcon(R.drawable.ic_action_mic);
			}
			this.recordUserEvent("stopAudio", "");

			if (mSpeechRecognizerFeedback != null) {
				mSpeechRecognizerFeedback
						.setImageResource(R.drawable.speech_recognizer_waiting);
			}

			if (mSpeechRecognizerInstructions != null) {
				mSpeechRecognizerInstructions.setText("Tap to speak again");
			}
			if (Config.APP_TYPE.equals("speechrec")) {
				autoAdvanceAfterRecordingAudio();
			}

		}
		return true;
	}

	protected boolean autoAdvanceAfterRecordingAudio() {
		if (this.mDatumPager != null) {
			int currentStimulusIndex = this.mDatumPager.getCurrentItem();
			if (currentStimulusIndex == this.mLastDatumIndex) {
				Intent openDataList = new Intent(getActivity(),
						DatumListActivity.class);
				startActivity(openDataList);
			} else {
				this.mDatumPager.setCurrentItem(this.mDatumPager
						.getCurrentItem() + 1);
			}
		}
		return true;
	}

	protected boolean delete() {
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
								getActivity().getContentResolver().delete(mUri,
										null, null);
								dialog.dismiss();

								if (mTwoPane) {
									getActivity().getSupportFragmentManager()
											.popBackStack();
								} else {
									NavUtils.navigateUpTo(getActivity(),
											new Intent(getActivity(),
													DatumListActivity.class));
								}

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
		return true;
	}

	protected void loadVisuals(boolean playImmediately) {
		loadMainVideo(playImmediately);
	}

	@SuppressLint("NewApi")
	public boolean loadMainVideo(boolean playNow) {
		String fileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getMainAudioVideoFile();
		File audioVideoFile = new File(fileName);
		if (!audioVideoFile.exists()) {
			this.loadMainImage();
			return false;
		}
		if (mVideoView != null) {
			mVideoView.setVideoPath(fileName);
			if (fileName.endsWith(Config.DEFAULT_AUDIO_EXTENSION)) {
				loadMainImage();
			} else {
				int sdk = android.os.Build.VERSION.SDK_INT;
				if (sdk >= 16) {
					mVideoView.setBackground(null);
				} else {
					Log.e(Config.TAG,
							"Couldnt set the video background. (this might be a kindle)");
					mImageView.setImageBitmap(null);
					mImageView.setVisibility(View.VISIBLE);
					mVideoView.setVisibility(View.GONE);
				}
			}
			if (playNow) {
				this.recordUserEvent("loadMainVideo", fileName);

				mVideoView.start();
				mMediaController.setPrevNextListeners(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								String filename = mItem.getPrevNextMediaFile(
										"audio", mItem.getAudioVideoFiles(),
										"next");
								if (filename != null) {
									mVideoView.stopPlayback();
									mVideoView
											.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY
													+ "/" + filename);
									mVideoView.start();
								}
							}
						}, new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								String filename = mItem.getPrevNextMediaFile(
										"audio", mItem.getAudioVideoFiles(),
										"prev");
								if (filename != null) {
									mVideoView.stopPlayback();
									mVideoView
											.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY
													+ "/" + filename);
									mVideoView.start();
								}
							}
						});
			}
		} else {
			Log.d(Config.TAG, "Playing audio only (no video)");
			mAudioPlayer = MediaPlayer.create(
					getActivity(),
					Uri.parse("file://" + Config.DEFAULT_OUTPUT_DIRECTORY + "/"
							+ fileName));
			mAudioPlayer
					.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							mp.start();
						}
					});
		}
		return true;
	}

	protected void loadMainImage() {
		File image = new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getMainImageFile());
		if (!image.exists()) {
			if (mVideoView != null) {
				mVideoView.setVisibility(View.GONE);
			}
			if (mImageView != null) {
				mImageView.setVisibility(View.VISIBLE);
			}
			return;
		}
		Bitmap d = new BitmapDrawable(this.getResources(),
				image.getAbsolutePath()).getBitmap();
		if (d == null) {
			return;
		}
		int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
		Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
		// int sdk = android.os.Build.VERSION.SDK_INT;
		// if (falssdk >= 16) {
		// mVideoView
		// .setBackground(new BitmapDrawable(getResources(), scaled));
		// } else {
		// Log.e(Config.TAG,
		// "Couldnt set the video background. (this might be a kindle)");
		mImageView.setImageBitmap(scaled);
		mImageView.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.GONE);
		// }

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (!(resultCode == Activity.RESULT_OK || resultCode == 65596)) {
		// return;
		// }
		String resultFile;
		switch (requestCode) {
			case Config.CODE_EXPERIMENT_COMPLETED :
				if (data != null && data.hasExtra(Config.EXTRA_RESULT_FILENAME)) {
					resultFile = data.getExtras().getString(
							Config.EXTRA_RESULT_FILENAME);
					if (resultFile != null) {
						// if (resultFile != null && new
						// File(resultFile).exists())
						// {
						if (resultFile.endsWith(Config.DEFAULT_AUDIO_EXTENSION)) {
							mItem.addAudioFile(resultFile.replace(
									Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
						} else {
							mItem.addVideoFile(resultFile.replace(
									Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
						}
						ContentValues values = new ContentValues();
						values.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES, mItem
								.getMediaFilesAsCSV(mItem.getAudioVideoFiles()));
						getActivity().getContentResolver().update(mUri, values,
								null, null);
						this.loadMainVideo(false);
					}
				}
				break;
			case Config.CODE_PICTURE_TAKEN :
				if (data != null && data.hasExtra(Config.EXTRA_RESULT_FILENAME)) {
					resultFile = data.getExtras().getString(
							Config.EXTRA_RESULT_FILENAME);
					if (resultFile != null) {
						// if (resultFile != null && new
						// File(resultFile).exists())
						// {
						mItem.addImageFile(resultFile.replace(
								Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
						ContentValues values = new ContentValues();
						values.put(DatumTable.COLUMN_IMAGE_FILES,
								mItem.getMediaFilesAsCSV(mItem.getImageFiles()));
						getActivity().getContentResolver().update(mUri, values,
								null, null);
						this.loadMainImage();
					}
				}
				break;
		}
		super.onActivityResult(requestCode, requestCode, data);
	}

	protected boolean captureVideo() {
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
		this.recordUserEvent("captureVideo", videoFileName);
		return true;
	}

	protected boolean captureImage() {
		String imageFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/"
				+ mItem.getBaseFilename() + Config.DEFAULT_IMAGE_EXTENSION;
		Intent intent = new Intent(getActivity(), TakePicture.class);
		intent.putExtra(Config.EXTRA_RESULT_FILENAME, imageFileName);
		startActivityForResult(intent, Config.CODE_PICTURE_TAKEN);
		this.recordUserEvent("captureImage", imageFileName);
		return true;
	}

	@Override
	public void onPause() {
		if (this.mDatumEditCounts != null) {
			String edits = "";
			Iterator it = this.mDatumEditCounts.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (!"".equals(edits)) {
					edits = edits + ",";
				}
				edits = edits + "{" + pair.getKey() + " : " + pair.getValue()
						+ "}";
				it.remove(); // avoids a ConcurrentModificationException
			}
			edits = "[" + edits + "]";
			recordUserEvent("totalDatumEditsOnPause", edits);
		}

		if (mAudioPlayer != null) {
			// if (mPrompt.isPlaying()) {
			// mPrompt.stop();
			// }
			mAudioPlayer.release();
		}

		super.onPause();
	}

	protected void recordUserEvent(String eventType, String eventValue) {
		if ("editDatum".equals(eventType)) {
			if (this.mDatumEditCounts == null) {
				this.mDatumEditCounts = new HashMap<String, Integer>();
			}
			Integer count = 1;
			if (this.mDatumEditCounts.containsKey(eventValue)) {
				count = this.mDatumEditCounts.get(eventValue) + 1;
			}
			this.mDatumEditCounts.put(eventValue, count);
			return;
		}
		if (!BuildConfig.DEBUG) {
			ACRA.getErrorReporter().putCustomData("action",
					"{\"" + eventType + "\" : \"" + eventValue + "\"}");
			ACRA.getErrorReporter().putCustomData("androidTimestamp",
					System.currentTimeMillis() + "");
			ACRA.getErrorReporter().putCustomData("deviceDetails",
					this.mDeviceDetails.getCurrentDeviceDetails());
			ACRA.getErrorReporter().handleException(
					new Exception("*** User event " + eventType + " ***"));
		}
	}
}
