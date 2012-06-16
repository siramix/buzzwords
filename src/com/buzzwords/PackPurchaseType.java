package com.buzzwords;

public class PackPurchaseType {
  public static final int UNSET = -1;
  public static final int FREE = 0;
  public static final int PAY = 1;
  public static final int TWITTER = 2;
  public static final int FACEBOOK = 3;
  public static final int GOOGLE = 4;
  
  public static final int RESULT_NOCODE = 0;
  public static final int RESULT_TWITTER = 2;
  public static final int RESULT_FACEBOOK = 3;
  public static final int RESULT_GOOGLE = 4;
  /**
   * IDs of label strings for purchase buttons.
   */
  public static final int[] PURCHASE_LABEL_IDS = {
    R.string.packInfo_button_free,
    R.string.packInfo_button_pay,
    R.string.packInfo_button_tweet,
    R.string.packInfo_button_facebook,
    R.string.packInfo_button_googleplus
  };
  
  /**
   * Codes that will be returned when returning from the app that is
   * launched depending on the type of purchase being made.
   */
  public static final int[] PURCHASE_RESULT_CODES = {
    RESULT_NOCODE,
    RESULT_NOCODE,
    RESULT_TWITTER,
    RESULT_FACEBOOK,
    RESULT_GOOGLE
  };

}
