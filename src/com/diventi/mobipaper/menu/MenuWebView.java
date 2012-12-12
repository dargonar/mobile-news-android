package com.diventi.mobipaper.menu;

import com.diventi.mobipaper.SectionHandler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MenuWebView extends WebView {

  private Context mContext;
  
  private WebViewClient mWebViewClient = new WebViewClient()
  {
    
    public void onLoadResource(WebView view, String url) {
      
    }
    
    public boolean shouldOverrideUrlLoading(WebView view, String url) 
    {
      if( url.startsWith("section://") )
      {
        mSectionHandler.onShowSection(url);
        return true;
      }

      if( url.startsWith("page://") )
      {
        mSectionHandler.onShowPage(url);
        return true;
      }
      
      return false;
    }
    
    public void onPageFinished(WebView view, String url)
    {
      
    }

    public void onPageStarted(WebView paramWebView, String paramString, Bitmap paramBitmap)
    {
      
    }

    public void onReceivedError(WebView paramWebView, int paramInt, String paramString1, String paramString2)
    {
      
    }
  };

  
  public MenuWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }
  
  public MenuWebView(Context context, AttributeSet attrs, int paramInt)
  {   
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
    
    mContext = context;
    setWebViewClient(this.mWebViewClient);
  }
  
  private SectionHandler mSectionHandler;
  public void setSectionHandler(SectionHandler handler) {
    mSectionHandler = handler;
  }
  
}
