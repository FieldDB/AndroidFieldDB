package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;


import com.github.opensourcefieldlinguistics.datacollection.AudioRecorder;
import com.github.opensourcefieldlinguistics.fielddb.Config;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.speech.kartuli.R;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class ProductionExperimentActivity extends FragmentActivity
		implements
			LoaderManager.LoaderCallbacks<Cursor> {

	private DatumFragmentPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_production_experiment_datum_list);

		this.initialisePaging();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {DatumTable.COLUMN_ID};
		String filterStr = "AutomaticallyRecognized";
		String selection = DatumTable.COLUMN_VALIDATION_STATUS + " IS NULL OR "
				+ DatumTable.COLUMN_VALIDATION_STATUS + " NOT LIKE ? ";
		String[] selectionArgs = new String[]{"%" + filterStr + "%"};

		CursorLoader cursorLoader = new CursorLoader(this,
				DatumContentProvider.CONTENT_URI, projection, selection,
				selectionArgs, null);
		Cursor cursor = cursorLoader.loadInBackground();
		this.mPagerAdapter.swapCursor(cursor);

		return cursorLoader;
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		Log.d(Config.TAG, "Finished loading the ids for swipe paging");
		this.mPagerAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		this.mPagerAdapter.swapCursor(null);
	}

	/**
	 * Initialise the fragments to be paged
	 */
	private void initialisePaging() {

		this.mPagerAdapter = new DatumFragmentPagerAdapter(
				super.getSupportFragmentManager());
		this.onCreateLoader(0, null);

		ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
		pager.setAdapter(this.mPagerAdapter);
	}

	@Override
	public void onBackPressed() {
		Intent audio = new Intent(this, AudioRecorder.class);
		this.stopService(audio);

		super.onBackPressed();
	}

}
