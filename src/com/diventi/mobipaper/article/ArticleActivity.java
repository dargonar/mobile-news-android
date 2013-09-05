package com.diventi.mobipaper.article;

import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.diventi.eldia.R;
import com.diventi.mobipaper.BaseActivity;
import com.diventi.mobipaper.BaseWebView;
import com.diventi.mobipaper.ScreenManager;
import com.diventi.mobipaper.ui.ToolbarProvider;
import com.diventi.mobipaper.ui.ToolbarView;

public class ArticleActivity extends BaseActivity implements OnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG       = "ArticleActivity";
    private static final String TEXT_SIZE = "text.size";

    private ImageButton        mBtnBack;
    private ImageButton        mBtnShare;
    private ImageButton        mBtnIncreaseText;
    private ImageButton        mBtnDecreaseText;
	  private String             mUrl;
	  private String             mRemoteUrl;
	  private String             mTitle;
	  private String             mHeader;
	  private ArticleWebView     mArticleWebView;
    private ProgressBar        mProgressBar;
	  
	  private boolean            mArticleShown;
	  private ScreenManager      mScreenManager = new ScreenManager();
	  
	  @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      
      mArticleShown = false;
      setContentView(R.layout.article_content);
      setupViews();
      addAdView();

      try {
        String tmp0 = getIntent().getExtras().getString("url");
        tmp0 = tmp0.replace("%0A", "").replace("%0D", "");

        NumericEntityUnescaper escaper = new NumericEntityUnescaper();
        String tmp1 = Uri.decode(tmp0);
        String tmp2 = escaper.translate(tmp1);
        
        Uri uri = Uri.parse(tmp2);

        mUrl       = String.format("%s://%s",uri.getScheme(), uri.getHost());
        mRemoteUrl = uri.getQueryParameter("url");
        mTitle     = uri.getQueryParameter("title");
        mHeader    = uri.getQueryParameter("header");
        
        if( mScreenManager.articleExists(mUrl) ) {
          onUrlLoaded(mUrl, true, null, ScreenManager.ARTICLE_PREFIX, false);
          return;
        }
        
        showLoading(true);
        loadArticle(mUrl, false);
        
      } catch (Exception ex) {
        //TODO: mostrar alert
        showAlert("No se puede mostrar noticia", ex);
      }
    }

    private void showLoading(boolean show)
    {
      mProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }


    private void setupViews() {

      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      float text_size = prefs.getFloat(TEXT_SIZE, 1.0f);

      mProgressBar = (ProgressBar)findViewById(R.id.img_article_loading);
      
      mWebView = (BaseWebView)findViewById(R.id.article_webview);
      mArticleWebView = (ArticleWebView)mWebView;
      mArticleWebView.setTextSize(text_size);
      
      mBtnBack = (ImageButton)findViewById(R.id.btn_back_article);
      mBtnBack.setOnClickListener(this);
      mBtnBack.setVisibility(View.INVISIBLE);
      
      mBtnShare = (ImageButton)findViewById(R.id.btn_share_article);
      mBtnShare.setOnClickListener(this);
      
      mBtnIncreaseText = (ImageButton)findViewById(R.id.btn_options_aplus);
      mBtnIncreaseText.setOnClickListener(this);
      
      mBtnDecreaseText = (ImageButton)findViewById(R.id.btn_options_aless);
      mBtnDecreaseText.setOnClickListener(this);
      
      final ToolbarView toolbarView = (ToolbarView)findViewById(R.id.toolbar);
      ToolbarProvider.getInstance().setToolbar(toolbarView);
    }

    public void onClick(View v) {
      if( v.getId() == R.id.btn_back_article )
        OnBack();
      
      if( v.getId() == R.id.btn_share_article )
        OnShare();

      if( v.getId() == R.id.btn_options_aplus )
        OnTextSizeUp();

      if( v.getId() == R.id.btn_options_aless )
        OnTextSizeDown();
    }

    
    void OnTextSizeDown() {
      ToolbarProvider.getInstance().getToolbar().resetAutoHideTimer();
      mArticleWebView.onTextSizeDown();
      saveTextSize();
    }
    
    void OnTextSizeUp() {
      ToolbarProvider.getInstance().getToolbar().resetAutoHideTimer();
      mArticleWebView.onTextSizeUp();
      saveTextSize();
    }
  
    void saveTextSize() {
      float text_size = mArticleWebView.getTextSize();
      //Log.e(TAG, String.format("saveTextSize: %.2f", text_size));
      SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
      
      Editor editor = prefs.edit();
      editor.putFloat(TEXT_SIZE, text_size);
      editor.commit();
    }
    
    @Override
    public void onBackPressed() {
      if( mArticleShown == false )
        return;
      
      super.onBackPressed();
    }
    
    private void OnBack() {
      onBackPressed();
    }

    private void OnShare() {
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, mTitle + "\n\n" + mHeader + "\n\n" + mRemoteUrl);
      sendIntent.setType("text/plain");
      startActivity(Intent.createChooser(sendIntent, "Compartir nota"));      
    }

    @Override
    protected void onUrlLoaded(String url, boolean useCache, Exception loadError, String prefix, boolean fromUser) {

      showLoading(false);
      
      if(loadError == null ) {
        loadWebView(url, useCache, prefix, fromUser);
        return;
      }

      //Log.e(TAG, loadError.toString());
      
      showAlert("No se puede mostrar noticia", loadError);
    }

    @Override
    protected void onAlertDismissed() {
      mArticleShown = true;
      OnBack();
    }

    @Override
    protected void onWebViewLoaded(String url, boolean useCache, boolean fromUser) {
      mBtnBack.setVisibility(View.VISIBLE);
      mArticleShown = true;
    }

}
