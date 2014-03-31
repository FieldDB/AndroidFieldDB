package com.github.opensourcefieldlinguistics.fielddb.lessons;

public class PrivateConstantsSample {

	public static final String ACRA_PASS = "acouchdbpassword";
	public static String ACRA_SERVER_URL = "https://10.0.2.2:6984/acra-learnx/_design/acra-storage/_update/report";

	public static final String ACRA_USER = "acouchdbusername";

	public static final String DEFAULT_PUBLIC_USER_PASS = "none";
	public static final String DEFAULT_PUBLIC_USERNAME = "public";

	public static String DEFAULT_REGISTER_USER_URL = "https://10.0.2.2:3183/register";

	// 10.0.2.2 emulator connection to localhost
	// http://developer.android.com/tools/devices/emulator.html#networkaddresses
	public static String DEFAULT_SERVER_URL = "https://10.0.2.2:6984";

	/*
	 * Example on how to create a keystore from:
	 * http://transoceanic.blogspot.com
	 * /2011/11/android-import-ssl-certificate-and-use.html
	 * 
	 * keytool -importcert -v -trustcacerts -file ../acorpusserver.crt -alias
	 * acorpusserver -keystore "sslkeystore.bks" -provider
	 * org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath
	 * bcprov-jdk15on-150.jar -storetype BKS -storepass
	 * "apasswordforyourkeystore"
	 * 
	 * Workaround for:
	 * http://en.wikipedia.org/wiki/Server_Name_Indication#Support
	 */
	public static String KEYSTORE_PASS = "apasswordforyourkeystore";
}
