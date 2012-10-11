package com.github.opensourcefieldlinguistics.fielddb.android.content;

import java.io.File;
import java.util.Locale;
import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class FieldDBApp extends Application {
	protected static final String TAG = "FieldDB";
	public static final boolean D = true;
	Locale language;
	private String  outputDir= "/sdcard/FieldDB/";

	@Override
	public void onCreate() {
		super.onCreate();
		
//		outputDir = "file:///sdcard"+this.getFilesDir().getAbsolutePath() + File.separator;
		
		language = Locale.getDefault();

		new File(outputDir + "video/").mkdirs();
		new File(outputDir + "audio/").mkdirs();
		new File(outputDir + "images/").mkdirs();
		new File(outputDir + "touchdata/").mkdirs();

		if(D) Log.d(TAG, "Oncreate of the application");
	}

	/**
	 * Forces the locale for the duration of the app to the language needed for
	 * that version of the Experiment. It accepts a variable in the form en or
	 * en-US containing just the language code, or the language code followed by
	 * a - and the co
	 * 
	 * @param lang
	 * @return
	 */
	public String forceLocale(String lang) {
		if (lang.equals(Locale.getDefault().getLanguage())) {
			return Locale.getDefault().getDisplayLanguage();
		}
		Configuration config = getBaseContext().getResources()
				.getConfiguration();
		Locale locale;
		if (lang.contains("-")) {
			String[] langCountrycode = lang.split("-");
			locale = new Locale(langCountrycode[0], langCountrycode[1]);
		} else {
			locale = new Locale(lang);
		}
		Locale.setDefault(locale);
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		language = Locale.getDefault();
		return Locale.getDefault().getDisplayLanguage();
	}

	public Locale getLanguage() {
		return language;
	}

	public void setLanguage(Locale language) {
		this.language = language;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public static boolean isD() {
		return D;
	}

	public static String getTag() {
		return TAG;
	}
	
}
