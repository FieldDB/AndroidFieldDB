package ca.ilanguage.oprime.database;

import java.util.ArrayList;

public abstract class OPrimeTable {
	public static final String COLUMN_ACTUAL_JSON = "actualJSON";
	public static final String COLUMN_ANDROID_ID = "android_id";
	public static final String COLUMN_APP_VERSIONS_WHEN_MODIFIED = "appVersionsWhenModified";
	public static final String COLUMN_COMMENTS = "comments";
	public static final String COLUMN_CREATED_AT = "created_at";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_RELATED = "related";
	public static final String COLUMN_REV = "_rev";
	public static final String COLUMN_TRASHED = "trashed";
	public static final String COLUMN_UPDATED_AT = "updated_at";

	public static ArrayList<String> columns;

	public static String generateCreateTableSQLStatement(String TABLE_NAME)
			throws Exception {
		if (columns == null) {
			throw new Exception(
					"Columns have not been defined, please define the columns first.");
		}

		String preamble = "create table " + TABLE_NAME + "("
				+ COLUMN_ANDROID_ID + " INTEGER primary key AUTOINCREMENT, ";
		String postamble = ");";
		final StringBuilder sb = new StringBuilder(preamble);
		boolean isFirst = true;
		for (String column : columns) {
			/* Primary key and actualJSON get special treatment */
			if (COLUMN_ANDROID_ID.equals(column)
					|| COLUMN_ACTUAL_JSON.equals(column)) {
				continue;
			}
			if (!isFirst) {
				sb.append(" text , ");
			} else {
				isFirst = false;
			}
			sb.append(column);
		}
		sb.append(" text , ");
		sb.append(COLUMN_ACTUAL_JSON);
		sb.append(" blob ");
		sb.append(postamble);

		return sb.toString();
	}

	public static String generateUpgradeTableSQLStatement(String TABLE_NAME,
			ArrayList<String> previousColumns) throws Exception {
		if (previousColumns == null) {
			throw new Exception(
					"Previous columns has not been defined, please define the previous columns first.");
		}

		String preamble = "INSERT INTO " + TABLE_NAME + "(";
		String middle = ") SELECT ";
		String postamble = " FROM " + TABLE_NAME + "backup1;";
		final StringBuilder sb = new StringBuilder(preamble);

		/* from previous columns */
		boolean isFirst = true;
		for (String column : previousColumns) {
			if (!isFirst) {
				sb.append(" , ");
			} else {
				isFirst = false;
			}
			sb.append(column);
		}

		sb.append(middle);

		/* to previous columns */
		isFirst = true;
		for (String column : previousColumns) {
			if (!isFirst) {
				sb.append(" , ");
			} else {
				isFirst = false;
			}
			sb.append(column);
		}

		sb.append(postamble);

		return sb.toString();
	}

	public static ArrayList<String> getBaseColumns() {
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(COLUMN_ANDROID_ID);
		columns.add(COLUMN_ID);
		columns.add(COLUMN_REV);
		columns.add(COLUMN_TRASHED);
		columns.add(COLUMN_CREATED_AT);
		columns.add(COLUMN_UPDATED_AT);
		columns.add(COLUMN_APP_VERSIONS_WHEN_MODIFIED);
		columns.add(COLUMN_RELATED);
		columns.add(COLUMN_ACTUAL_JSON);
		return columns;
	}
}
