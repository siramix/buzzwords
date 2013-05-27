package com.buzzwords;

/**
 * Configurable constants belong in this file. This file serves as a template
 * that can be modified at build time by ant by parameterizing any const. Ant
 * will copy this file to src/ and make any necessary replacements.
 */
public class Config {

  public static final String packList = "packs.json";
  public static final String packBaseUri = "https://s3.amazonaws.com/siramix.buzzwords/bw-packdata-test/";
  public static final String storeUriBuzzwords = "http://www.amazon.com/gp/mas/dl/android?p=com.buzzwords";
  public static final String storeUriBuzzwordsLite = "http://www.amazon.com/gp/mas/dl/android?p=com.buzzwordslite";
  
  public static final String buzzwordsFBAppLauncher = "fb://page/472759256084535";
  public static final String buzzwordsFBPage = "https://www.facebook.com/buzzwordsapp";
}
