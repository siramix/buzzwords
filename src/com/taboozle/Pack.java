package com.taboozle;

import android.net.*;
import android.provider.*;

/**
 * Convenience definitions for CardProvider
 */
public final class Pack
{

  /**
   * Constant for the authority to be used in the URI
   */
  public static final String AUTHORITY = "com.taboozle.Pack";

  // This class cannot be instantiated
  private Pack()
  {
  }

  /**
   * Notes table
   */
  public static final class Cards implements BaseColumns
  {

    // This class cannot be instantiated
    private Cards()
    {
    }

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY
                                                     + "/cards" );

    /**
     * The MIME type of {@link #CONTENT_URI} providing a pack of cards.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.taboozle.card";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single card
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.taboozle.card";

    /**
     * The title of the card
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String TITLE = "title";

    /**
     * The words the user cannot say when describing the card
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String BAD_WORDS = "badwords";
  }
}
