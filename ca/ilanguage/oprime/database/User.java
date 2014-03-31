package ca.ilanguage.oprime.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class User extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "users";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_DETAILS = "userDetails";

	private static final String DATABASE_NAME = TABLE_NAME + ".db";
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME
			+ "(" + COLUMN_ID + " integer primary key autoincrement" 
			+ ", " + COLUMN_USERNAME + " text not null "
			+ ", " + COLUMN_DETAILS + " text not null " + ");";

	public User(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public User(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_SQL);
		// TODO insert sample users here
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(User.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
