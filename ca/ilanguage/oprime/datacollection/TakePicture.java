package ca.ilanguage.oprime.datacollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import ca.ilanguage.oprime.content.OPrime;

import android.app.Activity;
import android.content.ContentValues;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.widget.Toast;
import ca.ilanguage.oprime.R;

public class TakePicture extends Activity {
  Uri myPicture;
  String mImageFilename;
  boolean mAppearSeamless = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.take_picture);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    mImageFilename = getIntent().getExtras().getString(
        OPrime.EXTRA_RESULT_FILENAME);
    if (mImageFilename != null && mImageFilename != "") {
      if (mAppearSeamless) {
        SharedPreferences prefs = getSharedPreferences(OPrime.PREFERENCE_NAME,
            MODE_PRIVATE);
        String picture = prefs.getString(OPrime.PREFERENCE_LAST_PICTURE_TAKEN,
            "");
        if (picture == "") {
          this.captureImage(null);
        }
      }
    }
  }

  public void captureImage(View view) {
    ContentValues values = new ContentValues();
    values.put(Media.TITLE, mImageFilename);
    values.put(Media.DESCRIPTION, "Image Captured an Android using OPrime");

    myPicture = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
    /*
     * Store uri for the expected image into the prefs for persistance
     * (workaround for onCreate being called before onActivityResult)
     */
    SharedPreferences prefs = getSharedPreferences(OPrime.PREFERENCE_NAME,
        MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor
        .putString(OPrime.PREFERENCE_LAST_PICTURE_TAKEN, myPicture.toString());
    editor.commit();

    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    i.putExtra(MediaStore.EXTRA_OUTPUT, myPicture);

    startActivityForResult(i, 0);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

      /*
       * get expected image uri in gallery folder
       */
      SharedPreferences prefs = getSharedPreferences(OPrime.PREFERENCE_NAME,
          MODE_PRIVATE);
      String picture = prefs
          .getString(OPrime.PREFERENCE_LAST_PICTURE_TAKEN, "");
      if (picture == "") {
        return;
      }
      myPicture = Uri.parse(picture);

      /*
       * Copy it to the results folder
       */
      try {
        File sd = Environment.getExternalStorageDirectory();
        if (sd.canWrite()) {
          // Get the source and destination
          String sourceImagePath = getPath(myPicture);
          File source = new File(sourceImagePath);
          String destinationImagePath = mImageFilename;

          // Calculate the scale based on the given max picture size, if there
          // is one
          int maxPictureSize = getIntent().getExtras().getInt(OPrime.EXTRA_MAX_PICTURE_SIZE);
          if (maxPictureSize > 0) {
            // Code from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#answer-3549021
            Bitmap b = null;
            
            // Decode the source picture size
            BitmapFactory.Options memoryEfficientOptions = new BitmapFactory.Options();
            memoryEfficientOptions.inJustDecodeBounds = true;
            FileInputStream fis = new FileInputStream(source);
            BitmapFactory.decodeStream(fis, null, memoryEfficientOptions);
            fis.close();

            // Determine the desired scale
            int scale = 1;
            if (memoryEfficientOptions.outHeight > maxPictureSize
                || memoryEfficientOptions.outWidth > maxPictureSize) {
              scale = (int) Math.pow(
                  2,
                  (int) Math.round(Math.log(maxPictureSize
                      / (double) Math.max(memoryEfficientOptions.outHeight,
                          memoryEfficientOptions.outWidth))
                      / Math.log(0.5)));
            }
            
            // Decode the picture with the determined scale
            BitmapFactory.Options scalingOptions = new BitmapFactory.Options();
            scalingOptions.inSampleSize = scale;
            fis = new FileInputStream(source);
            b = BitmapFactory.decodeStream(fis, null, scalingOptions);
            fis.close();
            
            if (b != null) {
              // Send image to the destination
              FileOutputStream destStream = new FileOutputStream(new File(
                  destinationImagePath));
              b.compress(Bitmap.CompressFormat.PNG, 100, destStream);
              
              destStream.flush();
              destStream.close();
            }
          } else {
            // There was no max picture size, so save to the destination as is
            if (source.exists()) {
              FileOutputStream destStream = new FileOutputStream(new File(
                  destinationImagePath));
              FileInputStream inStream = new FileInputStream(source);

              FileChannel src = inStream.getChannel();
              FileChannel dst = destStream.getChannel();

              dst.transferFrom(src, 0, src.size());

              src.close();
              dst.close();
              destStream.close();
              inStream.close();
            }
          }
        }

        // blank out the last picture taken, as it is used to control whether
        // onCreate launches directly into a picture.
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(OPrime.PREFERENCE_LAST_PICTURE_TAKEN, "");
        editor.commit();

        Toast.makeText(getApplicationContext(), "Saving as " + mImageFilename,
            Toast.LENGTH_LONG).show();
        if (mAppearSeamless) {
          Intent intent = new Intent();
          intent.putExtra(OPrime.EXTRA_RESULT_FILENAME, mImageFilename);
          setResult(Activity.RESULT_OK, intent);
          finish();
        }
      } catch (Exception e) {
        Toast.makeText(
            getApplicationContext(),
            "Result picture wasn't copied, its in the Camera folder: "
                + getPath(myPicture), Toast.LENGTH_LONG).show();
      }

    }
  }

  public String getPath(Uri uri) {

    String selection = null;
    String[] selectionArgs = null;
    String sortOrder = null;

    String[] projection = { MediaStore.Images.Media.DATA };
    CursorLoader cursorLoader = new CursorLoader(this, uri, projection,
        selection, selectionArgs, sortOrder);

    Cursor cursor = cursorLoader.loadInBackground();

    int column_index = cursor
        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }

  @Override
  protected void onDestroy() {

    super.onDestroy();
  }
}
