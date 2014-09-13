package com.github.opensourcefieldlinguistics.fielddb.lessons.ui;

import java.util.ArrayList;

import ca.ilanguage.oprime.database.UserContentProvider.UserTable;

import com.github.opensourcefieldlinguistics.fielddb.database.DatumContentProvider;
import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class DatumFragmentPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<String> mDatumsIds;
	private ArrayList<Fragment> mFragments;

	Uri mVisibleDatumUri;

	public DatumFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
	}

	public void swapCursor(Cursor cursor) {
		this.mDatumsIds = new ArrayList<String>();
		this.mDatumsIds.add("instructions");
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				String id = cursor.getString(cursor
						.getColumnIndexOrThrow(UserTable.COLUMN_ID));
				if (!"instructions".equals(id)) {
					this.mDatumsIds.add(id);
				}
			}
			cursor.close();
		}
	}
	@Override
	public Fragment getItem(int position) {
		Log.d(Config.TAG, "Displaying datum in position " + position);
		if (mFragments.size() > position) {
			if (mFragments.get(position) != null) {
				return mFragments.get(position);
			}
		}

		String id = "instructions";
		if (mDatumsIds.size() > position) {
			id = mDatumsIds.get(position);
		}
		Bundle arguments = new Bundle();
		DatumDetailFragment fragment = new DatumProductionExperimentFragment();
		if (Config.APP_TYPE.equals("speechrecognition")) {
//			fragment = new DatumProductionExperimentFragment();
		} else {
			 fragment = new DatumDetailFragment();
		}
		mVisibleDatumUri = Uri.parse(DatumContentProvider.CONTENT_URI + "/"
				+ id);
		Log.d(Config.TAG, mVisibleDatumUri + "");
		arguments.putParcelable(DatumContentProvider.CONTENT_ITEM_TYPE,
				mVisibleDatumUri);
		arguments.putString(DatumDetailFragment.ARG_ITEM_ID, id);
		arguments.putInt(DatumDetailFragment.ARG_TOTAL_DATUM_IN_LIST,
				mDatumsIds.size() - 1);

		fragment.mTwoPane = false;
		fragment.setArguments(arguments);
		if (mFragments.size() == position ||mFragments.size() < position) {
			mFragments.add(fragment);
		} else {
			mFragments.set(position, fragment);
		}
		return fragment;
	}

	@Override
	public int getCount() {
		if (mDatumsIds != null) {
			return mDatumsIds.size();
		}
		return 0;
	}

}
