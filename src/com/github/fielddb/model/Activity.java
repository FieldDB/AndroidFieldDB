package com.github.fielddb.model;

import android.util.Log;

import com.github.fielddb.BugReporter;
import com.github.fielddb.BuildConfig;
import com.github.fielddb.Config;

public class Activity {
  String _id;
  String _rev;
  long timestamp;
  String verb;
  String verbicon;
  String directobject;
  String directobjecticon;
  String indirectobject;
  String context;
  String teamOrPersonal;
  String user;
  long dateModified;
  String appVersion;

  public static void sendActivity(String action, String deviceDetails, String type) {
    if (!BuildConfig.DEBUG) {
      BugReporter.putCustomData("action", action);
      BugReporter.putCustomData("androidTimestamp", System.currentTimeMillis() + "");
      if (!"{}".equals(deviceDetails) && deviceDetails != null) {
        BugReporter.putCustomData("deviceDetails", deviceDetails);
      }
      BugReporter.sendBugReport(type);
    } else {
      Log.d(Config.TAG, "Skipping activity (we are in a debug build)" + action);
    }
  }

  public Activity() {
    super();
    this.timestamp = System.currentTimeMillis();
    this._id = this.timestamp + "";
  }

  public Activity(String _id, String _rev, long timestamp, String verb, String verbicon, String directobject,
      String directobjecticon, String indirectobject, String context, String teamOrPersonal, String user,
      long dateModified, String appVersion) {
    super();
    this._id = _id;
    this._rev = _rev;
    this.timestamp = timestamp;
    this.verb = verb;
    this.verbicon = verbicon;
    this.directobject = directobject;
    this.directobjecticon = directobjecticon;
    this.indirectobject = indirectobject;
    this.context = context;
    this.teamOrPersonal = teamOrPersonal;
    this.user = user;
    this.dateModified = dateModified;
    this.appVersion = appVersion;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String get_rev() {
    return _rev;
  }

  public void set_rev(String _rev) {
    this._rev = _rev;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getVerb() {
    return verb;
  }

  public void setVerb(String verb) {
    this.verb = verb;
  }

  public String getVerbicon() {
    return verbicon;
  }

  public void setVerbicon(String verbicon) {
    this.verbicon = verbicon;
  }

  public String getDirectobject() {
    return directobject;
  }

  public void setDirectobject(String directobject) {
    this.directobject = directobject;
  }

  public String getDirectobjecticon() {
    return directobjecticon;
  }

  public void setDirectobjecticon(String directobjecticon) {
    this.directobjecticon = directobjecticon;
  }

  public String getIndirectobject() {
    return indirectobject;
  }

  public void setIndirectobject(String indirectobject) {
    this.indirectobject = indirectobject;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getTeamOrPersonal() {
    return teamOrPersonal;
  }

  public void setTeamOrPersonal(String teamOrPersonal) {
    this.teamOrPersonal = teamOrPersonal;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public long getDateModified() {
    return dateModified;
  }

  public void setDateModified(long dateModified) {
    this.dateModified = dateModified;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

}
