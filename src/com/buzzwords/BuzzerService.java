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
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class BuzzerService {

  /**
   * Instance of the BuzzerService that can be called
   */
  private static BuzzerService _instance = new BuzzerService();

  /**
   * TODO: Determine if this is the best way to check the state.
   */
  private static boolean isBuzzing = false;
  
  /**
   * Holds the unique ID for the game being connected to
   */
  private String gameToken;

  private final static String TAG = "BuzzerService";

  /**
   * Get the current instance of the BuzzerService
   * @return the current instance of the BuzzerService
   */	
  public static BuzzerService getInstance() {
    return _instance;
  }

  public String getLocaltoken() {
	return this.gameToken;
  }
  
  public boolean getLocalisBuzzing() {
	return isBuzzing;
  }
  /**
   * Takes a response from the server and processes the data on
   * the Buzzer's device.
   * @param gameData
   */
  public void updateBuzzer(JSONObject gameData) {
	
  }
  
  /**
   * Takes a response from the server and processes the data on
   * the Presenter's device.
   * @param gameData
   */
  public void updatePresenter(JSONObject gameData) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "updatePresenter(" + gameToken + ")");
    }		
  
  }
  
  public static boolean getBuzzing(String token) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getBuzz(" + token + ")");
    }		

    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost("http://www.siramix.com/buzz/server/getbuzz.php");		

    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("token", token));      
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
      HttpResponse response = httpClient.execute(httpPost); 
      if (checkBuzzStatus(response)) {        
        return true;
      }      
    } catch (Exception e) {
        Log.v("ioexception", e.toString());
    }

    return false;	  
  }

  /**
   * Processes a call to syncronize and start a networked game.
   * @param gameToken
   */
  public void setToken() {
    if (BuzzWordsApplication.DEBUG) {
	  Log.d(TAG, "syncGame(" + gameToken + ")");
	}		

    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost("http://www.siramix.com/buzz/server/start.php");		

    try {
      HttpResponse response = httpClient.execute(httpPost);       
	  BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String json = reader.readLine();
      JSONTokener tokener = new JSONTokener(json);
      JSONObject finalResult = new JSONObject(tokener);
      this.gameToken = finalResult.getString("token");
      
    } catch (Exception e) {
      Log.v("ioexception", e.toString());
    }     
  }  
  
  /**
   * Processes a call to syncronize and start a networked game.
   * @param gameToken
   */
  public boolean syncGame(String gameToken) {
    if (BuzzWordsApplication.DEBUG) {
	  Log.d(TAG, "syncGame(" + gameToken + ")");
	}		

    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost("http://www.siramix.com/buzz/server/isgamevalid.php");		

    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("gameToken", gameToken));      
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
      HttpResponse response = httpClient.execute(httpPost); 
      /*if (checkResponseStatus(response)) {
        return true;
      }*/     
    } catch (Exception e) {
      Log.v("ioexception", e.toString());
    }
    
    return false;     
  }
  
  /**
   * Sets the local service to the buzzing state and tells the 
   * server to do the same.  A successful response will be received
   * from the server if the server sets the state to 'buzzing'.
   * 
   * @param gameToken
   */
  public void setBuzzing(String gameToken) {
	if (BuzzWordsApplication.DEBUG) {
	  Log.d(TAG, "setBuzzing(" + gameToken + ")");
	}
	
	isBuzzing = true;
	
	HttpClient httpClient = new DefaultHttpClient();
	HttpPost httpPost = new HttpPost("http://www.siramix.com/buzz/server/setbuzz.php");
	
    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("token", gameToken));
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = httpClient.execute(httpPost);
      //checkResponseStatus(response); // stub status check
      } catch (Exception e) {
        Log.v("ioexception", e.toString());
      }
  }

  /**
   * Sets the local service to the buzzing state and tells the 
   * server to do the same.  A successful response will be received
   * from the server if the server sets the state to 'buzzing'.
   * 
   * @param gameToken
   */
  public void unsetBuzzing(String gameToken) {
	if (BuzzWordsApplication.DEBUG) {
	  Log.d(TAG, "setBuzzing(" + gameToken + ")");
	}
	
	isBuzzing = true;
	
	HttpClient httpClient = new DefaultHttpClient();
	HttpPost httpPost = new HttpPost("http://www.siramix.com/buzz/server/unsetbuzz.php");
	
    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("token", gameToken));   
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      HttpResponse response = httpClient.execute(httpPost);
      //checkResponseStatus(response); // stub status check
      } catch (Exception e) {
        Log.v("ioexception", e.toString());
      }
  }
  
  /**
   * Returns true for good response, false for bad
   * @param response
   * @return
   */
  private static boolean checkBuzzStatus(HttpResponse response) {
	try {
	  BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String json = reader.readLine();
      JSONTokener tokener = new JSONTokener(json);
      JSONObject finalResult = new JSONObject(tokener);
      int status = Integer.parseInt(finalResult.getString("status"));
      int buzzing = Integer.parseInt(finalResult.getString("buzzing"));
      
      if (status == 1 && buzzing == 1) {
    	  return true;
      }
	} catch (Exception e) {
	  Log.v("ioexception", e.toString());
	  return false;
	}
	
    return false;
  }
  
}
