package com.buzzwords;

public class PackPurchaseConsts {
  public static final int PACKTYPE_UNSET = -1;
  public static final int PACKTYPE_FREE = 0;
  public static final int PACKTYPE_PAY = 1;
  public static final int PACKTYPE_SOCIAL = 2;
  
  public static final int FACEBOOK_PACK_ID = 2;
  
  public static final int RESULT_NOCODE = 100;
  public static final int RESULT_FACEBOOK = 101;

  /**
   * IDs of label strings for purchase buttons.
   */
  public static final int[] PURCHASE_LABEL_IDS = {
    R.string.packInfo_button_free,
    R.string.packInfo_button_pay,
    R.string.packInfo_button_facebook,
  };
  
  /**
   * Codes that will be returned when returning from the app that is
   * launched depending on the type of purchase being made.
   */
  public static final int[] PURCHASE_RESULT_CODES = {
    RESULT_NOCODE,
    RESULT_NOCODE,
    RESULT_FACEBOOK,
  };

}
