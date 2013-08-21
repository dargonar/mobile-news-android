package com.diventi.mobipaper;

import java.io.File;

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
