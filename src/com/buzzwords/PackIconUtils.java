package com.buzzwords;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
public class PackIconUtils {

  public static String TAG = "PackIconsUtils";
  public static String ICON_DIRECTORY = "icons";
  public static int ICON_DP = 32;

  /**
   * Get a Bitmap icon from the internal storage which serves as our cache.
   * @param iconName to retrieve from cache
   * @param context of application
   * @return Bitmap of icon, null if error occurs
   */
  public static Bitmap getCachedIcon(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    InputStream in = null;
    Bitmap bitmap = null;
    try {
      in = new FileInputStream(path);
      bitmap = BitmapFactory.decodeStream(in);
      if (bitmap == null) {
        Log.e(TAG, "Cached bitmap " + path + " decoded as null.");
      }
      if (in != null) {
        in.close();
      }
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Could not find cached bitmap file " + path);
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(TAG, "IOException while closing cache file input stream for " + path);
      e.printStackTrace();
    }
    return bitmap;
  }
  
  /**
   * Return whether or not a pack icon is cached.
   * @param iconName of icon to search for
   * @param context of application
   * @return true if icon found, false otherwise
   */
  public static boolean isPackIconCached(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    File iconFile = new File(path);
    return iconFile.exists();
  }
  
  /**
   * Delete the pack icon from internal storage.
   * @param iconName name of icon to delete
   * @param context of application
   */
  public static boolean deleteIcon(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    boolean deleted = new File(path).delete();
    if (!deleted) {
      Log.w(TAG, "Error deleting icon: " + path);
    }
    return deleted;
  }

  
  /**
   * Store the icon png in Internal Storage according to iconName.png format.  The
   * icon should be world readable, but only for transparency's sake.  It should be deleted
   * when a pack is uninstalled, and will certainly when the app is uninstalled.
   * @param iconName name of icon to store
   * @param iconBitmap image to store
   * @param context of application
   * @return true if stored successfully, false for IO or FileNotFound exception
   */
  public static boolean storeIcon(String iconName, Bitmap iconBitmap, Context context) {
      String path = buildIconPath(iconName, context);
      File outFile = new File(path);
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(outFile);
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
        return true;
      } catch (FileNotFoundException e) {
        Log.e(TAG, "Encountered File not found exception: " + outFile.getAbsolutePath());
        e.printStackTrace();
        return false;
      } catch (IOException e) {
        Log.e(TAG, "Encountered I/O error storing icon " + outFile.getAbsolutePath());
        e.printStackTrace();
        return false;
      }
  }
  
  /**
   * Scale a bitmap to the correct size for a given device's density.  This 
   * ensures all of our icons can be the same size regardless of where the
   * bitmap comes from.
   * @param icon to scale
   * @param context of application
   * @return new Bitmap with height and width suitable for display density
   */
  public static Bitmap scaleIconForDensity(Bitmap bitmap, Context context) {
    Log.d(TAG, "Scaling image...");
    Log.d(TAG, "width: " + bitmap.getWidth());
    Log.d(TAG, "height: " + bitmap.getHeight());
    Log.d(TAG, "densityBefore: " + bitmap.getDensity());
    
    final float density = context.getResources().getDisplayMetrics().density;
    int p = (int) (ICON_DP * density + 0.5f);
    bitmap = Bitmap.createScaledBitmap(bitmap, p, p, true);
    
    Log.d(TAG, "widthAfter: " + bitmap.getWidth());
    Log.d(TAG, "heightAfter: " + bitmap.getHeight());
    Log.d(TAG, "densityAfter: " + bitmap.getDensity());
    
    return bitmap;
  }
  
  /**
   * Helper method to build the path to location in internal storage where icon
   * pngs are stored.
   * @param iconName to build a path for
   * @param context of application
   * @return path to icon file
   */
  public static String buildIconPath(String iconName, Context context) {
    return context.getDir(ICON_DIRECTORY, Context.MODE_WORLD_READABLE).getPath() + "/" 
                           + iconName + ".png";
  }
}
