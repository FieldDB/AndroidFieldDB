package com.github.fielddb.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.github.fielddb.Config;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AudioVideoContentProvider extends ContentProvider {
  private AudioVideoSQLiteHelper database;
  // Used for the UriMacher
  private static final int ITEMS = 10;
  private static final int ITEM_ID = 20;

  private static final String AUTHORITY = "com.github.fielddb." + Config.APP_TYPE.toLowerCase(new Locale("en")) + "."
      + Config.DATA_IS_ABOUT_LANGUAGE_NAME_ASCII.toLowerCase(new Locale("en")) + "." + AudioVideoTable.TABLE_NAME;
  private static final String BASE_PATH = AudioVideoTable.TABLE_NAME + "s";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AudioVideoTable.TABLE_NAME
      + "s";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
      + AudioVideoTable.TABLE_NAME;

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", ITEM_ID);
  }

  @Override
  public int delete(Uri arg0, String arg1, String[] arg2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getType(Uri arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri id, ContentValues values) {
    Log.d(Config.TAG, "insert " + id.toString());
    SQLiteDatabase db = database.getWritableDatabase();
    long insertedRowId = db.insert(AudioVideoTable.TABLE_NAME, null, values);
    Log.d(Config.TAG, "insertedRowId " + insertedRowId);
    return id;
  }

  @Override
  public boolean onCreate() {
    this.database = new AudioVideoSQLiteHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    // Using SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    queryBuilder.setStrict(true);

    // Set the table
    queryBuilder.setTables(AudioVideoTable.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case ITEMS:
      break;
    case ITEM_ID:
      // Adding the ID to the original query
      queryBuilder.appendWhere(AudioVideoTable.COLUMN_FILENAME + "='" + uri.getLastPathSegment() + "'");
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();

    // Enforce the caller has not requested a column which does not exists
    Map<String, String> restrictColumns = AudioVideoTable.getProjectionMap();
    Log.d(Config.TAG, "restrictColumns " + restrictColumns.toString());
    queryBuilder.setProjectionMap(restrictColumns);

    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    // Make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsUpdated = 0;
    switch (uriType) {
    case ITEMS:
      Log.d(Config.TAG, "selecting items is unsupported " + selection);
      break;
    case ITEM_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        String[] whereArgs = {id.toString()};
        rowsUpdated = sqlDB.update(AudioVideoTable.TABLE_NAME, values, AudioVideoTable.COLUMN_ID + "=?", whereArgs);
      } else {
        Log.d(Config.TAG, "ignoring an unsupported selection " + selection);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown Update URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  public static class AudioVideoSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = AudioVideoTable.TABLE_NAME + ".db";
    private static final int DATABASE_VERSION = 1;

    public AudioVideoSQLiteHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      try {
        AudioVideoTable.setColumns();
        db.execSQL(AudioVideoTable.generateCreateTableSQLStatement(AudioVideoTable.TABLE_NAME));
      } catch (SQLException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      /*
       * export user database http://stackoverflow.com/questions/805363/how
       * -do-i-rename-a-column-in-a-sqlite-database-table The SQLite ALTER TABLE
       * documentation can be found here. If you add new columns you can use
       * ALTER TABLE to insert them into a live table. If you rename or remove
       * columns you can use ALTER TABLE to rename the old table, then create
       * the new table and then populate the new table with the contents of the
       * old table.
       */
      String copyTableToBackup = "ALTER TABLE " + AudioVideoTable.TABLE_NAME + " RENAME TO "
          + AudioVideoTable.TABLE_NAME + "backup1;";
      db.execSQL(copyTableToBackup);

      Log.w(Config.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
          + ", will try to copy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + AudioVideoTable.TABLE_NAME);

      /* Re-create table using current schema, and remove the sample data */
      onCreate(db);
      try {
        String clearSampleData = "DELETE FROM " + AudioVideoTable.TABLE_NAME + ";";
        db.execSQL(clearSampleData);
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to clear sample data." + e);
      }
      ArrayList<String> previousColumns = FieldDBTable.getBaseColumns();
      String[] knownColumns = AudioVideoTable.version1Columns;

      /* Add other versions to this if statement */
      if (oldVersion == 1) {
        knownColumns = AudioVideoTable.version1Columns;
      }

      /* Copy the data from previous columns over */
      for (String column : knownColumns) {
        previousColumns.add(column);
      }
      try {
        db.execSQL(AudioVideoTable.generateUpgradeTableSQLStatement(AudioVideoTable.TABLE_NAME, previousColumns));
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to copy user data." + e);
      }
    }

  }

  public static class AudioVideoTable extends FieldDBTable {
    public static final String TABLE_NAME = "audiovideo";

    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_DESCRIPTION = "description";

    public static String[] version1Columns = { COLUMN_FILENAME, COLUMN_URL, COLUMN_DESCRIPTION };

    public static String[] currentColumns = version1Columns;

    public static void setColumns() {
      AudioVideoTable.columns = FieldDBTable.getBaseColumns();
      for (String column : currentColumns) {
        AudioVideoTable.columns.add(column);
      }
    }

    public static Map<String, String> getProjectionMap() {
      Map<String, String> projection = new HashMap<>();
      if (null == AudioVideoTable.columns || AudioVideoTable.columns.isEmpty()) {
        AudioVideoTable.setColumns();
      }
      for (String column : AudioVideoTable.columns) {
        projection.put(column, column);
      }
      return projection;
    }
  }

}
