package com.github.fielddb.datacollection;

import com.github.fielddb.Config;
import com.github.fielddb.BuildConfig;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceDetails implements LocationListener {
  String androidId = "unknown";
  String appVersion;

  String brand;
  int currentOrientation;

  String device;
  String hardware;
  double latitude = 0;
  double locationAccuracy = 0;
  double longitude = 0;
  String manufacturer;
  Context mContext;
  String mDeviceDetails;
  long min_dis = 10;
  long min_time = 100;

  String model;
  String osversion;
  String product;
  int screenHeight;

  double screenRatio;
  int screenWidth;
  int sdk;

  String serial = "unknown";
  String telephonyDeviceId = "unknown";
  String userFriendlyBuildID;
  String wifiMacAddress = "unknown";

  @SuppressLint("NewApi")
  public DeviceDetails(Context mContext) {
    super();
    this.mContext = mContext;

    try {
      this.appVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
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
    this.osversion = System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
    this.sdk = android.os.Build.VERSION.SDK_INT;
    if (this.sdk > 9) {
      this.serial = android.os.Build.SERIAL != null ? android.os.Build.SERIAL : "unknown";
    }

    this.screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
    this.screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
    this.screenRatio = (double) this.screenWidth / this.screenHeight;
    this.currentOrientation = mContext.getResources().getConfiguration().orientation;
    if (this.currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      this.screenRatio = this.screenHeight / this.screenWidth;
    }

    int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_WIFI_STATE);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_READ_PHONE_STATE);
    } else {
      this.wifiMacAddress = (((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo()).getMacAddress();
    }
    if (this.wifiMacAddress == null) {
      this.wifiMacAddress = "unknown";
    }

    permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
    } else {
      this.telephonyDeviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    if (this.telephonyDeviceId == null) {
      this.telephonyDeviceId = "unknown";
    }

    this.androidId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
    if (this.androidId == null) {
      this.androidId = "unknown";
    }
    LocationManager locationManager = null;

    locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    Criteria crta = new Criteria();
    crta.setAccuracy(Criteria.ACCURACY_FINE);
    crta.setAltitudeRequired(false);
    crta.setBearingRequired(false);
    crta.setCostAllowed(true);
    crta.setPowerRequirement(Criteria.POWER_LOW);

    String provider = locationManager.getBestProvider(crta, true);
    if ("network".equals(provider)) {
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "Using network for location provider.");
      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, this.min_time, this.min_dis, this);
      }
    } else if ("gps".equals(provider)) {
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "Using network for location provider.");
      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      }
    } else {
      if (BuildConfig.DEBUG)
        Log.d(Config.TAG, "Best location provider was not specified, using both network and gps.");

      permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        if (BuildConfig.DEBUG) {
          Log.d(Config.TAG, "Using network for location provider.");
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//          ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_READ_PHONE_STATE);
        } else {
          locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, this.min_time, this.min_dis, this);
        }
      }
      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        if (BuildConfig.DEBUG)
          Log.d(Config.TAG, "Using gps for location provider.");

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//          ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else {
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
      }
    }
    this.setDeviceDetails();
  }

  public String getCurrentDeviceDetails() {
    return this.setDeviceDetails();
  }

  @Override
  public void onLocationChanged(Location location) {
    this.longitude = location.getLongitude();
    this.latitude = location.getLatitude();
    this.locationAccuracy = location.getAccuracy();
    if (BuildConfig.DEBUG)
      Log.d(Config.TAG, "Location changed; " + this.longitude + ":" + this.latitude + " accuracy: "
          + this.locationAccuracy);
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

  public String setDeviceDetails() {
    String orientation = "landscape";
    if (this.currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      orientation = "portrait";
    }
    this.mDeviceDetails = "{name: '" + this.manufacturer + " " + this.model + "', model: '" + this.model
        + "', product: '" + this.product + "', manufacturer: '" + this.manufacturer + "', appversion: '"
        + this.appVersion + "', sdk: '" + this.sdk + "', osversion: '" + this.osversion + "',device: '" + this.device
        + "', screen: {height: '" + this.screenHeight + "', width: '" + this.screenWidth + "', ratio: '"
        + this.screenRatio + "', currentOrientation: '" + orientation + "'}, serial: '" + this.serial
        + "', identifier: '" + this.androidId + "', wifiMACaddress: '" + this.wifiMacAddress + "', timestamp: '"
        + System.currentTimeMillis() + "',location:{longitude: '" + this.longitude + "', latitude: '" + this.latitude
        + "', accuracy: '" + this.locationAccuracy + "'} , telephonyDeviceId:'" + this.telephonyDeviceId + "'}";
    return this.mDeviceDetails;
  }

}
