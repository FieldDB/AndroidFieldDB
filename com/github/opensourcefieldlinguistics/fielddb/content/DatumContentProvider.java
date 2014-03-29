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

public class DatumContentProvider extends ContentProvider {
	private DatumSQLiteHelper database;
	// Used for the UriMacher
	private static final int ITEMS = 10;
	private static final int ITEM_ID = 20;

	private static final String AUTHORITY = "com.github.opensourcefieldlinguistics.fielddb.datum";
	private static final String BASE_PATH = DatumTable.TABLE_NAME + "s";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + DatumTable.TABLE_NAME + "s";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + DatumTable.TABLE_NAME;

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
		this.database = new DatumSQLiteHelper(getContext());
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
		queryBuilder.setTables(DatumTable.TABLE_NAME);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ITEMS:
			break;
		case ITEM_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(DatumTable.COLUMN_ID + "="
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

	public class DatumSQLiteHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "datum.db";
		private static final int DATABASE_VERSION = 1;

		public DatumSQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DatumTable.CREATE);
			db.insert(DatumTable.TABLE_NAME, null, DatumTable.sampleData());
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
			String copyTableToBackup = "ALTER TABLE " + DatumTable.TABLE_NAME
					+ " RENAME TO " + DatumTable.TABLE_NAME + "backup1;";
			db.execSQL(copyTableToBackup);

			Log.w(Config.TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", will try to copy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DatumTable.TABLE_NAME);

			/* Re-create table using current schema, and remove the sample data */
			onCreate(db);
			String clearSampleData = "DELETE FROM " + DatumTable.TABLE_NAME
					+ ";";
			try {
				db.execSQL(clearSampleData);
			} catch (Exception e) {
				Log.w(Config.TAG,
						"Problem upgrading, unable to clear sample data." + e);
			}
			String performUpgrade = "";
			performUpgrade = "INSERT INTO " + DatumTable.TABLE_NAME + "("
					+ DatumTable.COLUMN_ID + ", " + DatumTable.COLUMN_REV
					+ ", " + DatumTable.COLUMN_UTTERANCE + ", "
					+ DatumTable.COLUMN_MORPHEMES + ", "
					+ DatumTable.COLUMN_GLOSS + ", "
					+ DatumTable.COLUMN_TRANSLATION + ", "
					+ DatumTable.COLUMN_ORTHOGRAPHY + ", "
					+ DatumTable.COLUMN_CONTEXT + ", "
					+ DatumTable.COLUMN_IMAGE_FILES + ", "
					+ DatumTable.COLUMN_AUDIO_VIDEO_FILES + ", "
					+ DatumTable.COLUMN_LOCATIONS + ", "
					+ DatumTable.COLUMN_RELATED + ", "
					+ DatumTable.COLUMN_REMINDERS + ", "
					+ DatumTable.COLUMN_TAGS + ", "
					+ DatumTable.COLUMN_COMMENTS + ", "
					+ DatumTable.COLUMN_ACTUAL_JSON + ") " + "SELECT "

					+ DatumTable.COLUMN_ID + ", " + DatumTable.COLUMN_REV
					+ ", " + DatumTable.COLUMN_UTTERANCE + ", "
					+ DatumTable.COLUMN_MORPHEMES + ", "
					+ DatumTable.COLUMN_GLOSS + ", "
					+ DatumTable.COLUMN_TRANSLATION + ", "
					+ DatumTable.COLUMN_ORTHOGRAPHY + ", "
					+ DatumTable.COLUMN_CONTEXT + ", "
					+ DatumTable.COLUMN_IMAGE_FILES + ", "
					+ DatumTable.COLUMN_AUDIO_VIDEO_FILES + ", "
					+ DatumTable.COLUMN_LOCATIONS + ", "
					+ DatumTable.COLUMN_RELATED + ", "
					+ DatumTable.COLUMN_REMINDERS + ", "
					+ DatumTable.COLUMN_TAGS + ", "
					+ DatumTable.COLUMN_COMMENTS + ", "
					+ DatumTable.COLUMN_ACTUAL_JSON + " " + "FROM "
					+ DatumTable.TABLE_NAME + "backup1;";
			try {
				db.execSQL(performUpgrade);
			} catch (Exception e) {
				Log.w(Config.TAG,
						"Problem upgrading, unable to copy user data." + e);
			}
		}

	}

	public static class DatumTable {
		public static final String TABLE_NAME = "datum";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_REV = "_rev";
		public static final String COLUMN_UTTERANCE = "utterance";
		public static final String COLUMN_MORPHEMES = "morphemes";
		public static final String COLUMN_GLOSS = "gloss";
		public static final String COLUMN_TRANSLATION = "translation";
		public static final String COLUMN_ORTHOGRAPHY = "orthography";
		public static final String COLUMN_CONTEXT = "context";
		public static final String COLUMN_IMAGE_FILES = "imageFiles";
		public static final String COLUMN_AUDIO_VIDEO_FILES = "audioVideoFiles";
		public static final String COLUMN_LOCATIONS = "locations";
		public static final String COLUMN_RELATED = "related";
		public static final String COLUMN_REMINDERS = "reminders";
		public static final String COLUMN_TAGS = "tags";
		public static final String COLUMN_COMMENTS = "comments";
		public static final String COLUMN_ACTUAL_JSON = "actualJSON";

		// Database creation SQL statement
		private static final String CREATE = "create table " + TABLE_NAME + "("
				+ COLUMN_ID + " text primary key, " + COLUMN_REV + " text , "
				+ COLUMN_UTTERANCE + " text , " + COLUMN_MORPHEMES + " text , "
				+ COLUMN_GLOSS + " text , " + COLUMN_TRANSLATION + " text , "
				+ COLUMN_ORTHOGRAPHY + " text , " + COLUMN_CONTEXT + " text , "
				+ COLUMN_IMAGE_FILES + " text , " + COLUMN_AUDIO_VIDEO_FILES
				+ " text , " + COLUMN_LOCATIONS + " text , " + COLUMN_RELATED
				+ " text , " + COLUMN_REMINDERS + " text , " + COLUMN_TAGS
				+ " text , " + COLUMN_COMMENTS + " text , "
				+ COLUMN_ACTUAL_JSON + " blob " + ");";

		// Sample data
		private static ContentValues sampleData() {
			ContentValues values = new ContentValues();
			values.put(COLUMN_ID, "sample1234");
			values.put(COLUMN_MORPHEMES, "gamardʒoba");
			values.put(COLUMN_GLOSS, "hello");
			values.put(COLUMN_TRANSLATION, "Hello");
			values.put(COLUMN_ORTHOGRAPHY, "გამარჯობა");
			values.put(COLUMN_CONTEXT, "(Standard greeting)");
			values.put(COLUMN_IMAGE_FILES, "gamardZoba.jpg");
			return values;

		}
	}

}
