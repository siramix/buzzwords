package com.buzzwords;

/**
 * Configurable constants belong in this file. This file serves as a template
 * that can be modified at build time by ant by parameterizing any const. Ant
 * will copy this file to src/ and make any necessary replacements.
 */
public class Config {
  public static final String packBaseUri = @CONFIG.PACKURI@;
  public static final String storeUriBuzzwords = @CONFIG.BUZZWORDSSTORE@;
  public static final String storeUriBuzzwordsLite = @CONFIG.BUSSWORDSLITESTORE@;
}
