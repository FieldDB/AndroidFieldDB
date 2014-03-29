package com.github.opensourcefieldlinguistics.fielddb.content;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AudioVideoContentProvider extends ContentProvider {
	private AudioVideoSQLiteHelper database;
	// Used for the UriMacher
	private static final int ITEMS = 10;
	private static final int ITEM_ID = 20;

	private static final String AUTHORITY = "com.github.opensourcefieldlinguistics.fielddb.audiovideo";
	private static final String BASE_PATH = AudioVideoTable.TABLE_NAME + "s";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + AudioVideoTable.TABLE_NAME + "s";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + AudioVideoTable.TABLE_NAME;

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ITEM_ID);
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
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		this.database = new AudioVideoSQLiteHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		// checkColumns(projection);

		// Set the table
		queryBuilder.setTables(AudioVideoTable.TABLE_NAME);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ITEMS:
			break;
		case ITEM_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(AudioVideoTable.COLUMN_FILENAME + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class AudioVideoSQLiteHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "AudioVideo.db";
		private static final int DATABASE_VERSION = 1;

		public AudioVideoSQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(AudioVideoTable.CREATE);
			db.insert(AudioVideoTable.TABLE_NAME, null, AudioVideoTable.sampleData());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			 * export user database
			 * http://stackoverflow.com/questions/805363/how
			 * -do-i-rename-a-column-in-a-sqlite-database-table The SQLite ALTER
			 * TABLE documentation can be found here. If you add new columns you
			 * can use ALTER TABLE to insert them into a live table. If you
			 * rename or remove columns you can use ALTER TABLE to rename the
			 * old table, then create the new table and then populate the new
			 * table with the contents of the old table.
			 */
			String copyTableToBackup = "ALTER TABLE " + AudioVideoTable.TABLE_NAME
					+ " RENAME TO " + AudioVideoTable.TABLE_NAME + "backup1;";
			db.execSQL(copyTableToBackup);

			Log.w(Config.TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", will try to copy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + AudioVideoTable.TABLE_NAME);

			/* Re-create table using current schema, and remove the sample data */
			onCreate(db);
			String clearSampleData = "DELETE FROM " + AudioVideoTable.TABLE_NAME
					+ ";";
			try {
				db.execSQL(clearSampleData);
			} catch (Exception e) {
				Log.w(Config.TAG,
						"Problem upgrading, unable to clear sample data." + e);
			}
			String performUpgrade = "";
			performUpgrade = "INSERT INTO " + AudioVideoTable.TABLE_NAME + "("
					+ AudioVideoTable.COLUMN_FILENAME + ", " + AudioVideoTable.COLUMN_URL
					+ ", " + AudioVideoTable.COLUMN_DESCRIPTION + ", "
					+ AudioVideoTable.COLUMN_COMMENTS + ", "
					+ AudioVideoTable.COLUMN_ACTUAL_JSON + ") " + "SELECT "

					+ AudioVideoTable.COLUMN_FILENAME + ", " + AudioVideoTable.COLUMN_URL
					+ ", " + AudioVideoTable.COLUMN_DESCRIPTION + ", "
					+ AudioVideoTable.COLUMN_COMMENTS + ", "
					+ AudioVideoTable.COLUMN_COMMENTS + ", "
					+ AudioVideoTable.COLUMN_ACTUAL_JSON + " " + "FROM "
					+ AudioVideoTable.TABLE_NAME + "backup1;";
			try {
				db.execSQL(performUpgrade);
			} catch (Exception e) {
				Log.w(Config.TAG,
						"Problem upgrading, unable to copy user data." + e);
			}
		}

	}

	public static class AudioVideoTable {
		public static final String TABLE_NAME = "AudioVideo";

		public static final String COLUMN_FILENAME = "filename";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_DESCRIPTION = "description";
		public static final String COLUMN_COMMENTS = "comments";
		public static final String COLUMN_ACTUAL_JSON = "actualJSON";

		// Database creation SQL statement
		private static final String CREATE = "create table " + TABLE_NAME + "("
				+ COLUMN_FILENAME + " text primary key, " + COLUMN_URL + " text , "
				+ COLUMN_DESCRIPTION + " text , " + COLUMN_COMMENTS + " text , "
				+ COLUMN_ACTUAL_JSON + " blob " + ");";

		// Sample data
		private static ContentValues sampleData() {
			ContentValues values = new ContentValues();
			values.put(COLUMN_FILENAME, "ar_makfs_cleaned.mp3");
			values.put(COLUMN_URL, "https://corpus.lingsync.org/community-georgian/723a8b707e579087aa36c2e338ecdb4c/ar_makfs_cleaned.mp3");
			return values;

		}
	}

}