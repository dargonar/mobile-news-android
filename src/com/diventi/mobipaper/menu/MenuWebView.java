package com.diventi.mobipaper.menu;

import com.diventi.mobipaper.BaseWebView;
import com.diventi.mobipaper.SectionHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class MenuWebView extends BaseWebView {

  //private Context mContext;
  
  public MenuWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }
  
  public MenuWebView(Context context, AttributeSet attrs, int paramInt)
  {   
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
    
    //mContext = context;
  }
  
  public boolean shouldOverrideUrlLoading(WebView view, String url) 
  {
    if( url.startsWith("section://") || url.startsWith("clasificados://") || 
        url.startsWith("funebres://") || url.startsWith("farmacia://") || 
        url.startsWith("cartelera://") )
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
  
  private SectionHandler mSectionHandler;
  public void setSectionHandler(SectionHandler handler) {
    mSectionHandler = handler;
  }
  
}
