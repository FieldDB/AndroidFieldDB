package com.github.fielddb;

/**
 *
 * http://developer.android.com/tools/devices/emulator.html#networkaddresses
 * 10.0.2.2 emulator connection to localhost
 */
public class PrivateConstantsSample {
  public static String APP_TYPE = "speechrecognition"; // "learnx"
  public static String APP_BRAND = "kartulispeechrecognition"; // "lingsync"

  public static String ACRA_PASS = "acouchdbpassword";
  public static String ACRA_SERVER_URL = "http://10.0.2.2:5984/acra-learnx/_design/acra-storage/_update/report";
  public static String ACRA_USER = "acouchdbusername";

  // HTTPS localhost must restarting couchdb with the ssl pointing to this ip
  // public static String DEFAULT_DATA_SERVER_URL = "https://10.0.2.2:6984";
  public static String DEFAULT_DATA_SERVER_URL = "http://10.0.2.2:5984";
  public static String DEFAULT_AUTH_LOGIN_URL = "https://10.0.2.2:3183/login";
  public static String DEFAULT_PUBLIC_USER_PASS = "ausername";
  public static String DEFAULT_PUBLIC_USERNAME = "apassword";
  public static String DEFAULT_REGISTER_USER_URL = "https://10.0.2.2:3183/register";
  public static String DEFAULT_UPLOAD_AUDIO_VIDEO_URL = "https://10.0.2.2:3184/upload/extract/utterances";
  public static String DEFAULT_UPLOAD_TOKEN = "yourapitoken";

  public static String DATA_IS_ABOUT_LANGUAGE_ISO = "ka";
  public static String DATUM_AUTHORITY = "com.github.fielddb.default.datum";

  /*
   * Make sure this is part of your ContentProvider <provider
   * android:name="com.github.fielddb.database.DatumContentProvider"
   * android:authorities="com.github.fielddb.speechrecognition.kartuli.datum"
   * android:exported="false" > </provider>
   */
  public static String DATA_IS_ABOUT_LANGUAGE_NAME_ASCII = "kartuli";
  public static String DATA_IS_ABOUT_LANGUAGE_NAME = "ქართული";

  /*
   * Example on how to support self signed ssl certificates by creating a
   * keystore from: http://transoceanic.blogspot.com
   * /2011/11/android-import-ssl-certificate-and-use.html
   *
   * keytool -importcert -v -trustcacerts -file ../acorpusserver.crt -alias
   * acorpusserver -keystore "sslkeystore.bks" -provider
   * org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath
   * bcprov-jdk15on-146.jar -storetype BKS -storepass "apasswordforyourkeystore"
   */
  public static String KEYSTORE_PASS = "apasswordforyourkeystore";

  public static String USER_FRIENDLY_DATA_NAME = "ფრაზა"; // "datum"
}
