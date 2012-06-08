/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * @author Siramix Labs
 * Client for communicating with the Buzzwords pack server
 */
public class PackClient {
  
  private static final String TAG = "PackClient";
  
  /**
   * URL Constants
   */
  private static final String URL_BASE = "http://siramix.com/buzzwords/packs/";
  private static final String PAY_LIST_URL = "premiumpacks.json";
  private static final String FREE_LIST_URL = "freepacks.json";
  
  /**
   * Members
   */
  private static PackClient mInstance = null;

  /**
   * Return the instance of the PackClient object
   * @return
   */
  public static PackClient getInstance() {
    Log.d(TAG, "getInstance");
    if(mInstance == null) {
      mInstance = new PackClient();
    }
    return mInstance;
  }

  /**
   * Get all of the packs available on the server for pay
   * @return a LinkedList of Packs representing the pack that is available
   * @throws IOException if the request to the server fails
   * @throws URISyntaxException if the uri is malformed
   * @throws JSONException if the JSON is invalid
   */
  public LinkedList<Pack> getPayPacks() throws IOException, URISyntaxException, JSONException {
    Log.d(TAG, "getPayPacks");
    StringBuilder in = null;
    LinkedList<Pack> ret = null;
    in = doHTTPGet(URL_BASE+PAY_LIST_URL);
    ret = PackParser.parsePacks(in);
    return ret;
  }

  /**
   * Get all of the packs available on the server that don't go through the market (free)
   * @return a LinkedList of Packs representing the pack that is available
   * @throws IOException if the request to the server fails
   * @throws URISyntaxException if the uri is malformed
   * @throws JSONException if the JSON is invalid
   */
  public LinkedList<Pack> getFreePacks() throws IOException, URISyntaxException, JSONException {
    Log.d(TAG, "getFreePacks");
    StringBuilder in = null;
    LinkedList<Pack> ret = null;
    in = doHTTPGet(URL_BASE+FREE_LIST_URL);
    ret = PackParser.parsePacks(in);
    return ret;
  }

  /**
   * Get the cards associated with a given pack object. The iterator actually
   * catches and handles IOExceptions in this function because of its parent
   * API requirements.
   * @param pack the pack to be retrieved from the server
   * @return an iterator over the cards
   * @throws IOException if the request to the server fails
   * @throws URISyntaxException if the URI is invalid in some way
   */
  public CardJSONIterator getCardsForPack(Pack pack) throws IOException, URISyntaxException {
    Log.d(TAG, "getCardsForPack");
    StringBuilder in = null;
    CardJSONIterator ret = null;
    String packPath = pack.getPath();
    in = doHTTPGet(URL_BASE+packPath);
    ret = PackParser.parseCards(in);
    return ret;
  }

  /**
   * Perform an HTTP get on a URL and return the response. This is a helper
   * function to be used in pack and card fetching
   * @param url the address to perform the GET upon
   * @return a BufferedReader of the response body
   * @throws IOException if the request fails in some way
   * @throws URISyntaxException if the URI is malformed
   */
  private static StringBuilder doHTTPGet(String url) throws IOException, URISyntaxException {
    Log.d(TAG, "doHTTPGet()");
    HttpClient client = new DefaultHttpClient();
    HttpGet request = new HttpGet();
    request.setURI(new URI(url));
    HttpResponse response = client.execute(request);
    BufferedReader reader = new BufferedReader
      (new InputStreamReader(response.getEntity().getContent()));
    StringBuilder ret = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      ret.append(line).append("\n");
    }
    return ret;
  }
}
