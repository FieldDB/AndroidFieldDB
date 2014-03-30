package com.github.opensourcefieldlinguistics.fielddb.service;

import org.acra.ACRA;

import ca.ilanguage.oprime.Config;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DownloadDatumsService extends IntentService {

	public DownloadDatumsService(String name) {
		super(name);
	}

	public DownloadDatumsService() {
		super("DownloadDatumsService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		if (Config.D) {
			Log.d(Config.TAG, "Inside DownloadDatumsService intent");
		}

		int notificationId = (int) System.currentTimeMillis();
		// String uploadStatusMessage = getString(R.string.preparing_upload);
		String tag = "SampleData";
		ACRA.getErrorReporter().putCustomData("downloadDatums", tag);
		ACRA.getErrorReporter().handleException(
				new Exception("*** Download Data Started ***"));

	}

}
