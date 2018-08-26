package com.github.fielddb.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

public class DatumContentProvider extends ContentProvider {
  private DatumSQLiteHelper database;
  // Used for the UriMacher
  private static final int ITEMS = 10;
  private static final int ITEM_ID = 20;

  private static String mAppType = Config.APP_TYPE;
  private static String mDataIsAboutLanguageName = Config.DATA_IS_ABOUT_LANGUAGE_NAME_ASCII;

  private static String AUTHORITY = "com.github.fielddb." + mAppType.toLowerCase(new Locale("en")) + "."
      + mDataIsAboutLanguageName.toLowerCase(new Locale("en")) + "." + DatumTable.TABLE_NAME;
  private static final String BASE_PATH = DatumTable.TABLE_NAME + "s";
  public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatumTable.TABLE_NAME + "s";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatumTable.TABLE_NAME;

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", ITEM_ID);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    ContentValues values = new ContentValues();
    values.put(DatumTable.COLUMN_TRASHED, "deleted");
    int rowsUpdated = this.update(uri, values, selection, selectionArgs);
    return rowsUpdated;
  }

  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    String id;
    if (values == null) {
      values = new ContentValues();
      id = UUID.randomUUID().toString();
      values.put(DatumTable.COLUMN_ID, id);
    } else {
      id = values.getAsString(DatumTable.COLUMN_ID);
      if (id == null) {
        id = UUID.randomUUID().toString();
        values.put(DatumTable.COLUMN_ID, id);
      }
    }
    Log.d(Config.TAG, "insert " + id.toString());
    SQLiteDatabase db = database.getWritableDatabase();
    long insertedRowId = db.insert(DatumTable.TABLE_NAME, null, values);
    Log.d(Config.TAG, "insertedRowId " + insertedRowId);
    if (insertedRowId > 0) {
      uri = Uri.withAppendedPath(uri, id);
    } else {
      return null;
    }
    return uri;
  }

  @Override
  public boolean onCreate() {
    this.database = new DatumSQLiteHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    // Using SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    queryBuilder.setStrict(true);

    // Set the table
    queryBuilder.setTables(DatumTable.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case ITEMS:
      // queryBuilder.appendWhere(DatumTable.COLUMN_TRASHED +
      // " LIKE 'deleted'");
      queryBuilder.appendWhere(DatumTable.COLUMN_TRASHED + " IS NULL");
      break;
    case ITEM_ID:
      // Adding the ID to the original query
      queryBuilder.appendWhere(DatumTable.COLUMN_ID + "='" + uri.getLastPathSegment() + "'");
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();

    // Enforce the caller has not requested a column which does not exists
    Map<String, String> restrictColumns = DatumTable.getProjectionMap();
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
        rowsUpdated = sqlDB.update(DatumTable.TABLE_NAME, values, DatumTable.COLUMN_ID + "=?", whereArgs);
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

  public static class DatumSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = DatumTable.TABLE_NAME + ".db";
    private static final int DATABASE_VERSION = 1;

    public DatumSQLiteHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      try {
        DatumTable.setColumns();
        db.execSQL(DatumTable.generateCreateTableSQLStatement(DatumTable.TABLE_NAME));

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
      String copyTableToBackup = "ALTER TABLE " + DatumTable.TABLE_NAME + " RENAME TO " + DatumTable.TABLE_NAME
          + "backup1;";
      db.execSQL(copyTableToBackup);

      Log.w(Config.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
          + ", will try to copy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + DatumTable.TABLE_NAME);

      /* Re-create table using current schema, and remove the sample data */
      onCreate(db);
      try {
        String clearSampleData = "DELETE FROM " + DatumTable.TABLE_NAME + ";";
        db.execSQL(clearSampleData);
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to clear sample data." + e);
      }

      ArrayList<String> previousColumns = FieldDBTable.getBaseColumns();
      String[] knownColumns = DatumTable.version1Columns;

      /* Add other versions to this if statement */
      if (oldVersion == 1) {
        knownColumns = DatumTable.version1Columns;
      }

      /* Copy the data from previous columns over */
      for (String column : knownColumns) {
        previousColumns.add(column);
      }
      try {
        db.execSQL(DatumTable.generateUpgradeTableSQLStatement(DatumTable.TABLE_NAME, previousColumns));
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to copy datum data." + e);
      }
    }
  }

  public static class DatumTable extends FieldDBTable {
    public static final String TABLE_NAME = "datum";

    public static final String COLUMN_UTTERANCE = "utterance";
    public static final String COLUMN_MORPHEMES = "morphemes";
    public static final String COLUMN_GLOSS = "gloss";
    public static final String COLUMN_TRANSLATION = "translation";
    public static final String COLUMN_ORTHOGRAPHY = "orthography";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_IMAGE_FILES = "imageFiles";
    public static final String COLUMN_AUDIO_VIDEO_FILES = "audioVideoFiles";
    public static final String COLUMN_LOCATIONS = "locations";
    public static final String COLUMN_REMINDERS = "reminders";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_COMMENTS = "comments";
    public static final String COLUMN_VALIDATION_STATUS = "validationStatus";
    public static final String COLUMN_ENTERED_BY_USER = "enteredByUser";
    public static final String COLUMN_MODIFIED_BY_USER = "modifiedByUser";

    public static String[] version1Columns = { COLUMN_UTTERANCE, COLUMN_MORPHEMES, COLUMN_GLOSS, COLUMN_TRANSLATION,
        COLUMN_ORTHOGRAPHY, COLUMN_CONTEXT, COLUMN_IMAGE_FILES, COLUMN_AUDIO_VIDEO_FILES, COLUMN_LOCATIONS,
        COLUMN_REMINDERS, COLUMN_TAGS, COLUMN_COMMENTS, COLUMN_VALIDATION_STATUS, COLUMN_ENTERED_BY_USER,
        COLUMN_MODIFIED_BY_USER };

    public static String[] currentColumns = version1Columns;

    public static void setColumns() {
      DatumTable.columns = FieldDBTable.getBaseColumns();
      for (String column : currentColumns) {
        DatumTable.columns.add(column);
      }
    }

    public static Map<String, String> getProjectionMap() {
      Map<String, String> projection = new HashMap<>();
      if (null == DatumTable.columns || DatumTable.columns.isEmpty()) {
        DatumTable.setColumns();
      }
      projection.put(COLUMN_ANDROID_ID, COLUMN_ANDROID_ID);
      projection.put(COLUMN_ID, COLUMN_ID);
      projection.put(COLUMN_REV, COLUMN_REV);
      projection.put(COLUMN_TRASHED, COLUMN_TRASHED);
      projection.put(COLUMN_CREATED_AT, COLUMN_CREATED_AT);
      projection.put(COLUMN_UPDATED_AT, COLUMN_UPDATED_AT);
      projection.put(COLUMN_APP_VERSIONS_WHEN_MODIFIED, COLUMN_APP_VERSIONS_WHEN_MODIFIED);
      projection.put(COLUMN_RELATED, COLUMN_RELATED);
      projection.put(COLUMN_ACTUAL_JSON, COLUMN_ACTUAL_JSON);

      projection.put(COLUMN_UTTERANCE, COLUMN_UTTERANCE);
      projection.put(COLUMN_MORPHEMES, COLUMN_MORPHEMES);
      projection.put(COLUMN_GLOSS, COLUMN_GLOSS);
      projection.put(COLUMN_TRANSLATION, COLUMN_TRANSLATION);
      projection.put(COLUMN_ORTHOGRAPHY, COLUMN_ORTHOGRAPHY);
      projection.put(COLUMN_CONTEXT, COLUMN_CONTEXT);
      projection.put(COLUMN_IMAGE_FILES, COLUMN_IMAGE_FILES);
      projection.put(COLUMN_AUDIO_VIDEO_FILES, COLUMN_AUDIO_VIDEO_FILES);
      projection.put(COLUMN_LOCATIONS, COLUMN_LOCATIONS);
      projection.put(COLUMN_REMINDERS, COLUMN_REMINDERS);
      projection.put(COLUMN_TAGS, COLUMN_TAGS);
      projection.put(COLUMN_COMMENTS, COLUMN_COMMENTS);
      projection.put(COLUMN_VALIDATION_STATUS, COLUMN_VALIDATION_STATUS);
      projection.put(COLUMN_ENTERED_BY_USER, COLUMN_ENTERED_BY_USER);
      projection.put(COLUMN_MODIFIED_BY_USER, COLUMN_MODIFIED_BY_USER);
      return projection;
    }
  }

}
