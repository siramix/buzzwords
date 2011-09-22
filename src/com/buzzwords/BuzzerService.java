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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class BuzzerService {

  /**
   * Instance of the BuzzerService that can be called
   */
  private static BuzzerService _instance = new BuzzerService();

  /**
   * Determine if this is the best way to check the state.
   */
  private static boolean isBuzzing = false;
  
  /**
   * Holds the unique ID for the game being connected to
   */
  private static String gameToken;

  /**
   * TAG for debugging.  Every method should include a debug tag.
   */
  private final static String TAG = "BuzzerService";

  /**
   * Null constructor for the BuzzerService.
   */
  private BuzzerService() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "BuzzerService()");
    } 
    gameToken = "";
    isBuzzing = false;
  }
  
  /**
   * Get the current instance of the BuzzerService
   * @return the current instance of the BuzzerService
   */	
  public static BuzzerService getInstance() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getInstance()");
    } 
    return _instance;
  }

  /**
   * Get the game token stored locally (as opposed to getting a token
   * from the server.)
   * @return string token that will be used to pair phones
   */
  public static String getLocalToken() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getLocaltoken()");
    } 
    return gameToken;
  }
  
  /**
   * Get the buzzing status that is stored locally (as opposed 
   * to the status that exists on the server.)
   * @return true for buzzing, false for not
   */
  public static boolean getLocalisBuzzing() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getLocalisBuzzing()");
    } 
    return isBuzzing;
  }  
  
  /**
   * Sends a request to buzzserver to check the status of the buzzer
   * for a specified gameToken.  If true, the local isBuzzing status
   * will be turned on.  Otherwise isBuzzing will be set to false.
   * @param token
   * @return
   */
  public static boolean getBuzzing(String token) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getBuzz(" + token + ")");
    }   
    
    JSONObject responseJSON = sendBuzzServerRequest
        ("http://www.siramix.com/buzz/server/getbuzz.php?", gameToken, null, null, null, null);
      
    try {
      if (Integer.parseInt(responseJSON.getString("buzzing")) == 1 && validateResponse(responseJSON)) {
        isBuzzing = true;
      }
      else {     
        isBuzzing = false;
      }   
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    return isBuzzing;   
  }
  /**
   * Takes a response from the server and processes the data on
   * the Buzzer's device.
   * @param gameData
   */
  public static void updateBuzzer(JSONObject gameData) {
	
  }
  
  /**
   * Takes a response from the server and processes the data on
   * the Presenter's device.
   * @param gameData
   */
  public static void updatePresenter(JSONObject gameData) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "updatePresenter(" + gameToken + ")");
    }		
  
  }

  /**
   * Processes a call to synchronize and start a networked game.  
   * The local gameToken will be set and will be used to identify
   * the active joinable games by another phone.
   */
  public static void newGame() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "syncGame(" + gameToken + ")");
    }		
    
    JSONObject responseJSON = sendBuzzServerRequest
        ("http://www.siramix.com/buzz/server/newgame.php?", null, "31.5", "31.5", null, null);
      
    try {
      gameToken = responseJSON.getString("token");
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }  
  
  /**
   * Sends geographic data to the server to see if a game can be joined.
   * @return An ArrayList of each available game token as a string
   */
  public static ArrayList<String> joinGame() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "syncGame(" + gameToken + ")");
    }   
    
    JSONObject responseJSON = sendBuzzServerRequest
        ("http://www.siramix.com/buzz/server/joingame.php?", null, "31.5", "31.5", null, null);
      
    try {
      JSONArray gamesJSONArray = responseJSON.getJSONArray("games");
      ArrayList<String> gamesArray = new ArrayList<String>();
      for (int i=0; i<gamesJSONArray.length(); ++i) {
        gamesArray.add(gamesJSONArray.getString(i));
      }
      return gamesArray;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    return null;
  }  
    
  /**
   * Sets the local service to the buzzing state and tells the 
   * server to do the same.  A successful response will be received
   * from the server if the server sets the state to 'buzzing'.
   * 
   * @param gameToken
   */
  public static void setBuzzing(String gameToken) {
  	if (BuzzWordsApplication.DEBUG) {
  	  Log.d(TAG, "setBuzzing(" + gameToken + ")");
  	}
  	
    JSONObject responseJSON = sendBuzzServerRequest
        ("http://www.siramix.com/buzz/server/setbuzz.php?", gameToken, null, null, null, null);
      
    try {
      if (validateResponse(responseJSON)) {
        isBuzzing = true;
      }        
      else {
        isBuzzing = false;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }  	
  }

  /**
   * Sets the local service to the buzzing state and tells the 
   * server to do the same.  A successful response will be received
   * from the server if the server sets the state to 'buzzing'.
   * 
   * @param gameToken
   */
  public static void unsetBuzzing(String gameToken) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "unsetBuzzing(" + gameToken + ")");
    }
    
    JSONObject responseJSON = sendBuzzServerRequest
        ("http://www.siramix.com/buzz/server/unsetbuzz.php?", gameToken, null, null, null, null);
      
    try {
      if (validateResponse(responseJSON)) {
        isBuzzing = false;
      }        
      else {
        Log.d(TAG, "Unsetbuzz status returned 0.  Verify request to unset buzzer.");
        isBuzzing = false;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }   
  }
  
  /**
   * Sends a request to Buzz Server and returns a tokenized JSON Object that
   * can be parsed for results.  This is should be a universal request that can
   * be used to call any of the php functions on buzzserver (setBuzz, newgame, etc).
   * @param URL 
   * @param token
   * @param lat
   * @param lon
   * @param cardTitle
   * @param Badwords
   * @return
   */
  private static JSONObject sendBuzzServerRequest(String URL, 
                  String token, String lat, String lon, String cardTitle, String Badwords) {
    
    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(URL);
    
    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
      if (token != null) {
        nameValuePairs.add(new BasicNameValuePair("token", token));
      }
      if (lat != null) {
        nameValuePairs.add(new BasicNameValuePair("lat", lat));
      }
      if (lon != null) {
        nameValuePairs.add(new BasicNameValuePair("long", lon));
      }
      if (cardTitle != null) {
        nameValuePairs.add(new BasicNameValuePair("cardTitle", cardTitle));
      }
      if (Badwords != null) {
        nameValuePairs.add(new BasicNameValuePair("badWords", Badwords));
      }
      
      Log.d("********************" + TAG, " Request sent: " + nameValuePairs.toString());
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
      HttpResponse response = httpClient.execute(httpPost);
      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String json = reader.readLine();
      JSONTokener tokener = new JSONTokener(json);
      JSONObject finalResult = new JSONObject(tokener);
      
      return finalResult;
      } catch (Exception e) {
        Log.v("ioexception", e.toString());
        return null;
      }
  }
  
  /**
   * Returns true for good response, false for bad
   * @param response
   * @return
   * @throws JSONException 
   * @throws NumberFormatException 
   */
  private static boolean validateResponse(JSONObject resultJSON) throws NumberFormatException, JSONException {
      int status = Integer.parseInt(resultJSON.getString("status"));
      if (status == 1) {
    	  return true;
      }
      else {
        return false;
      }
    }
  
}
