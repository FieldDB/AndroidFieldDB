package com.github.opensourcefieldlinguistics.fielddb.database;

import android.net.Uri;
import ca.ilanguage.oprime.database.UserContentProvider;

public class FieldDBUserContentProvider extends UserContentProvider {
	private static final String AUTHORITY = "com.github.opensourcefieldlinguistics.fielddb."
			+ UserTable.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ITEM_ID);
	}
}
