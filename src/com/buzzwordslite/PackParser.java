/*****************************************************************************
 *  Buzzwords is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.buzzwordslite;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for parsing input streams of JSON data into packs and cards.
 * There are times when we use iterators intstead of lists for performance
 * reasons.
 * @author Siramix Labs
 */
public class PackParser {
  
  private static final String TAG = "PackParser";

  /**
   * Convert a json string into a card object
   * @param strCard the json string
   * @return the card
   * @throws JSONException if the json is invalid in some way
   */
  public static Card stringToCard(String strCard) throws JSONException {
    SafeLog.d(TAG, "stringToCard");
    SafeLog.d(TAG, "--> " + strCard);
    JSONObject curCard = new JSONObject(strCard);
    String curName = curCard.getString("title");
    int curId = curCard.getInt("_id");
    String curBadWords= curCard.getString("badwords");
    Card card = new Card(curId, curName, curBadWords, null);
    return card;
  }

  /**
   * Pass in a buffered reader and return an iterator to get cards from it.
   * Don't forget to close the reader!
   * @param reader the buffered reader containing newline-delimited json cards
   * @return an iterator of cards
   */
  public static CardJSONIterator parseCards(StringBuilder builder) {
    return new CardJSONIterator(builder);
  }

}
