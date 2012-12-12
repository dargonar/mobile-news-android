package com.diventi.utils;

import com.diventi.mobipaper.MobiPaperApp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

  public static boolean hasConnection() {
    ConnectivityManager connManager = (ConnectivityManager)MobiPaperApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    
    if(connManager == null)
      return false;
    
    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if (!networkIsAvailable(wifi) && !networkIsAvailable(mobile)) {
        return false;
    }
    
    return true;
  }
  
  private static boolean networkIsAvailable(NetworkInfo info) {

    if(info != null)
      return info.isAvailable();
    
    return false;
  }
   
  public static String connectionType() {

    ConnectivityManager connManager = (ConnectivityManager)MobiPaperApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    
    if(connManager == null)
      return "nones1";
    
    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    if(networkIsAvailable(wifi))
      return "wifi";

    NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    if(networkIsAvailable(mobile))
      return "mobile";

    return "nones2";
  }
  
}
