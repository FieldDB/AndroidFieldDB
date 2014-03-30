package com.github.opensourcefieldlinguistics.fielddb.lessons;

public class PrivateConstantsSample {
	public static final String ACRA_USER = "acouchdbusername";
	public static final String ACRA_PASS = "acouchdbpassword";
	public static String DEFAULT_SERVER_URL = "https://acorpusserver.com";
	public static String ACRA_SERVER_URL = "https://anacralyzerserver.com/acra-learnx/_design/acra-storage/_update/report";

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
