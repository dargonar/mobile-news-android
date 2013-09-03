package com.diventi.mobipaper;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;
import com.diventi.mobipaper.cache.DiskCache;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

public class MobiPaperApp extends Application {

  public static final String mBugsenseApiKey = "8ca8f59d"; //The-mobi-paper
  public static final double MAX_CACHE_SIZE_MB = 15.0;
  
  private static Context mContext;  
  private static DiskCache mDiskCache;
  private static boolean mYoutubeInstalled;
  private static String mAppId;
  private static String mMediaVersion;
  private static String   mAdMob = "";
  private static String[] mGoogleAnalytics = new String[]{""};
  
  @Override
  public void onCreate() {
    super.onCreate();

    //bugsense
    BugSenseHandler.initAndStartSession(this, MobiPaperApp.mBugsenseApiKey);

    mContext = getApplicationContext();
    File cacheDir = mContext.getCacheDir();
    
    mDiskCache = DiskCache.getInstance();
    mDiskCache.configure( cacheDir, MAX_CACHE_SIZE_MB );
    
    mYoutubeInstalled = isAppInstalled("com.google.android.youtube");
    
    mAppId = getApplicationInfo().packageName;
    mMediaVersion = mDiskCache.getMediaVersion();
    
    loadConfigJson();
  }
  
  public static void loadConfigJson() {
    try {

      DiskCache cache = DiskCache.getInstance();
      byte[] data = cache.get("config","json");
      
      if(data == null)
        return;
      
      JSONObject obj = new JSONObject(new String(data));
      mAdMob = ((JSONObject)obj.get("android")).getString("ad_mob");
      
      JSONArray arr = ((JSONObject)obj.get("android")).getJSONArray("google_analytics");
      
      mGoogleAnalytics = new String[arr.length()];
      for(int i=0; i<arr.length(); i++)
      {
        mGoogleAnalytics[i] = arr.getString(i);
      }
      
    } catch (JSONException e) {
      
    }

  }
  
  public static String getAdmob() {
    return mAdMob;
  }

  public static String[] getGoogleAnalytics() {
    return mGoogleAnalytics;
  }
  
  public static DiskCache getCache() {
    return mDiskCache;
  }
  
  public static Context getContext() {
    return mContext;
  }

  public static boolean isYoutubeInstalled() {
    return mYoutubeInstalled;
  }
  
  public static String getAppId() {
    return mAppId;
  }
  
  public static String getMediaVersion() {
    return mMediaVersion;
  }
  
  private boolean isAppInstalled(String uri) {
    PackageManager pm = getPackageManager();
    boolean installed = false;
    try {
       pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
       installed = true;
    } catch (PackageManager.NameNotFoundException e) {
       installed = false;
    }
    return installed;
 }
}
