
package com.diventi.mobipaper.classified;

import com.diventi.eldia.R;

import com.diventi.mobipaper.BaseActivity;
import com.diventi.mobipaper.BaseWebView;
import com.diventi.mobipaper.ScreenManager;
import com.diventi.mobipaper.ui.ToolbarProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ClassifiedActivity extends BaseActivity implements OnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG       = "ClassifiedActivity";
    private static final String TEXT_SIZE = "text.size";

    private ImageButton        mBtnBack;
    private ImageButton        mBtnRefresh;
    private ImageView          mImgLoading;
    private String             mUrl;
	  private ClassifiedWebView  mClassifiedWebView;
    private ProgressBar        mProgressBar;
	  
    private boolean            mFromUser;
    
	  private boolean            mClassifiedShown;
	  private ScreenManager      mScreenManager = new ScreenManager();

	  @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      
      mClassifiedShown = false;
      setContentView(R.layout.classified_content);
      setupViews();
      addAdView();

      try {
        mUrl = getIntent().getExtras().getString("url");
        
        if( mScreenManager.classifiedExists(mUrl) ) {
          onUrlLoaded(mUrl, true, null, ScreenManager.CLASSIFIED_PREFIX, false);
          return;
        }
        
        showLoading(true);
        loadClassified(mUrl, false, false);
        
      } catch (Exception ex) {
        showAlert("No se puede mostrar clasificado", ex);
      }
    }

	  private void showLoading(boolean show)
    {
      mImgLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
      mBtnRefresh.setVisibility(show ? View.INVISIBLE : View.VISIBLE);

      if(show) {
        mImgLoading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely));

        if(!mClassifiedShown)
          mProgressBar.setVisibility(View.VISIBLE);

      }
      else {
        mImgLoading.clearAnimation();
        mProgressBar.setVisibility(View.INVISIBLE);
      }
    }

    private void setupViews() {

      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      float text_size = prefs.getFloat(TEXT_SIZE, 1.0f);

      mProgressBar = (ProgressBar)findViewById(R.id.img_classified_loading);
      
      mWebView = (BaseWebView)findViewById(R.id.classified_webview);
      mClassifiedWebView = (ClassifiedWebView)mWebView;
      mClassifiedWebView.setTextSize(text_size);
      
      mBtnBack = (ImageButton)findViewById(R.id.btn_back_classified);
      mBtnBack.setOnClickListener(this);
      mBtnBack.setVisibility(View.INVISIBLE);
      
      mBtnRefresh = (ImageButton)findViewById(R.id.btn_classified_refresh);
      mBtnRefresh.setOnClickListener(this);
      
      mImgLoading = (ImageView)findViewById(R.id.img_classified_refresh_loading);
    }

    public void onClick(View v) {
      if( v.getId() == R.id.btn_classified_refresh )
        OnRefresh();

      if( v.getId() == R.id.btn_back_classified )
        OnBack();
    }

    
    void OnTextSizeDown() {
      ToolbarProvider.getInstance().getToolbar().resetAutoHideTimer();
      mClassifiedWebView.onTextSizeDown();
      saveTextSize();
    }
    
    void OnTextSizeUp() {
      ToolbarProvider.getInstance().getToolbar().resetAutoHideTimer();
      mClassifiedWebView.onTextSizeUp();
      saveTextSize();
    }
  
    void saveTextSize() {
      float text_size = mClassifiedWebView.getTextSize();
      //Log.e(TAG, String.format("saveTextSize: %.2f", text_size));
      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      
      Editor editor = prefs.edit();
      editor.putFloat(TEXT_SIZE, text_size);
      editor.commit();
    }
    
    @Override
    public void onBackPressed() {
      if( mClassifiedShown == false )
        return;
      
      super.onBackPressed();
    };
    
    private void OnBack() {
      onBackPressed();
    }

    private void OnRefresh() {
      loadClassified(mUrl, false, true);
    }
    
    @Override
    protected void onUrlLoading() {
      showLoading(true);
    }
    
    @Override
    protected void onUrlLoaded(String url, boolean useCache, Exception loadError, String prefix, boolean fromUser) {

      mFromUser = fromUser;
      
      showLoading(false);
      
      if(loadError == null ) {
        loadWebView(url, useCache, prefix, fromUser);
        
        //Mandamos a refrescar si vino de cache y es viejo
        if(useCache == true && isOldThanSeconds(mScreenManager.classifiedDate(url), 86400) ) {
          loadClassified(url, false, false);
        }
        
        return;
      }

      //Log.e(TAG, loadError.toString());
      
      showAlert("No se puede mostrar clasificado", loadError);
    }

    @Override
    protected void onAlertDismissed() {
      mClassifiedShown = true;
      if(!mFromUser)
        OnBack();
    }

    @Override
    protected void onWebViewLoaded(String url, boolean useCache, boolean mFromUser) {
      mBtnBack.setVisibility(View.VISIBLE);
      mClassifiedShown = true;
    }

}
