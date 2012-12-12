package com.diventi.mobipaper.classified;

import com.diventi.mobipaper.ui.ToolbarProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class ClassifiedWebView extends WebView
{
  private static final String TAG       = "ArticleWebView";

  private static GestureDetector mGestureDetector;
  private Context mContext;
  private float mTextSize = 1.0f;
  
  private WebViewClient mWebViewClient = new WebViewClient()
  {
    public void onLoadResource(WebView view, String url) {
      
    }
    
    public boolean shouldOverrideUrlLoading(WebView view, String url) 
    {
      if(url.startsWith("tel:")) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        mContext.startActivity(callIntent);
        return true;
      }
      
      if( url.startsWith("http://") || url.startsWith("https://") ) {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(urlIntent);
        return true;
      }
      
      return false;
    }

    public void onPageFinished(WebView view, String url)
    {
      updateTextSize();
    }

    public void onPageStarted(WebView paramWebView, String paramString, Bitmap paramBitmap)
    {
      
    }

    public void onReceivedError(WebView paramWebView, int paramInt, String paramString1, String paramString2)
    {
      
    }
    
  };
 
  public void updateTextSize() {
    String js_text = String.format("javascript:text_size('%.2f', '%.2f')", mTextSize, mTextSize+0.2f);
    loadUrl(js_text);
  }
  
  
  public ClassifiedWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public ClassifiedWebView(Context context, AttributeSet attrs, int paramInt)
  {
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
    
    mContext = context;
    
    getGestureDetector(getContext());
    getSettings().setJavaScriptEnabled(true);

    //initJavascriptInterface();

    setInitialScale(100);
    setWebViewClient(this.mWebViewClient);
  }
  
  void onTextSizeUp() {
    mTextSize = (mTextSize < 2.6f) ? mTextSize +0.05f : mTextSize;
    updateTextSize();
  }

  void onTextSizeDown() {
    mTextSize = (mTextSize >= 1.0f) ? mTextSize -0.05f : mTextSize;
    updateTextSize();
  }
  
  private GestureDetector getGestureDetector(Context paramContext)
  {
    if (mGestureDetector == null)
    {
      mGestureDetector = new GestureDetector(paramContext, new GestureDetector.SimpleOnGestureListener()
      {
        public boolean onDoubleTap(MotionEvent paramMotionEvent)
        {
          return false;
        }

        public boolean onDoubleTapEvent(MotionEvent paramMotionEvent)
        {
          return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent paramMotionEvent)
        {
          if (ToolbarProvider.getInstance().getToolbar() != null)
            ToolbarProvider.getInstance().getToolbar().onTap();
          return false;
        }
      });
    }
    
    return mGestureDetector;
  }

  public void loadContent(String paramString, int paramInt)
  {

  }

  /*
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    mGestureDetector.onTouchEvent(paramMotionEvent);
    return super.onTouchEvent(paramMotionEvent);
  }*/

  public void setTextSize(float size) {
    mTextSize = size;
  }
  
  public float getTextSize() {
    return mTextSize;
  }
  
}

/* Location:           /Users/matias/Downloads/bbc-app/bbc/out_dex2jar.jar
 * Qualified Name:     bbc.mobile.news.view.ArticleWebView
 * JD-Core Version:    0.6.0
 */