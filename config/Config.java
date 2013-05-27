package com.buzzwords;

/**
 * Configurable constants belong in this file. This file serves as a template
 * that can be modified at build time by ant by parameterizing any const. Ant
 * will copy this file to src/ and make any necessary replacements.
 */
public class Config {

  public static final boolean DEBUG_TIMERTICKS = false;
  public static String packBaseUri = @CONFIG.PACKURI@;
  public static String storeURI_Buzzwords = @CONFIG.BUZZWORDSSTORE@;
  public static String storeURI_BuzzwordsLite = @CONFIG.BUSSWORDSLITESTORE@;
  
  public static final String buzzwordsRedirectUri = "http://www.siramix.com/buzzwordsappstore";
  public static final String buzzwordsFBAppLauncher = "fb://page/472759256084535";
  public static final String buzzwordsFBPage = "https://www.facebook.com/buzzwordsapp";
}
