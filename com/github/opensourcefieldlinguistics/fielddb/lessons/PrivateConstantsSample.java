package com.github.opensourcefieldlinguistics.fielddb.lessons;

/**
 * 
 * http://developer.android.com/tools/devices/emulator.html#networkaddresses
 * 10.0.2.2 emulator connection to localhost
 */
public class PrivateConstantsSample {

	public static final String ACRA_PASS = "acouchdbpassword";
	public static final String ACRA_SERVER_URL = "http://10.0.2.2:5984/acra-learnx/_design/acra-storage/_update/report";
	public static final String ACRA_USER = "acouchdbusername";

	// HTTPS localhost must restarting couchdb with the ssl pointing to this ip
	// public static String DEFAULT_DATA_SERVER_URL = "https://10.0.2.2:6984";
	public static final String DEFAULT_DATA_SERVER_URL = "http://10.0.2.2:5984";
	public static final String DEFAULT_AUTH_LOGIN_URL = "http://10.0.2.2:3183/login";
	public static final String DEFAULT_PUBLIC_USER_PASS = "ausername";
	public static final String DEFAULT_PUBLIC_USERNAME = "apassword";
	public static final String DEFAULT_REGISTER_USER_URL = "http://10.0.2.2:3183/register";
	public static final String DEFAULT_UPLOAD_AUDIO_VIDEO_URL = "http://10.0.2.2:3184/upload/extract/utterances";

	/*
	 * Example on how to support self signed ssl certificates by creating a
	 * keystore from: http://transoceanic.blogspot.com
	 * /2011/11/android-import-ssl-certificate-and-use.html
	 * 
	 * keytool -importcert -v -trustcacerts -file ../acorpusserver.crt -alias
	 * acorpusserver -keystore "sslkeystore.bks" -provider
	 * org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath
	 * bcprov-jdk15on-150.jar -storetype BKS -storepass
	 * "apasswordforyourkeystore"
	 */
	public static final String KEYSTORE_PASS = "apasswordforyourkeystore";
}
