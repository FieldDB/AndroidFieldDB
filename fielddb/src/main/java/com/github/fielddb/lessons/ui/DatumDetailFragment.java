package com.github.fielddb.lessons.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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

import com.github.fielddb.BuildConfig;
import com.github.fielddb.Config;
import com.github.fielddb.database.DatumContentProvider;
import com.github.fielddb.database.DatumContentProvider.DatumTable;
import com.github.fielddb.datacollection.AudioRecorder;
import com.github.fielddb.datacollection.DeviceDetails;
import com.github.fielddb.datacollection.TakePicture;
import com.github.fielddb.datacollection.VideoRecorder;
import com.github.fielddb.model.Datum;
import com.github.fielddb.service.UploadAudioVideoService;
import com.github.fielddb.BugReporter;
import com.github.fielddb.R;

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

  protected String mAudioFileName;

  @SuppressLint("NewApi")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    if (this.mDeviceDetails == null) {
      this.mDeviceDetails = new DeviceDetails(getActivity());
    }

    if (getArguments() == null) {
      Log.e(Config.TAG, "Agruments were empty, displaying nothing on the screen. this is a problem. ");
      return;
    }

    if (!getArguments().containsKey(ARG_ITEM_ID)) {
      Log.e(Config.TAG, "Agruments were missig ARG_ITEM_ID, displaying nothing on the screen. this is a problem. ");
      return;
    }

    String id = getArguments().getString(ARG_ITEM_ID);
    this.mLastDatumIndex = getArguments().getInt(ARG_TOTAL_DATUM_IN_LIST);
    Log.d(Config.TAG, "Will get id " + id);
    String selection = null;
    String[] selectionArgs = null;
    String sortOrder = null;

    String[] datumProjection = { DatumTable.COLUMN_ID, DatumTable.COLUMN_ORTHOGRAPHY, DatumTable.COLUMN_UTTERANCE,
        DatumTable.COLUMN_MORPHEMES, DatumTable.COLUMN_GLOSS, DatumTable.COLUMN_TRANSLATION,
        DatumTable.COLUMN_CONTEXT, DatumTable.COLUMN_IMAGE_FILES, DatumTable.COLUMN_AUDIO_VIDEO_FILES, DatumTable.COLUMN_VALIDATION_STATUS,
        DatumTable.COLUMN_TAGS };
    mUri = Uri.withAppendedPath(DatumContentProvider.CONTENT_URI, id);
    CursorLoader cursorLoader = new CursorLoader(getActivity(), mUri, datumProjection, selection, selectionArgs,
        sortOrder);

    Cursor cursor = cursorLoader.loadInBackground();
    if (cursor != null) {
      cursor.moveToFirst();
      if (cursor.getCount() > 0) {
        mItem = new Datum(cursor);
        cursor.close();
        this.recordUserEvent("loadDatum", mUri.getLastPathSegment());
        BugReporter.putCustomData("urlString", mUri.toString());
      } else {
        Log.e(Config.TAG, "Displaying nothing on the screen. this is a problem. ");
        BugReporter.sendBugReport("*** couldnt open" + mUri + " ***");
      }
      cursor.close();
    } else {
      Log.e(Config.TAG, "unable to open the datums content provider. this is a problem. ");
      BugReporter.sendBugReport("*** datumCursor is null ***");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_datum_detail, container, false);
    if (Config.APP_TYPE.equals("speechrecognition")) {
      rootView = inflater.inflate(R.layout.fragment_datum_detail_simple, container, false);
    }

    this.prepareVideoAndImageViews(rootView);

    if (mItem != null) {
      this.loadVisuals(false);
      this.prepareEditTextListeners(rootView);
      this.prepareSpeechRecognitionButton(rootView);
    }

    return rootView;
  }

  protected void prepareEditTextListeners(View rootView) {
    final EditText orthographyEditText = ((EditText) rootView.findViewById(R.id.orthography));
    if (orthographyEditText != null) {
      orthographyEditText.setText(mItem.getOrthography());
      orthographyEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = orthographyEditText.getText().toString();
          mItem.setOrthography(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_ORTHOGRAPHY, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "orthography");
        }
      });
    }

    final EditText morphemesEditText = ((EditText) rootView.findViewById(R.id.morphemes));
    if (morphemesEditText != null) {

      morphemesEditText.setText(mItem.getMorphemes());
      morphemesEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = morphemesEditText.getText().toString();
          mItem.setMorphemes(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_MORPHEMES, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "morphemes");
        }
      });
    }

    final EditText glossEditText = ((EditText) rootView.findViewById(R.id.gloss));
    if (glossEditText != null) {
      glossEditText.setText(mItem.getGloss());
      glossEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = glossEditText.getText().toString();
          mItem.setGloss(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_GLOSS, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "gloss");
        }
      });
    }

    final EditText translationEditText = ((EditText) rootView.findViewById(R.id.translation));
    if (translationEditText != null) {
      translationEditText.setText(mItem.getTranslation());
      translationEditText.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = translationEditText.getText().toString();
          mItem.setTranslation(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_TRANSLATION, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "translation");
        }
      });
    }

    final EditText contextEditText = ((EditText) rootView.findViewById(R.id.context));
    if (contextEditText != null) {
      contextEditText.setText(mItem.getContext());
      contextEditText.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = contextEditText.getText().toString();
          mItem.setContext(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_CONTEXT, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "context");
        }
      });
    }

    final EditText tagsEditText = ((EditText) rootView.findViewById(R.id.tags));
    if (tagsEditText != null) {
      tagsEditText.setText(mItem.getTagsString());
      tagsEditText.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          String currentText = tagsEditText.getText().toString();
          mItem.setTagsFromSting(currentText);
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_TAGS, currentText);
          getActivity().getContentResolver().update(mUri, values, null, null);
          recordUserEvent("editDatum", "tags");
        }
      });
    }
  }

  protected void prepareVideoAndImageViews(View rootView) {
    if (mImageView == null) {
      mImageView = (ImageView) rootView.findViewById(R.id.image_view);
    }
    if (mMediaController == null) {
      mMediaController = new MediaController(getActivity());
      mMediaController.setAnchorView((VideoView) rootView.findViewById(R.id.video_view));
      // mMediaController.setPadding(0, 0, 0, 200);
    }
    if (mVideoView == null) {
      mVideoView = (VideoView) rootView.findViewById(R.id.video_view);
      if (mVideoView != null) {
        mVideoView.setMediaController(mMediaController);
      }
    }
  }

  protected void prepareSpeechRecognitionButton(View rootView) {

    mSpeechRecognizerFeedback = (ImageButton) rootView.findViewById(R.id.speech_recognizer_feedback);
    if (mSpeechRecognizerFeedback != null) {
      mSpeechRecognizerFeedback.setImageResource(R.drawable.speech_recognizer_listening);
      mSpeechRecognizerFeedback.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          toggleAudioRecording(null);
        }
      });
    }

    mSpeechRecognizerInstructions = (TextView) rootView.findViewById(R.id.speech_recognizer_instructions);
    if (mSpeechRecognizerInstructions != null) {
      mSpeechRecognizerInstructions.setText("");
    }

    this.mDatumPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
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
   * However, as soon as the actual content to be used in the intent is known or
   * changes, you must update the share intent by again calling
   * mShareActionProvider.setShareIntent()
   */
  protected Intent getDefaultIntent() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("image/*");
    return intent;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item == null) {
      return super.onOptionsItemSelected(item);
    } else if (item.getItemId() == R.id.action_speak) {
      return this.toggleAudioRecording(item);
    } else if (item.getItemId() == R.id.action_play) {
      return this.loadMainVideo(true);
    } else if (item.getItemId() == R.id.action_videos) {
      return this.captureVideo();
    } else if (item.getItemId() == R.id.action_images) {
      return this.captureImage();
    } else if (item.getItemId() == R.id.action_delete) {
      return this.delete();
    }
    return super.onOptionsItemSelected(item);
  }

  public void onToggleAudioRecording(View view) {
    this.toggleAudioRecording(null);
  }

  public boolean toggleAudioRecording(MenuItem item) {
    if (!this.mRecordingAudio) {

      if (!checkAndRequestPermissions(getActivity(), Config.CODE_REQUEST_MULTIPLE_PERMISSIONS, false)) {
        return false;
      }

      String audioFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/" + mItem.getBaseFilename()
          + Config.DEFAULT_AUDIO_EXTENSION;
      this.mAudioFileName = audioFileName;
      Intent intent;
      intent = new Intent(getActivity(), AudioRecorder.class);
      intent.putExtra(Config.EXTRA_RESULT_FILENAME, audioFileName);
      mItem.addAudioFile(audioFileName.replace(Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
      getActivity().startService(intent);
      ContentValues values = new ContentValues();
      values.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES, mItem.getMediaFilesAsCSV(mItem.getAudioVideoFiles()));
      getActivity().getContentResolver().update(mUri, values, null, null);
      Log.d(Config.TAG, "Recording audio " + audioFileName);
      this.mRecordingAudio = true;
      if (item != null) {
        item.setIcon(R.drawable.ic_action_stop);
        item.setTitle(R.string.action_stop_record_media);
      }
      this.recordUserEvent("captureAudio", audioFileName);

      if (mSpeechRecognizerFeedback != null) {
        mSpeechRecognizerFeedback.setImageResource(R.drawable.speech_recognizer_recognizing);
      }

      if (mSpeechRecognizerInstructions != null) {
        mSpeechRecognizerInstructions.setText(R.string.tap_to_end);
      }

    } else {
      this.turnOffRecorder(item);
    }
    return true;
  }

  protected boolean turnOffRecorder(MenuItem item) {
    if (mAudioFileName == null) {
      return false;
    }
    Intent audio = new Intent(getActivity(), AudioRecorder.class);
    getActivity().stopService(audio);
    Handler mainHandler = new Handler(getActivity().getMainLooper());
    Runnable launchUploadAudioService = new Runnable() {

      @Override
      public void run() {
        if (getActivity() == null) {
          return;
        }
        Intent uploadAudioFile = new Intent(getActivity(), UploadAudioVideoService.class);
        uploadAudioFile.setData(Uri.parse(mAudioFileName));
        uploadAudioFile.putExtra(Config.EXTRA_PARTICIPANT_ID, Config.CURRENT_USERNAME);
        uploadAudioFile.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION, mDeviceDetails.getCurrentDeviceDetails());
        getActivity().startService(uploadAudioFile);
      }
    };
    mainHandler.postDelayed(launchUploadAudioService, 1000);

    this.mRecordingAudio = false;
    if (item != null) {
      item.setIcon(R.drawable.ic_action_mic);
      item.setTitle(R.string.action_record_audio);
    }
    this.recordUserEvent("stopAudio", this.mAudioFileName);

    if (mSpeechRecognizerFeedback != null) {
      mSpeechRecognizerFeedback.setImageResource(R.drawable.speech_recognizer_waiting);
    }

    if (mSpeechRecognizerInstructions != null) {
      mSpeechRecognizerInstructions.setText(R.string.tap_to_end);
    }
    if (Config.APP_TYPE.equals("speechrecognition")) {
      autoAdvanceAfterRecordingAudio();
    }
    return true;
  }

  protected boolean autoAdvanceAfterRecordingAudio() {
    if (this.mDatumPager != null) {
      int currentStimulusIndex = this.mDatumPager.getCurrentItem();
      if (currentStimulusIndex == this.mLastDatumIndex) {
        Intent openDataList = new Intent(getActivity(), DatumListActivity.class);
        startActivity(openDataList);
      } else {
        this.mDatumPager.setCurrentItem(this.mDatumPager.getCurrentItem() + 1);
      }
    }
    return true;
  }

  protected boolean delete() {
    AlertDialog deleteConfirmationDialog = new AlertDialog.Builder(getActivity())
        .setMessage(getString(R.string.are_you_sure_put_in_trash).replace("datum", Config.USER_FRIENDLY_DATA_NAME))
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            getActivity().getContentResolver().delete(mUri, null, null);
            dialog.dismiss();

            if (mTwoPane) {
              getActivity().getSupportFragmentManager().popBackStack();
            } else {
              NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), DatumListActivity.class));
            }

          }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
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
  public boolean loadMainVideo(final boolean playNow) {
    if (mItem == null) {
      Log.e(Config.TAG, "Couldnt set the audio or video, there was no item.");
      return false;
    }

    String fileName = mItem.getMainAudioVideoFile();
    if (fileName == null) {
      Log.e(Config.TAG, "This item doesnt have an audio video file.");
      return loadMainImage();
    }

    String filePath = Config.DEFAULT_OUTPUT_DIRECTORY + "/" + fileName;
    File audioVideoFile = new File(filePath);
    if (!audioVideoFile.exists()) {
      return loadMainImage();
    }

    if (fileName.endsWith(Config.DEFAULT_AUDIO_EXTENSION) || fileName.endsWith(Config.DEFAULT_RECOGNIZER_AUDIO_EXTENSION)) {
      loadMainImage();
      Log.d(Config.TAG, "Playing audio only (no video)");
      mAudioPlayer = MediaPlayer.create(getActivity(), Uri.parse("file://" + filePath));
      if (mAudioPlayer == null) {
        Log.e(Config.TAG, "Couldn't play audio file" + filePath);
        return false;
      }
      mAudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          if (playNow) {
            mp.start();
          }
        }
      });
      return true;
    }

    mVideoView.setVideoPath(filePath);
    int sdk = android.os.Build.VERSION.SDK_INT;
    if (sdk >= 16) {
      mVideoView.setBackground(null);
    } else {
      Log.e(Config.TAG, "Couldnt set the video background. (this might be a kindle)");
      mImageView.setImageBitmap(null);
      mImageView.setVisibility(View.VISIBLE);
      mVideoView.setVisibility(View.GONE);
    }

    if (playNow) {
      this.recordUserEvent("loadMainVideo", fileName);

      mVideoView.start();
      mMediaController.setPrevNextListeners(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          String nextFile = mItem.getPrevNextMediaFile("audio", mItem.getAudioVideoFiles(), "next");
          if (nextFile != null) {
            mVideoView.stopPlayback();
            mVideoView.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY + "/" + nextFile);
            mVideoView.start();
          }
        }
      }, new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          String previousFile = mItem.getPrevNextMediaFile("audio", mItem.getAudioVideoFiles(), "prev");
          if (previousFile != null) {
            mVideoView.stopPlayback();
            mVideoView.setVideoPath(Config.DEFAULT_OUTPUT_DIRECTORY + "/" + previousFile);
            mVideoView.start();
          }
        }
      });
    }

    return true;
  }

  protected boolean loadMainImage() {
    File image = new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/" + mItem.getMainImageFile());
    if (!image.exists()) {
      mVideoView.setVisibility(View.GONE);
      mImageView.setVisibility(View.VISIBLE);
      return false;
    }
    Bitmap d = new BitmapDrawable(this.getResources(), image.getAbsolutePath()).getBitmap();
    if (d == null) {
      return false;
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
    return true;
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
        resultFile = data.getExtras().getString(Config.EXTRA_RESULT_FILENAME);
        if (resultFile != null) {
          // if (resultFile != null && new
          // File(resultFile).exists())
          // {
          if (resultFile.endsWith(Config.DEFAULT_AUDIO_EXTENSION)) {
            mItem.addAudioFile(resultFile.replace(Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
          } else {
            mItem.addVideoFile(resultFile.replace(Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
          }
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_AUDIO_VIDEO_FILES, mItem.getMediaFilesAsCSV(mItem.getAudioVideoFiles()));
          getActivity().getContentResolver().update(mUri, values, null, null);
          this.loadMainVideo(false);
        }
      }
      break;
    case Config.CODE_PICTURE_TAKEN:
      if (data != null && data.hasExtra(Config.EXTRA_RESULT_FILENAME)) {
        resultFile = data.getExtras().getString(Config.EXTRA_RESULT_FILENAME);
        if (resultFile != null) {
          // if (resultFile != null && new
          // File(resultFile).exists())
          // {
          mItem.addImageFile(resultFile.replace(Config.DEFAULT_OUTPUT_DIRECTORY + "/", ""));
          ContentValues values = new ContentValues();
          values.put(DatumTable.COLUMN_IMAGE_FILES, mItem.getMediaFilesAsCSV(mItem.getImageFiles()));
          getActivity().getContentResolver().update(mUri, values, null, null);
          this.loadMainImage();
        }
      }
      break;
    }
    super.onActivityResult(requestCode, requestCode, data);
  }

  protected boolean captureVideo() {
    if (!checkAndRequestPermissions(getActivity(), Config.CODE_REQUEST_MULTIPLE_PERMISSIONS, true)) {
      return false;
    }

    String videoFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/" + mItem.getBaseFilename()
        + Config.DEFAULT_VIDEO_EXTENSION;
    Intent intent = new Intent(getActivity(), VideoRecorder.class);
    intent.putExtra(Config.EXTRA_USE_FRONT_FACING_CAMERA, true);
    intent.putExtra(Config.EXTRA_LANGUAGE, Config.ENGLISH);
    intent.putExtra(Config.EXTRA_RESULT_FILENAME, videoFileName);
    intent.putExtra(Config.EXTRA_PARTICIPANT_ID, Config.DEFAULT_PARTICIPANT_ID);
    intent.putExtra(Config.EXTRA_OUTPUT_DIR, Config.DEFAULT_OUTPUT_DIRECTORY);
    intent.putExtra(Config.EXTRA_EXPERIMENT_TRIAL_INFORMATION, "");
    startActivityForResult(intent, Config.CODE_EXPERIMENT_COMPLETED);
    this.recordUserEvent("captureVideo", videoFileName);
    return true;
  }

  protected boolean captureImage() {
    if (!checkAndRequestPermissions(getActivity(), Config.CODE_REQUEST_MULTIPLE_PERMISSIONS, true)) {
      return false;
    }

    String imageFileName = Config.DEFAULT_OUTPUT_DIRECTORY + "/" + mItem.getBaseFilename()
        + Config.DEFAULT_IMAGE_EXTENSION;
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
      Iterator<Entry<String, Integer>> it = this.mDatumEditCounts.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
        if (!"".equals(edits)) {
          edits = edits + ",";
        }
        edits = edits + "{" + pair.getKey() + " : " + pair.getValue() + "}";
        it.remove(); // avoids a ConcurrentModificationException
      }
      edits = "[" + edits + "]";
      if (BuildConfig.DEBUG) {
        Log.d(Config.TAG, "edits: " + edits);
      }
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
    com.github.fielddb.model.Activity.sendActivity(eventType, eventValue);
  }


  public static boolean checkAndRequestPermissions(Activity activity, int REQUEST_ID_MULTIPLE_PERMISSIONS, boolean requestCamera) {
    List<String> listPermissionsNeeded = new ArrayList<>();

    if (requestCamera && ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.CAMERA);
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
    }
    if (!listPermissionsNeeded.isEmpty()) {
      ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
      return false;
    }
    return true;
  }

  public Datum getItem() {
    return mItem;
  }

  public void setItem(Datum mItem) {
    this.mItem = mItem;
  }
}
