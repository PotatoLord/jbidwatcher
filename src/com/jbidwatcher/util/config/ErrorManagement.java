package com.jbidwatcher.util.config;
/*
 * Copyright (c) 2000-2007, CyberFOX Software, Inc. All Rights Reserved.
 *
 * Developed by mrs (Morgan Schweers)
 */

import java.io.*;
import java.util.Date;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "UtilityClass"})
public class ErrorManagement {
  public static int MAX_BUFFER_SIZE = 50000;
  private static PrintWriter mLogWriter = null;
  private static ScrollingBuffer sLogBuffer = new ScrollingBuffer(MAX_BUFFER_SIZE);
  private ErrorManagement() { }

  private static void init() {
    String sep = System.getProperty("file.separator");
    String home = JConfig.getHomeDirectory("jbidwatcher");

    String doLogging = JConfig.queryConfiguration("logging", "true");
    if(doLogging.equals("true")) {
      if(mLogWriter == null) {
        try {
          File fp;
          String increment = "";
          int stepper = 1;
          do {
            fp = new File(home + sep + "errors" + increment + ".log");
            increment = "." + stepper++;
          } while(fp.exists());

          mLogWriter = new PrintWriter(new FileOutputStream(fp));
        } catch(IOException ioe) {
          System.err.println("FAILED TO OPEN AN ERROR LOG.");
          ioe.printStackTrace();
        }
      }
    }
  }

  public static void closeLog() {
    if(mLogWriter != null) {
      mLogWriter.close();
      mLogWriter = null;
    }
  }

  public static void logMessage(String msg) {
    init();
    Date log_time = new Date();

    System.err.println(log_time + ": " + msg);

    String logMsg = log_time + ": " + msg;
    sLogBuffer.addLog(logMsg);

    String doLogging = JConfig.queryConfiguration("logging", "true");
    if(doLogging.equals("true")) {
      if(mLogWriter != null) {
        mLogWriter.println(logMsg);
        mLogWriter.flush();
      }
    }
  }

  public static void logDebug(String msg) {
    if(JConfig.debugging) logMessage(msg);
  }

  public static void logVerboseDebug(String msg) {
    if(JConfig.queryConfiguration("debug.uber", "false").equals("true")) logMessage(msg);
  }

  public static void handleDebugException(String sError, Throwable e) {
    if(JConfig.debugging) handleException(sError, e);
  }

  public static void handleException(String sError, Throwable e) {
    init();

    Date log_time = new Date();

    if(sError == null || sError.length() == 0) {
      System.err.println("[" + log_time + "]");
    } else {
      System.err.println(log_time + ": " + sError);
    }
    e.printStackTrace();

    String doLogging = JConfig.queryConfiguration("logging", "true");

    String logMsg;
    if (sError == null || sError.length() == 0) {
      logMsg = "[" + log_time + "]";
    } else {
      logMsg = log_time + ": " + sError;
    }

    sLogBuffer.addLog(logMsg);
    sLogBuffer.addStackTrace(e);

    if(doLogging.equals("true")) {
      if(mLogWriter != null) {
        mLogWriter.println(logMsg);
        e.printStackTrace(mLogWriter);
        mLogWriter.flush();
      }
    }
  }

  public static void logFile(String msgtop, StringBuffer dumpsb) {
    String doLogging = JConfig.queryConfiguration("logging", "true");
    if(doLogging.equals("true")) {
      if(JConfig.debugging) {
        init();
        if(mLogWriter != null) {
          mLogWriter.println("+------------------------------");
          mLogWriter.println("| " + msgtop);
          mLogWriter.println("+------------------------------");
          if(dumpsb != null) {
            mLogWriter.println(dumpsb);
          } else {
            mLogWriter.println("(null)");
          }
          mLogWriter.println("+------------end---------------");
          mLogWriter.flush();
        }
      }
    }
  }

  public static StringBuffer getLog() {
    return sLogBuffer.getLog();
  }
}
