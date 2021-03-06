package com.github.fielddb.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.fielddb.Config;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UserContentProvider extends ContentProvider {
  protected UserSQLiteHelper database;
  protected static String appVersion;

  // Used for the UriMacher
  protected static final int ITEMS = 10;
  protected static final int ITEM_ID = 20;

  protected static String AUTHORITY = "com.github.fielddb." + UserTable.TABLE_NAME;
  protected static final String BASE_PATH = UserTable.TABLE_NAME + "s";
  public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + UserTable.TABLE_NAME + "s";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + UserTable.TABLE_NAME;

  protected static UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

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
    long insertedRowId = db.insert(UserTable.TABLE_NAME, null, values);
    Log.d(Config.TAG, "insertedRowId " + insertedRowId);
    return id;
  }

  @Override
  public boolean onCreate() {

    try {
      UserContentProvider.appVersion = getContext().getPackageManager()
          .getPackageInfo(getContext().getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      UserContentProvider.appVersion = "0";
    }

    this.database = new UserSQLiteHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    // Using SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    queryBuilder.setStrict(true);

    // Set the table
    queryBuilder.setTables(UserTable.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case ITEMS:
      break;
    case ITEM_ID:
      // Adding the ID to the original query
      queryBuilder.appendWhere(UserTable.COLUMN_USERNAME + "='" + uri.getLastPathSegment() + "'");
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();

    // Enforce the caller has not requested a column which does not exists
    Map<String, String> restrictColumns = UserTable.getProjectionMap();
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
        rowsUpdated = sqlDB.update(UserTable.TABLE_NAME, values, UserTable.COLUMN_ID + "=?", whereArgs);
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

  public static class UserSQLiteHelper extends SQLiteOpenHelper {
    protected static final String DATABASE_NAME = UserTable.TABLE_NAME + ".db";
    protected static final int DATABASE_VERSION = 1;

    public UserSQLiteHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      try {
        UserTable.setColumns();
        db.execSQL(UserTable.generateCreateTableSQLStatement(UserTable.TABLE_NAME));
        db.insert(UserTable.TABLE_NAME, null, UserTable.createAnonymousUser());
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
      String copyTableToBackup = "ALTER TABLE " + UserTable.TABLE_NAME + " RENAME TO " + UserTable.TABLE_NAME
          + "backup1;";
      db.execSQL(copyTableToBackup);

      Log.w(Config.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
          + ", will try to copy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);

      /* Re-create table using current schema Remove the sample data */
      onCreate(db);
      try {
        String clearSampleData = "DELETE FROM " + UserTable.TABLE_NAME + ";";
        db.execSQL(clearSampleData);
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to clear sample data." + e);
      }

      ArrayList<String> previousColumns = FieldDBTable.getBaseColumns();
      String[] knownColumns = UserTable.version1Columns;

      /* Add other versions to this if statement */
      if (oldVersion == 1) {
        knownColumns = UserTable.version1Columns;
      }

      /* Copy the data from previous columns over */
      for (String column : knownColumns) {
        previousColumns.add(column);
      }
      try {
        db.execSQL(UserTable.generateUpgradeTableSQLStatement(UserTable.TABLE_NAME, previousColumns));
      } catch (Exception e) {
        Log.w(Config.TAG, "Problem upgrading, unable to copy user data." + e);
      }
    }
  }

  public static class UserTable extends FieldDBTable {
    public static final String TABLE_NAME = "user";
    public static String ANONYMOUS_PREFIX = "anonymous";

    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_FIRSTNAME = "firstname";
    public static final String COLUMN_LASTNAME = "lastname";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_GRAVATAR = "gravatar";
    public static final String COLUMN_AFFILIATION = "affiliation";
    public static final String COLUMN_RESEARCH_INTEREST = "researchInterest";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_SUBTITLE = "subtitle";
    public static final String COLUMN_GENERATED_PASSWORD = "generatedPassword";

    public static String[] version1Columns = { COLUMN_USERNAME, COLUMN_FIRSTNAME, COLUMN_LASTNAME, COLUMN_EMAIL,
        COLUMN_GRAVATAR, COLUMN_AFFILIATION, COLUMN_RESEARCH_INTEREST, COLUMN_DESCRIPTION, COLUMN_SUBTITLE,
        COLUMN_GENERATED_PASSWORD };

    public static String[] currentColumns = version1Columns;

    // Create a user
    protected static ContentValues createAnonymousUser() {
      String generatedPassword = System.currentTimeMillis() + "";
      ContentValues values = new ContentValues();
      String username = ANONYMOUS_PREFIX + generatedPassword;
      values.put(COLUMN_USERNAME, username);
      values.put(COLUMN_ID, username);
      values.put(COLUMN_FIRSTNAME, "Anony");
      values.put(COLUMN_LASTNAME, "Mouse");
      values.put(COLUMN_LASTNAME, "Mouse");
      values.put(COLUMN_EMAIL, "");
      values.put(COLUMN_GRAVATAR, "");
      values.put(COLUMN_AFFILIATION, "");
      values.put(COLUMN_RESEARCH_INTEREST, "");
      values.put(COLUMN_DESCRIPTION, "");
      values.put(COLUMN_SUBTITLE, "");
      values.put(COLUMN_APP_VERSIONS_WHEN_MODIFIED, UserContentProvider.appVersion);
      values.put(COLUMN_GENERATED_PASSWORD, generatedPassword);
      return values;
    }

    public static void setColumns() {
      UserTable.columns = FieldDBTable.getBaseColumns();
      for (String column : currentColumns) {
        UserTable.columns.add(column);
      }
    }

    public static Map<String, String> getProjectionMap() {
      Map<String, String> projection = new HashMap<>();
      if (null == UserTable.columns || UserTable.columns.isEmpty()) {
        UserTable.setColumns();
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

      projection.put(COLUMN_USERNAME, COLUMN_USERNAME);
      projection.put(COLUMN_FIRSTNAME, COLUMN_FIRSTNAME);
      projection.put(COLUMN_LASTNAME, COLUMN_LASTNAME);
      projection.put(COLUMN_EMAIL, COLUMN_EMAIL);
      projection.put(COLUMN_GRAVATAR, COLUMN_GRAVATAR);
      projection.put(COLUMN_AFFILIATION, COLUMN_AFFILIATION);
      projection.put(COLUMN_RESEARCH_INTEREST, COLUMN_RESEARCH_INTEREST);
      projection.put(COLUMN_DESCRIPTION, COLUMN_DESCRIPTION);
      projection.put(COLUMN_SUBTITLE, COLUMN_SUBTITLE);
      projection.put(COLUMN_GENERATED_PASSWORD, COLUMN_GENERATED_PASSWORD);
      return projection;
    }
  }
}
