package com.github.fielddb;

import org.acra.ACRA;

public class BugReporter {

  public static void putCustomData(String type, String message) {
    if (!BuildConfig.DEBUG) {
      ACRA.getErrorReporter().putCustomData(type, message);
    }
  }

  public static void sendBugReport(String type) {
    if (!BuildConfig.DEBUG) {
      ACRA.getErrorReporter().putCustomData("androidTimestamp", System.currentTimeMillis() + "");
      ACRA.getErrorReporter().handleException(new Exception(type));
    }
  }
}
