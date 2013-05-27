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
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Siramix Labs
 * Client for communicating with the Buzzwords pack server
 */
public class PackClient {
  
  private static final String TAG = "PackClient";
  
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
    in = doHTTPGet(Config.packBaseUri + Config.packList);
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
    in = doHTTPGet(Config.packBaseUri + packPath);
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
  private static Bitmap fetchIconForPack(String iconPath)  {
    final String urlString = Config.packBaseUri + iconPath;
    try {
      InputStream is = fetch(urlString);
      Bitmap bitmap = BitmapFactory.decodeStream(is);
      if (bitmap == null) {
        Log.w(TAG, "Could not get thumbnail");
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
  public static void fetchIconOnThread(final Pack pack, final PackPurchaseRowLayout packRow, final Context context) {

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
          Bitmap packIcon = (Bitmap) message.obj;
          if (packIcon != null) {
            packIcon = PackIconUtils.scaleIconForDensity(packIcon, context);
            packRow.setAndScalePackIcon(packIcon);
            PackIconUtils.storeIcon(pack.getIconName(), packIcon, context);
          } else {
            Log.w(TAG, "Fetched icon response was null for " + pack.getIconPath());
          }
        }
    };
  
    Thread thread = new Thread() {
        @Override
        public void run() {
            Bitmap bitmap = fetchIconForPack(pack.getIconPath());
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
  private static InputStream fetch(String urlString) throws MalformedURLException, IOException {
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
