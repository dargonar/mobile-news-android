package com.diventi.mobipaper;
import com.diventi.eldia.R;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.mobipaper.menu.MenuWebView;
import com.diventi.mobipaper.ui.ActionsContentProvider;
import com.diventi.mobipaper.ui.ActionsContentView;
import com.diventi.utils.SHA1;
import com.diventi.utils.TimeDiff;
import com.google.ads.Ad;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomeActivity extends BaseActivity implements OnClickListener, SectionHandler {
    
    @SuppressWarnings("unused")
    private static final String TAG  = "HomeActivity";  
    
    private static String      MAIN_URL  = "section://main";
    private static String      MENU_LEFT = "menu://";
  
	  private ImageButton        mBtnOptions;
	  private ImageButton        mBtnRefresh;
	  private ImageView          mImgRefreshLoading;
	  private ActionsContentView mActionsView;
	  private MenuWebView        mMenuWebView;

	  private ImageView          mImgLoading;
	  private ImageView          mImgWarning;
	  private Button             mBtnRetry;
	  private TextView           mTxtMessage;
	  private View               mSplash;
	  
	  private String mCurrentSectionUrl = MAIN_URL;

	  @Override
	  protected void onSaveInstanceState (Bundle outState) {
	      super.onSaveInstanceState(outState);
	      outState.putString("current_section", mCurrentSectionUrl);
	  }
	  
	  @Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.home);
	    
	    if(savedInstanceState != null)
	    {
	      mCurrentSectionUrl = savedInstanceState.getString("current_section"); 
	      onCreateEx(mCurrentSectionUrl);
	      return;
	    }
	    
	    checkHTMLResources();
	  }
	  
	  private void checkHTMLResources() {
	    
	     Thread t = new Thread(){ 
	        public void run() {
	            
	            try {
                sleep(500);
              } catch (InterruptedException e1) {

              }
	            
	            htmlResourcesError = null;
              try {
                
                ResourceManager resourceManager = new ResourceManager();
                if(!resourceManager.foldersExists())
                  resourceManager.copyResources();

                
                
                DiskCache cache = DiskCache.getInstance();
                double cacheSize = cache.size();
                //Log.e(TAG, String.format("cache size pre: %.2f Mb", cacheSize));
                if(cacheSize > cache.maxSize()) {
                  cache.purge();
                }
                //Log.e(TAG, String.format("cache size post: %.2f Mb", cacheSize));

              } catch (IOException e) {
                htmlResourcesError = e;
              }
	          
              runOnUiThread( new Runnable() {
                
                @Override
                public void run() {
                  onCreateEx();
                }
              });
	        }
	      };

	      t.start();

    }
    
	  Exception htmlResourcesError = null;
	  public void onCreateEx() {
	    onCreateEx(MAIN_URL);
	  }
	  
	  public void onCreateEx(String url) {

	    setupViews();
	    addAdView();

          if(htmlResourcesError != null) {
              showSplashError(false, htmlResourcesError);
              return;
          }
          if( mScreenManager.sectionExists(url) ) {
              onUrlLoaded(url, true, null, ScreenManager.SECTION_PREFIX, false);
              return;
          }

	    loadSection(url, false, false);
    }

	  @Override
	  protected void onAlertDismissed() {
      showLoading(false);
    }

	  @Override
	  protected void onUrlLoaded(String url, boolean useCache, Exception loadError, String prefix, boolean fromUser) {

          if(loadError == null ) {

            loadWebView(url, useCache, prefix);

            if(isSplashShowing())
              hideSplash();
            else
              showLoading(false);

            return;
          }

          //Log.e(TAG, loadError.toString());

          if(isSplashShowing())
            showSplashError(false, loadError);
          else {
            if( fromUser )
              showAlert("No se pueden mostrar noticias", loadError);

            showLoading(false);
          }
	  }

    @Override
    protected void onUrlLoading() {
      if(isSplashShowing())
        showSplashError(true, null);
      else
        showLoading(true);
    }
    
    @Override
    protected void onWebViewLoaded(String url, boolean useCache) {
      
      //mostrar de cuando es la actualizacion
      if( url.startsWith("section://")   ) 
      {
        long section_date = mScreenManager.sectionDate(url);
        
        if( url == MAIN_URL )
          loadMenu();

        String js = String.format("javascript:setTimeout(function(){show_actualizado('%s')},1000)", TimeDiff.timeAgo(section_date));
        mWebView.loadUrl(js);
        
        if ( TimeDiff.minutesSince(section_date) > 15 )
        {
            Log.i(TAG, "Hace mas de 15 mins .. refreshing");
            loadSection(url, false, false);
        }
      }
    }

    private boolean isSplashShowing() {
      return mSplash.getVisibility() == View.VISIBLE;
    }

    private void hideSplash() {
      mSplash.setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void onBackPressed() {
      
      if(mActionsView != null && mActionsView.isActionsShown()) {
        mActionsView.showContent();
        return;
      }
      
      super.onBackPressed();
    }

    private void loadMenu() {

      DiskCache cache = DiskCache.getInstance();
      String key = SHA1.sha1(MENU_LEFT);
      File f = new File(cache.getFolder(),key + "." + ScreenManager.MENU_PREFIX);
      mMenuWebView.loadUrl("file://" + f.getAbsolutePath());
      
      mBtnOptions.setEnabled(true);
    }
    
    private void setupViews() {
      
      mSplash     = findViewById(R.id.splash);
      mImgLoading = (ImageView)findViewById(R.id.img_splash_loading);
      
      mImgWarning = (ImageView)findViewById(R.id.img_splash_warning);
      
      mBtnRetry   = (Button)findViewById(R.id.btn_splash_retry);
      mBtnRetry.setOnClickListener(this);
      
      mTxtMessage = (TextView)findViewById(R.id.txt_splash_msg);

      //------------------------------------------------------------
      
      mBtnOptions = (ImageButton)findViewById(R.id.btn_options);
      mBtnOptions.setOnClickListener(this);
      
      mBtnRefresh = (ImageButton)findViewById(R.id.btn_refresh);
      mBtnRefresh.setOnClickListener(this);
      
      mActionsView = (ActionsContentView) findViewById(R.id.content);
      if(mActionsView != null)
        mActionsView.setSwipingEnabled(false);

      mImgRefreshLoading = (ImageView)findViewById(R.id.img_refresh_loading);

      mMenuWebView = (MenuWebView)findViewById(R.id.menu_webview);
      mMenuWebView.setSectionHandler(this);

      mWebView = (BaseWebView)findViewById(R.id.feed_webview);

      ActionsContentProvider.getInstance().setActionView(mActionsView);
      enableRotation(this);
    }

    public void onClick(View v) {
      if( v.getId() == R.id.btn_options )
        OnOptions();

      if( v.getId() == R.id.btn_refresh )
        OnRefresh();
      
      if( v.getId() == R.id.btn_splash_retry )
        OnRetry();
    }

    private void OnRetry() {
      loadSection(MAIN_URL, false, true);
    }

    private void showSplashError(boolean showLoading, Exception ex) {

      mImgWarning.setVisibility(showLoading ? View.INVISIBLE : View.VISIBLE);
      mBtnRetry.setVisibility(showLoading ? View.INVISIBLE : View.VISIBLE);
      mTxtMessage.setVisibility(showLoading ? View.INVISIBLE : View.VISIBLE);
      
      mImgLoading.setVisibility(showLoading ? View.VISIBLE : View.INVISIBLE);
      
      if(showLoading)
        mImgLoading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely));
      else
        mImgLoading.clearAnimation();
      
      if( showLoading == false ) {
        mTxtMessage.setText(tranlsateMessage(ex));
      }
      
    }

    private void OnRefresh() {
        mBtnOptions.setEnabled(false);
        loadSection(mCurrentSectionUrl, false, true);
    }
    
    private void OnOptions() {

      if (mActionsView.isActionsShown())
        mActionsView.showContent();
      else
        mActionsView.showActions();
    }

    private void showLoading(boolean show)
    {
      mImgRefreshLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
      mBtnRefresh.setVisibility(show ? View.INVISIBLE : View.VISIBLE);

      if(show)
        mImgRefreshLoading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely));
      else
        mImgRefreshLoading.clearAnimation();
    }

    @Override
    public void onShowSection(String url) {
      mActionsView.showContent();
      mCurrentSectionUrl =  url;
      loadSection(mCurrentSectionUrl, true, true);
    }
    
    @Override
    public void onShowPage(String url) {
//      mActionsView.showContent();
//      mCurrentSectionUrl =  url;
//
//      File file = mScreenManager.getPage(url);
//      String baseUrl = String.format("file://%s", file.getAbsolutePath());
//
//      mWebView.loadUrl( baseUrl );
    }
    
}
