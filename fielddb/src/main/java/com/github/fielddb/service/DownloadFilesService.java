package com.github.fielddb.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.fielddb.Config;
import com.github.fielddb.database.AudioVideoContentProvider;
import com.github.fielddb.database.AudioVideoContentProvider.AudioVideoTable;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.util.Log;

public class DownloadFilesService extends IntentService {

  public DownloadFilesService(String name) {
    super(name);
  }

  public DownloadFilesService() {
    super("DownloadDatumsService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    String fileName = intent.getStringExtra(Config.EXTRA_RESULT_FILENAME);
    if (fileName == null) {
      Log.d(Config.TAG, "You asked to download an empty filename");
      return;
    }
    String[] filesProjection = { AudioVideoTable.COLUMN_ID, AudioVideoTable.COLUMN_FILENAME };
    Cursor cursor = getContentResolver().query(AudioVideoContentProvider.CONTENT_URI, filesProjection,
        AudioVideoTable.COLUMN_FILENAME + "=\"" + fileName + "\"", null, null);
    if (cursor != null) {
      if (cursor.getCount() <= 0) {
        ContentValues sampleImage = new ContentValues();
        sampleImage.put(AudioVideoTable.COLUMN_FILENAME, fileName);
        sampleImage
            .put(AudioVideoTable.COLUMN_URL, "https://speech.lingsync.org/speechrecognition-kartuli/" + fileName);
        getContentResolver().insert(AudioVideoContentProvider.CONTENT_URI, sampleImage);

        AssetManager assetManager = getApplicationContext().getAssets();
        try {
          InputStream is = assetManager.open("images/" + fileName);
          File out = new File(Config.DEFAULT_OUTPUT_DIRECTORY + "/" + fileName);
          byte[] buffer = new byte[1024];
          FileOutputStream fos = new FileOutputStream(out);
          int read = 0;

          while ((read = is.read(buffer, 0, 1024)) >= 0) {
            fos.write(buffer, 0, read);
          }

          fos.flush();
          fos.close();
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
          Log.d(Config.TAG, "TODO look for the file " + fileName + " online");
        }
      }
      cursor.close();
    }

  }
}
