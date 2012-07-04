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
package com.buzzwords;

import java.io.Serializable;

import android.util.Log;

/**
 * Helpful structure for holding pack metadata. You can also attach cards to
 * it, but that is not a requirement.
 * 
 * Two uses for Pack objects -- database inserts and as a helper during playtime.
 * Refer to the member variables to see which fields are relevant for the database
 * and which are relevant to playtime.
 * 
 * @author Siramix Labs
 */
public class Pack implements Serializable {

  private static final long serialVersionUID = -3764144456280018930L;
  
  // Model fields for database and server
  private int mId;
  private String mName;
  private String mPath;
  private String mDescription;
  private int mPurchaseType;
  private String mUpdateMessage;
  private int mServerSize;
  private int mVersion;
  private int mIconID;
  
  // Model fields for app at playtime
  private int mSize;
  private int mNumSeen;
  private float mWeight;
  private int mNumToPullNext;
  private boolean mInstalled;
  
  private static final String TAG = "Pack";
  
  /**
   * Default constructor
   */
  public Pack() {
    this(-1, "","", "","", -1, -1, -1, PackPurchaseType.UNSET, false);
    Log.d(TAG, "null consructor Pack()");
  }

  /**
   * Standard constructor
   * @param id
   * @param name
   * @param path
   * @param description 
   * @param updateMessage
   * @param version
   * @param size
   * @param installed (whether the pack has been installed yet)
   */
  public Pack(int id, String name, String path, String description,
              String updateMessage, int iconID, int serverSize, int purchaseType,
              int version, boolean installed) {
    Log.d(TAG, "constructor Pack(args)");
    mId = id;
    mName = name;
    mPath = path;
    mDescription = description;
    mUpdateMessage = updateMessage;
    mIconID = iconID;
    mServerSize = serverSize;
    mPurchaseType = purchaseType;
    mVersion = version;
    mWeight = -1;
    mSize = -1;
    mNumSeen = -1;
    mNumToPullNext = -1;
    mInstalled = installed;
  }
  
  /**
   * @return the id of the pack (MUST NOT CHANGE)
   */
  public int getId() {
    return mId;
  }

  /**
   * @return the name of the pack
   */
  public String getName() {
    return mName;
  }

  /**
   * @return the update message
   */
  public String getUpdateMessage() {
    return mUpdateMessage;
  }

  /**
   * @return the path from server-root with which to retrieve cards.
   */
  public String getPath() {
    return mPath;
  }
  
  
  /**
   * @return the pack's icon resource id.
   */
  public int getIconID() {
    return mIconID;
  }


  /**
   * @return the version of the pack
   */
  public int getVersion() {
    return mVersion;
  }
  
  /**
   * @return the description string of the pack
   */
  public String getDescription() {
    return mDescription;
  }
  
  /**
   * @return the total number of cards in the pack
   */
  public int getSize() {
    return mSize;
  }
  
  /**
   * Size of the pack according to our web server
   * @return the pack size according to our server
   */
  public int getServerSize() {
    return mServerSize;
  }
  
  /**
   * @return the number of cards already seen in the pack
   */
  public int getNumSeen() {
    return mNumSeen;
  }
  
  /**
   * @return the type of pack purchased
   */
  public int getPurchaseType() {
    return mPurchaseType;
  }
  
  /**
   * @return the weight of the pack relative to the entire deck
   */
  public float getWeight() {
    return mWeight;
  }
  
  /**
   * @return the number of cards to pull from the pack next
   */
  public int getNumToPullNext() {
    return mNumToPullNext;
  }
  
  // TODO This may need to tie to purchase status instead of "in db" or not.
  public boolean isInstalled() {
    return mInstalled;
  }
  
  /**
   * Set the size of the pack
   * @param the number of cards in the pack
   */
  public void setSize(int numCards) {
    mSize = numCards;
  }
  
  /**
   * Set the number of cards seen in a pack
   * @param numSeen the number of cards seen in the pack
   */
  public void setNumSeen(int numSeen) {
    mNumSeen = numSeen;
  }
  
  /**
   * Set the weight of the pack
   * @param weight of the pack relative to all selected packs
   */
  public void setWeight(float weight) {
    mWeight = weight;
  }

  /**
   * Set the number of playable cards in the pack
   * @param numCards number of cards that are playable in the pack
   */
  public void setNumToPullNext(int numCards) {
    mNumToPullNext = numCards;
  }
  
  /**
   * Return a string representation of all pack data.
   */
  @Override
  public String toString() {
    String ret = "";
    ret += "===== PACK DATA  ====\n";
    ret += "---- db fields --\n";
    ret += "   pack.Id: " + String.valueOf(mId) + "\n";
    ret += "   pack.Name: " + mName + "\n";
    ret += "   pack.Path: " + mPath + "\n";
    ret += "   pack.Description: " + mDescription + "\n";
    ret += "   pack.PurchaseType: " + String.valueOf(mPurchaseType) + "\n";    
    ret += "   pack.Version: " + String.valueOf(mVersion) + "\n";
    ret += "---- runtime fields --\n";
    ret += "   pack.ServerSize: " + String.valueOf(mServerSize) + "\n";
    ret += "   pack.Size: " + String.valueOf(mSize) + "\n";
    ret += "   pack.NumSeen: " + String.valueOf(mNumSeen) + "\n";
    ret += "   pack.UpdateMessage: " + mUpdateMessage + "\n";
    ret += "   pack.Weight: " + String.valueOf(mWeight) + "\n";
    ret += "   pack.NumToPullNext: " + String.valueOf(mNumToPullNext) + "\n";
    ret += "   pack.mInstalled: " + String.valueOf(mInstalled) + "\n";
    return ret;
  }
}