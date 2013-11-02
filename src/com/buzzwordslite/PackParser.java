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

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Utility class for parsing input streams of JSON data into packs and cards.
 * There are times when we use iterators intstead of lists for performance
 * reasons.
 * @author Siramix Labs
 */
public class PackParser {
  
  private static final String TAG = "PackParser";

  /**
   * Return a linked-list of packs from a buffered reader
   * @param reader the buffered-reader containing json pack objects
   * @return a linked list of packs
   * @throws IOException if there is something wrong with the reader
   * @throws JSONException if the json is malformed
   */
  public static LinkedList<Pack> parsePacks(StringBuilder builder) throws IOException, JSONException {
    LinkedList<Pack> packList = new LinkedList<Pack>();
    Scanner scan = new Scanner(builder.toString());
    while (scan.hasNextLine()) {
      Pack pack = stringToPack(scan.nextLine());
      packList.add(pack);
    }
    return packList;
  }

  /**
   * Convert a string into a pack object
   * @param strPack the json string of a card
   * @return a pack object
   * @throws JSONException if the json is invalid in some way
   */
  public static Pack stringToPack(String strPack) throws JSONException {
    Log.d(TAG, "stringToPack");
    Log.d(TAG, "--> " + strPack);
    JSONObject curPack = new JSONObject(strPack);
    int curId = curPack.getInt("_id");
    String curName = curPack.getString("name");
    String curPath = curPack.getString("path");
    String curIconPath = curPack.getString("icon_path");
    int curPurchaseType= curPack.getInt("purchase_type");
    String curDescription = curPack.getString("description");
    int curVersion = curPack.getInt("version");
    String curPrice = curPack.getString("price");
    int curSize = curPack.getInt("size");
    Pack pack = new Pack(curId, curName, curPath, curIconPath, curDescription, 
                         curSize, curPurchaseType, curVersion, false, curPrice);
    return pack;
  }

  /**
   * Convert a json string into a card object
   * @param strCard the json string
   * @return the card
   * @throws JSONException if the json is invalid in some way
   */
  public static Card stringToCard(String strCard) throws JSONException {
    Log.d(TAG, "stringToCard");
    Log.d(TAG, "--> " + strCard);
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
