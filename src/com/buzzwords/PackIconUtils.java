package com.buzzwords;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
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
  public static int LDPI_ICON_SIDE_PX = 16; 
  public static int MDPI_ICON_SIDE_PX = 32; 
  public static int HDPI_ICON_SIDE_PX = 64; 
  public static int XHDPI_ICON_SIDE_PX = 96; 
  
  public static String buildIconPath(String iconName, Context context) {
    return context.getDir(ICON_DIRECTORY, Context.MODE_WORLD_READABLE).getPath() + "/" + iconName + ".png";
  }
  
  public static BitmapDrawable getPackIconFilev2(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    InputStream in = null;
    try {
      in = new FileInputStream(path);
      Bitmap bitmap = BitmapFactory.decodeStream(in);
      if (bitmap == null) {
        Log.e(TAG, "Cached bitmap " + path + " decoded as null.");
        return null;
      }
      
      Log.v(TAG, "width: " + bitmap.getHeight());
      Log.v(TAG, "height: " + bitmap.getWidth());
      Log.v(TAG, "densityBefore: " + bitmap.getDensity());
      Log.v(TAG, "DENSITY: " + context.getResources().getDisplayMetrics().densityDpi);
      
      switch (context.getResources().getDisplayMetrics().densityDpi) {
      case DisplayMetrics.DENSITY_LOW:
        bitmap.setDensity(DisplayMetrics.DENSITY_LOW);
        bitmap = Bitmap.createScaledBitmap(bitmap, LDPI_ICON_SIDE_PX, LDPI_ICON_SIDE_PX, true);
        break;
      case DisplayMetrics.DENSITY_MEDIUM:
        bitmap.setDensity(DisplayMetrics.DENSITY_MEDIUM);
        bitmap = Bitmap.createScaledBitmap(bitmap, MDPI_ICON_SIDE_PX, MDPI_ICON_SIDE_PX, true);
        break;
      case DisplayMetrics.DENSITY_HIGH:
        bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
        bitmap = Bitmap.createScaledBitmap(bitmap, HDPI_ICON_SIDE_PX, HDPI_ICON_SIDE_PX, true);
        break;
      default:
        bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
        bitmap = Bitmap.createScaledBitmap(bitmap, XHDPI_ICON_SIDE_PX, XHDPI_ICON_SIDE_PX, true);
        break;
      }
      
      Log.v(TAG, "widthAfter: " + bitmap.getHeight());
      Log.v(TAG, "heightAfter: " + bitmap.getWidth());
      Log.v(TAG, "densityAfter: " + bitmap.getDensity());
      
      return new BitmapDrawable(bitmap);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Could not find cached bitmap file " + path);
      e.printStackTrace();
      return null;
    } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            Log.e(TAG, "Encountred IOException while closing cache file input stream for " + path);
            e.printStackTrace();
          }
        }
    }
  }
  
  public static boolean packIconCached(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    File iconFile = new File(path);
    return iconFile.exists();
  }
   
  // Update pack icon
  
  // Delete pack icon
  public static void deleteIcon(String iconName, Context context) {
    String path = buildIconPath(iconName, context);
    new File(path).delete();
  }

  
  public static boolean storeIcon(String iconName, Drawable drawableIcon, Context context) {
      String path = context.getDir(ICON_DIRECTORY, Context.MODE_WORLD_READABLE).getPath() + "/" + iconName + ".png";
      File outFile = new File(path);
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(outFile);
        Bitmap iconBitmap = ((BitmapDrawable) drawableIcon).getBitmap();
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
          out.close();
        } catch (IOException e) {
          Log.e(TAG, "Encountered I/O error storing icon " + outFile.getAbsolutePath());
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        Log.e(TAG, "Encountered File not found exception: " + outFile.getAbsolutePath());
        e.printStackTrace();
      }
      return true;
  }
  
}
