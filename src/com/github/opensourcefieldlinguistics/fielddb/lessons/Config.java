package com.github.opensourcefieldlinguistics.fielddb.lessons;

public class Config extends ca.ilanguage.oprime.Config {
	public static final boolean D = false;
	public static final String ACRA_PASS = PrivateConstants.ACRA_PASS;
	public static final String ACRA_SERVER_URL = PrivateConstants.ACRA_SERVER_URL;
	public static final String ACRA_USER = PrivateConstants.ACRA_USER;
	public static final String DATA_IS_ABOUT_LANGUAGE_ISO = "ka";
	public static final String DATA_IS_ABOUT_LANGUAGE_NAME_ASCII = "kartuli";
	public static final String APP_TYPE = "speechrecognition";
	// public static final String DATA_IS_ABOUT_LANGUAGE_NAME = "ქართული";

	public static String DEFAULT_CORPUS = "username-"
			+ DATA_IS_ABOUT_LANGUAGE_NAME_ASCII;
	public static String CURRENT_USERNAME = "default";
	public static final String DEFAULT_RECOGNIZER_AUDIO_EXTENSION = ".raw";
	public static final String DEFAULT_DATA_LOGIN = PrivateConstants.DEFAULT_DATA_SERVER_URL
			+ "/_session";
	public static final String DEFAULT_AUTH_LOGIN_URL = PrivateConstants.DEFAULT_AUTH_LOGIN_URL;
	public static final String DEFAULT_DATA_SERVER_URL = PrivateConstants.DEFAULT_DATA_SERVER_URL;
	public static String DEFAULT_OUTPUT_DIRECTORY = "/sdcard/"
			+ DATA_IS_ABOUT_LANGUAGE_NAME_ASCII + "-" + APP_TYPE + "/"
			+ DEFAULT_CORPUS;
	public static final String DEFAULT_PUBLIC_USER_PASS = PrivateConstants.DEFAULT_PUBLIC_USER_PASS;
	public static final String DEFAULT_PUBLIC_USERNAME = PrivateConstants.DEFAULT_PUBLIC_USERNAME;
	public static final String DEFAULT_UPLOAD_TOKEN = PrivateConstants.DEFAULT_UPLOAD_TOKEN;
	public static final String DEFAULT_REGISTER_USER_URL = PrivateConstants.DEFAULT_REGISTER_USER_URL;
	public static final String DEFAULT_UPLOAD_AUDIO_VIDEO_URL = PrivateConstants.DEFAULT_UPLOAD_AUDIO_VIDEO_URL;
	public static final String DEFAULT_SAMPLE_DATA_URL = PrivateConstants.DEFAULT_DATA_SERVER_URL
			+ "/" + DEFAULT_CORPUS + "/_design/learnx/_view/byTag";

	public static final String KEYSTORE_PASS = PrivateConstants.KEYSTORE_PASS;
	public static final String USER_FRIENDLY_DATA_NAME = "ფრაზა";
	public static final String ANONYMOUS_USER_PREFIX = "anonymous"
			+ DATA_IS_ABOUT_LANGUAGE_NAME_ASCII + APP_TYPE;

}
