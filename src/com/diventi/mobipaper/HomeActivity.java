package com.diventi.mobipaper;
import com.diventi.eldia.R;

import java.io.File;
import java.io.IOException;

import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.mobipaper.menu.MenuWebView;
import com.diventi.mobipaper.ui.ActionsContentProvider;
import com.diventi.mobipaper.ui.ActionsContentView;
import com.diventi.utils.SHA1;
import com.diventi.utils.TimeDiff;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends BaseActivity implements OnClickListener, SectionHandler {
    
    private static final String TAG  = "HomeActivity";  
    
    private static String      MAIN_URL  = "section://main";
    private static String      MENU_LEFT = "menu://left";
  
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
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.home);
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
                //Log.d(TAG, String.format("cache size pre: %.2f Mb", cacheSize));
                if(cacheSize > cache.maxSize()) {
                  cache.purge();
                }
                //Log.d(TAG, String.format("cache size post: %.2f Mb", cacheSize));

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

	    setupViews();
	    
	    if(htmlResourcesError != null) {
	      showSplashError(false, htmlResourcesError);
	      return;
	    }
	    
	    if( mScreenManager.sectionExists(MAIN_URL) ) {
	      onUrlLoaded(MAIN_URL, true, null, ScreenManager.SECTION_PREFIX, false);
	      return;
	    }
      
	    loadSection(MAIN_URL, false, false);
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
        
        //Mandamos a refrescar si vino de cache y es viejo
        if(useCache == true && isOldThanSeconds(mScreenManager.sectionDate(url), 60*15) ) {
          loadSection(url, false, fromUser);
        }
        
        return;
      }

      //Log.d(TAG, loadError.toString());
      
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
      long section_date = mScreenManager.sectionDate(url);
      String js = String.format("javascript:show_actualizado('%s')", TimeDiff.timeAgo(section_date));
      mWebView.loadUrl(js);
      
      //Esta cargando la main desde red (refrescar menu)
      if( url != MAIN_URL )
        return;
      
      reloadMenu(useCache);
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
     
    private void reloadMenu(boolean useCache) {
      
      if(useCache == true) {
        loadMenu();
        return;
      }
      
      mBtnOptions.setEnabled(false);
      
      Thread t = new Thread() {
        public void run() {
          try {
            mScreenManager.getMenu(false);
          } catch (Exception e) {
            //Log.e(TAG, e.toString());
          }
          
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              loadMenu();
            }
          });
        }
      };
      t.run();
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
      mActionsView.setSwipingEnabled(false);

      mImgRefreshLoading = (ImageView)findViewById(R.id.img_refresh_loading);

      mMenuWebView = (MenuWebView)findViewById(R.id.menu_webview);
      mMenuWebView.setSectionHandler(this);

      mWebView = (WebView)findViewById(R.id.feed_webview);
      
      ActionsContentProvider.getInstance().setActionView(mActionsView);
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
      
      if(mCurrentSectionUrl.startsWith("page://"))
        onShowPage(mCurrentSectionUrl);
      else
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
      mActionsView.showContent();
      mCurrentSectionUrl =  url;

      File file = mScreenManager.getPage(url);
      String baseUrl = String.format("file://%s", file.getAbsolutePath());
      mWebView.loadUrl( baseUrl );
    }

}
