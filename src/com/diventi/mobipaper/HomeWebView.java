package com.diventi.mobipaper;

import com.diventi.mobipaper.article.ArticleActivity;
import com.diventi.mobipaper.classified.ClassifiedActivity;
import com.diventi.mobipaper.ui.ActionsContentProvider;
import com.diventi.mobipaper.ui.ActionsContentView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HomeWebView extends BaseWebView
{
  
  private Context mContext;
  
  public HomeWebView(Context context) {
    super(context);
  }

  public HomeWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }
  
  @SuppressLint("SetJavaScriptEnabled")
  public HomeWebView(Context context, AttributeSet attrs, int paramInt)
  {   
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
    
    mContext = context;
    
    setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          ActionsContentView tmp = ActionsContentProvider.getInstance().getActionView();
          if(tmp == null)
            return false;

          return tmp.isActionsShown();
        }
    });
  }
  
  public boolean shouldOverrideUrlLoading(WebView view, String url) 
  {
    if( url.startsWith("noticia://") )
    {
      Intent articleIntent = new Intent(mContext, ArticleActivity.class);
      articleIntent.putExtra("url", url);
      mContext.startActivity(articleIntent);
      return true;
    }
    
    if( url.startsWith("clasificados://") )
    {
      Intent classifiedIntent = new Intent(mContext, ClassifiedActivity.class);
      classifiedIntent.putExtra("url", url);
      mContext.startActivity(classifiedIntent);
      return true;
    }
    
    return false;
  }
  
}