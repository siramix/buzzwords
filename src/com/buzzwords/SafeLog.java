package com.buzzwords;

import android.util.Log;

/**
 * Safelog is a custom wrapper for Log which prevents logging
 * of debug and info messages on live devices. It also allows
 * us to avoid having to strip debug messages at runtime. This
 * also addresses the recommended usage of Log which is that...
 * "Before you make any calls to a logging method you should check to see
 *  if your tag should be logged."  
 */
public class SafeLog {

  /**
   * Log a DEBUG message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void d(String tag, String msg) {
    if (Log.isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, msg);
    }
  }

  /**
   * Log an INFO message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void i(String tag, String msg) {
    if (Log.isLoggable(tag, Log.INFO)) {
      Log.i(tag, msg);
    }
  }

  /**
   * Log an ERROR message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void e(String tag, String msg) {
    if (Log.isLoggable(tag, Log.ERROR)) {
      Log.e(tag, msg);
    }
  }
  
  /**
   * Log an ERROR message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void e(String tag, String msg, Throwable tr) {
    if (Log.isLoggable(tag, Log.ERROR)) {
      Log.e(tag, msg);
    }
  }

  /**
   * Log a VERBOSE message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void v(String tag, String msg) {
    if (Log.isLoggable(tag, Log.VERBOSE)) {
      Log.v(tag, msg);
    }
  }

  /**
   * Log a WARNING message only if device is loggable.
   * @param tag Used to identify the source of a log message. 
   *            It usually identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static void w(String tag, String msg) {
    if (Log.isLoggable(tag, Log.WARN)) {
      Log.w(tag, msg);
    }
  }
}