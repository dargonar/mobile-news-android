package com.diventi.mobipaper;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;
import com.diventi.mobipaper.cache.DiskCache;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.Tracker;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class MobiPaperApp extends Application {

  public static final String TAG = "MobiPaperApp";
  
  public static final String mBugsenseApiKey = "8ca8f59d"; //The-mobi-paper
  public static final double MAX_CACHE_SIZE_MB = 15.0;
  
  private static Context mContext;  
  private static DiskCache mDiskCache;
  private static boolean mYoutubeInstalled;
  private static String mAppId;
  private static String mMediaVersion;
  private static String   mAdMob = "";
  private static Tracker[] mTrackers = null;

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
    
    String tmp = getApplicationInfo().packageName;
    if(tmp.endsWith("2"))
      tmp = tmp.substring(0,tmp.length()-1);
    
    mAppId = tmp;
    mMediaVersion = mDiskCache.getMediaVersion();
    
    loadConfigJson();
    
    GoogleAnalytics.getInstance(mContext).getLogger().setLogLevel(LogLevel.VERBOSE);
    
//    UncaughtExceptionHandler myHandler = new ExceptionReporter(
//        GoogleAnalytics.getInstance(this).getDefaultTracker(), // Tracker, may return null if not yet initialized.
//        GAServiceManager.getInstance(),                        // GAServiceManager singleton.
//        Thread.getDefaultUncaughtExceptionHandler());          // Current default uncaught exception handler.
//
//    // Make myHandler the new default uncaught exception handler.
//    Thread.setDefaultUncaughtExceptionHandler(myHandler);  
  }

  public static Tracker[] getTracker() {
      return mTrackers;
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

//      mTrackers = null;
//      if(arr != null && arr.length() != 0) {
//        mTrackers = new Tracker[arr.length()];
//        for(int i=0; i<arr.length(); i++) {
//          Log.i(TAG, "Adding tracker =>" + arr.getString(i));
//          mTrackers[i] = GoogleAnalytics.getInstance(mContext).getTracker(arr.getString(i));
//          mTrackers[i].set(Fields.SESSION_CONTROL, "start");
//        }
//      }
      
      mTrackers = new Tracker[] { GoogleAnalytics.getInstance(mContext).getTracker("UA-32663760-6") };
      
      
    } catch (JSONException e) {
      
    }

  }
  
  public static String getAdmob() {
    return mAdMob;
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
