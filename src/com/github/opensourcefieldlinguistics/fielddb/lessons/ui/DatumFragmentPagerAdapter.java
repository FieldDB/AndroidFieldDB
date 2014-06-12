package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.util.ArrayList;
import java.util.List;

import ca.ilanguage.oprime.database.UserContentProvider.UserTable;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider.DatumTable;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DatumFragmentPagerAdapter extends FragmentPagerAdapter {
	private String[] datumsIds;
	private ArrayList<Fragment> mFragments;

	Uri mVisibleDatumUri;
	Cursor mCursor;

	public DatumFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
	}

	public void swapCursor(Cursor cursor) {
		this.mCursor = cursor;
	}

	@Override
	public Fragment getItem(int position) {
		if (mFragments.size() > position) {
			if (mFragments.get(position) != null) {
				return mFragments.get(position);
			}
		}

		String id = "sms1";
		if (mCursor.getCount() > position) {
			mCursor.moveToPosition(position);
			id = mCursor.getString(mCursor
					.getColumnIndexOrThrow(UserTable.COLUMN_ID));
		}
		Bundle arguments = new Bundle();
		DatumProductionExperimentFragment fragment = new DatumProductionExperimentFragment();
		if (Config.APP_TYPE.equals("speechrec")) {
			fragment = new DatumProductionExperimentFragment();
		} else {
			// fragment = new DatumDetailFragment();
		}
		mVisibleDatumUri = Uri.parse(DatumContentProvider.CONTENT_URI + "/"
				+ id);
		arguments.putParcelable(DatumContentProvider.CONTENT_ITEM_TYPE,
				mVisibleDatumUri);
		arguments.putString(DatumDetailFragment.ARG_ITEM_ID, id);

		fragment.mTwoPane = false;
		fragment.setArguments(arguments);
		if (mFragments.size() == position) {
			mFragments.add(fragment);
		} else {
			mFragments.set(position, fragment);
		}
		return fragment;
	}

	@Override
	public int getCount() {
		if (mCursor != null) {
			return mCursor.getCount();
		}
		return 0;
	}
}
