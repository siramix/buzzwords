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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;


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
  private static final String PACK_LIST_URL = "packs.json";
  
  /**
   * Members
   */
  private static PackClient mInstance = null;
  
//  private static Map<String, Drawable> mDrawableMap;
  private static Map<String, Bitmap> mBitmapMap;

  /**
   * Return the instance of the PackClient object
   * @return
   */
  public static PackClient getInstance() {
    Log.d(TAG, "getInstance");
    if(mInstance == null) {
      mInstance = new PackClient();
    }
//    if(mDrawableMap == null) {
//      mDrawableMap = new HashMap<String, Drawable>();
//    }
    if(mBitmapMap == null) {
      mBitmapMap = new HashMap<String, Bitmap>();
    }
    return mInstance;
  }

  /**
   * Get all of the packs available on the server
   * @return a LinkedList of Packs representing the pack that is available
   * @throws IOException if the request to the server fails
   * @throws URISyntaxException if the uri is malformed
   * @throws JSONException if the JSON is invalid
   */
  public LinkedList<Pack> getServerPacks() throws IOException, URISyntaxException, JSONException {
    Log.d(TAG, "getServerPacks");
    StringBuilder in = null;
    LinkedList<Pack> ret = null;
    in = doHTTPGet(URL_BASE+PACK_LIST_URL);
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
   * Retrieve an image from a provided url, in this case an icon for each purchasable
   * pack stored on our servers.
   * http://stackoverflow.com/questions/541966/android-how-do-i-do-a-lazy-load-of-images-in-listview
   * @param urlString reference to file
   * @return Bitmap icon
   */
  public Bitmap fetchIconForPack(String iconPath)  {
    final String urlString = URL_BASE + iconPath;
    try {
      InputStream is = fetch(urlString);
      Bitmap bitmap = BitmapFactory.decodeStream(is);
      if (bitmap != null) {
        mBitmapMap.put(urlString, bitmap);
      } else {
        Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
      }
      return bitmap;
    } catch (MalformedURLException e) {
        Log.e(TAG, "fetchIconForPack failed for url:" + urlString, e);
        return null;
    } catch (IOException e) {
        Log.e(TAG, "fetchIconForPack failed for url:" + urlString, e);
        return null;
    }
  }

  /**
   * Retrieve an icon within a thread
   * @param iconPath path to our icon in form of packs/icons/imgname.type
   * @param imageView to update with image
   */
  public void fetchIconOnThread(final String iconPath, final ImageView imageView) {
    final String urlString = URL_BASE + iconPath;
    
    // We've already retrieved it
    if (mBitmapMap.containsKey(urlString)) {
      imageView.setImageBitmap(mBitmapMap.get(urlString));
    }
  
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
          imageView.setImageBitmap((Bitmap) message.obj);
        }
    };
  
    Thread thread = new Thread() {
        @Override
        public void run() {
            Bitmap bitmap = fetchIconForPack(iconPath);
            Message message = handler.obtainMessage(1, bitmap);
            handler.sendMessage(message);
        }
    };
    thread.start();
  }

  /**
   * Perform a simple request for content, returning all content at once.  This
   * is being implemented for pack icon retrieval.
   * @param urlString
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  private InputStream fetch(String urlString) throws MalformedURLException, IOException {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    HttpGet request = new HttpGet(urlString);
    HttpResponse response = httpClient.execute(request);
    return response.getEntity().getContent();
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
    Log.d(TAG, "doHTTPGet(" + url + ")");
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
