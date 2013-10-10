package ca.ilanguage.oprime.model;

import ca.ilanguage.oprime.Config;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceDetails implements LocationListener {
  protected String TAG = Config.OPRIME_TAG;
  protected boolean D = true;

  Context mContext;
  String mDeviceDetails;

  String appVersion;

  String model;
  String product;
  String device;
  String hardware;
  String brand;
  String userFriendlyBuildID;
  String manufacturer;
  String osversion;
  int sdk;
  String serial = "unknown";

  double screenRatio;
  int screenHeight;
  int screenWidth;
  int currentOrientation;

  String wifiMacAddress = "unknown";
  String telephonyDeviceId = "unknown";
  String androidId = "unknown";

  double longitude = 0;
  double latitude = 0;
  double locationAccuracy = 0;
  long min_dis = 10;
  long min_time = 100;

  @SuppressLint("NewApi")
  public DeviceDetails(Context mContext, boolean debugMode, String tag) {
    super();
    this.mContext = mContext;
    this.D = debugMode;
    this.TAG = tag;

    try {
      this.appVersion = mContext.getPackageManager().getPackageInfo(
          mContext.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      this.appVersion = "0";
    }
    this.model = android.os.Build.MODEL;
    this.hardware = android.os.Build.HARDWARE;
    this.brand = android.os.Build.BRAND;
    this.manufacturer = android.os.Build.MANUFACTURER;
    if (this.manufacturer != null) {
      this.manufacturer = this.manufacturer.substring(0, 1).toUpperCase()
          + this.manufacturer.substring(1, this.manufacturer.length() - 1);
    }
    this.userFriendlyBuildID = android.os.Build.DISPLAY;
    this.product = android.os.Build.PRODUCT;
    this.device = android.os.Build.DEVICE;
    this.osversion = System.getProperty("os.version") + "("
        + android.os.Build.VERSION.INCREMENTAL + ")";
    this.sdk = android.os.Build.VERSION.SDK_INT;
    if (this.sdk > 9) {
      serial = android.os.Build.SERIAL != null ? android.os.Build.SERIAL
          : "unknown";
    }

    this.screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
    this.screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
    this.screenRatio = (double) screenWidth / screenHeight;
    this.currentOrientation = mContext.getResources().getConfiguration().orientation;
    if (this.currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      this.screenRatio = screenHeight / screenWidth;
    }

    this.wifiMacAddress = (((WifiManager) mContext
        .getSystemService(Context.WIFI_SERVICE)).getConnectionInfo())
        .getMacAddress();
    if (this.wifiMacAddress == null) {
      this.wifiMacAddress = "unknown";
    }

    this.telephonyDeviceId = ((TelephonyManager) mContext
        .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    if (this.telephonyDeviceId == null) {
      this.telephonyDeviceId = "unknown";
    }

    this.androidId = Secure.getString(mContext.getContentResolver(),
        Secure.ANDROID_ID);
    if (this.androidId == null) {
      this.androidId = "unknown";
    }
    LocationManager locationManager = null;

    locationManager = (LocationManager) mContext
        .getSystemService(Context.LOCATION_SERVICE);
    Criteria crta = new Criteria();
    crta.setAccuracy(Criteria.ACCURACY_FINE);
    crta.setAltitudeRequired(false);
    crta.setBearingRequired(false);
    crta.setCostAllowed(true);
    crta.setPowerRequirement(Criteria.POWER_LOW);

    String provider = locationManager.getBestProvider(crta, true);
    if ("network".equals(provider)) {
      if (D)
        Log.d(TAG, "Using network for location provider.");
      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, min_time, min_dis, this);
      }
    } else if ("gps".equals(provider)) {
      if (D)
        Log.d(TAG, "Using network for location provider.");
      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
            0, this);
      }
    } else {
      if (D)
        Log.d(TAG,
            "Best location provider was not specified, using both network and gps.");

      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        if (D)
          Log.d(TAG, "Using network for location provider.");

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, min_time, min_dis, this);
      }
      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        if (D)
          Log.d(TAG, "Using gps for location provider.");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
            0, this);
      }
    }
    this.setDeviceDetails();
  }

  public String getCurrentDeviceDetails() {
    return setDeviceDetails();
  }

  public String setDeviceDetails() {
    String orientation = "landscape";
    if (this.currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      orientation = "portrait";
    }
    this.mDeviceDetails = "{name: '" + this.manufacturer + " " + this.model
        + "', model: '" + this.model + "', product: '" + this.product
        + "', manufacturer: '" + this.manufacturer + "', appversion: '"
        + this.appVersion + "', sdk: '" + this.sdk + "', osversion: '"
        + this.osversion + "',device: '" + this.device
        + "', screen: {height: '" + this.screenHeight + "', width: '"
        + this.screenWidth + "', ratio: '" + this.screenRatio
        + "', currentOrientation: '" + orientation + "'}, serial: '"
        + this.serial + "', identifier: '" + this.androidId
        + "', wifiMACaddress: '" + this.wifiMacAddress + "', timestamp: '"
        + System.currentTimeMillis() + "',location:{longitude: '"
        + this.longitude + "', latitude: '" + this.latitude + "', accuracy: '"
        + this.locationAccuracy + "'} , telephonyDeviceId:'"
        + this.telephonyDeviceId + "'}";
    return mDeviceDetails;
  }

  @Override
  public void onLocationChanged(Location location) {
    this.longitude = location.getLongitude();
    this.latitude = location.getLatitude();
    this.locationAccuracy = location.getAccuracy();
    if (D)
      Log.d(TAG, "Location changed; " + this.longitude + ":" + this.latitude
          + " accuracy: " + this.locationAccuracy);
  }

  @Override
  public void onProviderDisabled(String provider) {
  }

  @Override
  public void onProviderEnabled(String provider) {
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

}
