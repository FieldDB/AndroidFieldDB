package com.github.opensourcefieldlinguistics.fielddb.database;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import android.content.UriMatcher;
import android.net.Uri;
import ca.ilanguage.oprime.database.UserContentProvider;

public class FieldDBUserContentProvider extends UserContentProvider {

	@Override
	public boolean onCreate() {
		UserTable.ANONYMOUS_PREFIX = Config.ANONYMOUS_USER_PREFIX;
		if (Config.D) {
			UserTable.ANONYMOUS_PREFIX = "testing" + UserTable.ANONYMOUS_PREFIX;
		}
		AUTHORITY = "com.github.opensourcefieldlinguistics.fielddb."
				+ Config.APP_TYPE.toLowerCase() + "."
				+ Config.DATA_IS_ABOUT_LANGUAGE_NAME_ASCII.toLowerCase() + "."
				+ UserTable.TABLE_NAME;
		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
		sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEMS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", ITEM_ID);

		return super.onCreate();
	}

}
