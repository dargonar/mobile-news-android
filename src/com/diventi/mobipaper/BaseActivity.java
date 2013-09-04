package com.diventi.mobipaper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bugsense.trace.BugSenseHandler;
import com.diventi.eldia.R;
import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.utils.Network;
import com.diventi.utils.NoNetwork;
import com.diventi.utils.SHA1;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.google.ads.*;
import com.google.ads.AdRequest.ErrorCode;
public class BaseActivity extends Activity  {

  @SuppressWarnings("unused")
  private static final String TAG = "BaseActivity";
  
  protected BaseWebView    mWebView;
  protected ScreenManager  mScreenManager = new ScreenManager();

  protected AdView       mAdView = null;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Configuration conf = getResources().getConfiguration();
    
    mScreenManager.IsBig((conf.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
    mScreenManager.IsLandscape(conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
    
//        <com.google.ads.AdView
//        android:id="@+id/adView"
//        android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:layout_alignParentBottom="true"
//        android:layout_alignParentLeft="true"
//        android:visibility="gone"
//        ads:adSize="BANNER"
//        ads:adUnitId="PUTIDHERE"
//        ads:loadAdOnCreate="false"
//        ads:testDevices="TEST_EMULATOR, TEST_DEVICE_ID" >
//    </com.google.ads.AdView>

        //AdRequest req = new AdRequest();
        //req.set
        //mAdView.
        //mAdView
        //mAdView.adun
        
//        runOnUiThread( new Runnable() {
//          @Override
//          public void run() {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mWebView.getLayoutParams();
//            params.addRule(RelativeLayout.ABOVE, mAdView.getId());
//            mWebView.setLayoutParams(params);
//          }
//        });
//      }
//    }
  }
  
  public void addAdView() {
    
    String code = MobiPaperApp.getAdmob();
    if(code.length() == 0)
      return;
    
    RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_view);
    
    AdView adView = new AdView(this, AdSize.BANNER, code);
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    adView.setId(1001);
    layout.addView(adView, params);
    
    adView.loadAd(new AdRequest());
    
    RelativeLayout.LayoutParams wvparams = (RelativeLayout.LayoutParams)mWebView.getLayoutParams();
    wvparams.addRule(RelativeLayout.ABOVE, adView.getId());
    mWebView.setLayoutParams(wvparams);    
  }
  
  public void enableRotation(Activity activity)
  {
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }
  
  @Override
  public void onDestroy() {
    if (mAdView != null) {
      mAdView.destroy();
    }
    super.onDestroy();
  }
  
  protected void loadImages(ArrayList<String> images) {
    AsyncHttpClient client = new AsyncHttpClient();
    for(final String img : images) {
      client.get(img, new BinaryHttpResponseHandler() {
        public void onSuccess(byte[] binaryData) {
          DiskCache cache = DiskCache.getInstance();
          cache.put(SHA1.sha1(img) , binaryData, ScreenManager.IMAGE_PREFIX);
          
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              onImageLoaded(img);
            }
          });
        }
      });
    }
  }
  
  private void onImageLoaded(String image) {
    //Log.e(TAG, "onImageLoaded: " + image.localUrl);
    mWebView.loadUrl( String.format("javascript:update_image('%s')", SHA1.sha1(image)));
  }

  protected void onUrlLoading() {
    
  }

  protected void onUrlLoaded(String url, boolean useCache, Exception error, String prefix, boolean fromUser) {
    
  }
    
  protected void loadClassified(String url, boolean useCache, boolean fromUser) {
    loadUrl(url, ScreenManager.CLASSIFIED_PREFIX, useCache, false);
  }
    
  protected void loadSection(String url, boolean useCache, boolean fromUser) {
    loadUrl(url, ScreenManager.SECTION_PREFIX, useCache, true, fromUser);
  }

  protected void loadArticle(String url, boolean useCache) {
    loadUrl(url, ScreenManager.ARTICLE_PREFIX, useCache, true);
  }
  
  protected void loadUrl(final String url, final String prefix, final boolean useCache) {
    loadUrl(url, prefix, useCache, true);
  }
  
  protected void loadUrl(final String url, final String prefix, final boolean useCache, final boolean processImages) {
    loadUrl(url, prefix, useCache, processImages, false);
  }
  
  protected void loadUrl(final String url, final String prefix, final boolean useCache, final boolean processImages, final boolean fromUser) {

    onUrlLoading();

    Thread t = new Thread() {
    public void run() {
      
      Exception error = null;
        try {
          mScreenManager.getScreen(url, useCache, processImages, prefix);
        } catch (final Exception e) {
          error = e;
        }

        onUrlLoadedStub(url, useCache, error, prefix, fromUser);
      }

    };
    t.start();
  }

  private void onUrlLoadedStub(final String url, final boolean useCache, final Exception error, final String prefix, final boolean fromUser) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        onUrlLoaded(url, useCache, error, prefix, fromUser);
      }
    });
  }

  protected void showAlert(String title, Exception ex) {
    Builder builder = new AlertDialog.Builder(this);
    
    builder.setTitle(title)
           .setMessage(tranlsateMessage(ex))
           .setNeutralButton("Ok", new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                onAlertDismissed();
              }
           })
           .setCancelable(false)
           .show();
  }
  
  protected void onAlertDismissed() {
    
  }

  protected String tranlsateMessage(Exception ex) {
    
    if(ex == null) {
      return "Contenido no disponible";
    }

    //Log exception
    BugSenseHandler.sendExceptionMessage("dummy", Network.connectionType(), ex);
    
    if( ex instanceof NoNetwork )
      return "No hay conexion";
    
    //TODO: translate las otras exceptions
    
    return "Contenido no disponible";
  }

  protected void loadWebView(final String url, boolean useCache, String prefix) {
    
    DiskCache cache = DiskCache.getInstance();      
    File html = new File(cache.getFolder(), SHA1.sha1(url) + "." + prefix );
    String baseUrl = String.format("file://%s", html.getAbsolutePath());
    mWebView.loadUrl( baseUrl );
    
    try {
      final ArrayList<String> images = mScreenManager.getPendingImages(url);
      if( images.size() > 0 )
      {
        Thread t = new Thread() {
          public void run() {
            loadImages(images);
          }
        }; t.run();
      }
      
    } catch (Exception e) {

    }
    
    onWebViewLoaded(url, useCache);
  }

  protected void onWebViewLoaded(String url, boolean useCache) {

  }
  
  private static Method overridePendingTransition;
  static {
    try {
      overridePendingTransition = Activity.class.getMethod("overridePendingTransition", new Class[] {Integer.TYPE, Integer.TYPE}); //$NON-NLS-1$
    }
    catch (NoSuchMethodException e) {
      overridePendingTransition = null;
    }
  }

  /**
  * Calls Activity.overridePendingTransition if the method is available (>=Android 2.0)
  * @param activity the activity that launches another activity
  * @param animEnter the entering animation
  * @param animExit the exiting animation
  */
  public static void overridePendingTransition(Activity activity, int animEnter, int animExit) {
    if (overridePendingTransition!=null) {
      try {
      overridePendingTransition.invoke(activity, animEnter, animExit);
      } catch (Exception e) {
      // do nothing
      }
    }
  }
  
  protected boolean isOldThanSeconds(long sectionDate, long seconds) {
    //milliseconds since january 1970
    Date now = new Date();
    long diffSeconds = (now.getTime() - sectionDate)/1000;
    
    //Log.e(TAG, String.format("segundos diff: %d", diffSeconds));
    
    //5 minutes
    if( diffSeconds > seconds ) {
      return true;
    }
        
    return false;
  }

  
}
