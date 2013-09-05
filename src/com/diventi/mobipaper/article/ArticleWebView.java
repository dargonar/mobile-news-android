package com.diventi.mobipaper.article;

import com.diventi.mobipaper.BaseWebView;
import com.diventi.mobipaper.MobiPaperApp;
import com.diventi.mobipaper.gallery.GalleryActivity;
import com.diventi.mobipaper.ui.ToolbarProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleWebView extends BaseWebView
{
  @SuppressWarnings("unused")
  private static final String TAG       = "ArticleWebView";

  private static GestureDetector mGestureDetector;
  private Context mContext;
  private float mTextSize = 1.0f;

  public void updateTextSize() {
    String js_text = String.format("javascript:text_size('%.2fem', '%.2fem')", mTextSize, mTextSize+0.2f);
    loadUrl(js_text);
  }
  
  public ArticleWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public ArticleWebView(Context context, AttributeSet attrs, int paramInt)
  {
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
    
    mContext = context;
    
    getGestureDetector(getContext());
  }
  
  void onTextSizeUp() {
    mTextSize = (mTextSize < 2.6f) ? mTextSize +0.05f : mTextSize;
    updateTextSize();
  }

  void onTextSizeDown() {
    mTextSize = (mTextSize >= 1.0f) ? mTextSize -0.05f : mTextSize;
    updateTextSize();
  }
  
  public void onPageFinished(WebView view, String url)
  {
    super.onPageFinished(view, url);
    updateTextSize();
  }
  
  public boolean shouldOverrideUrlLoading(WebView view, String url) 
  {
    //HACK: por que cuando viene el evento shouldOverrideUrlLoading y tenes un video://http:// lo cambia a video://http// 
    url = url.replace("http//", "http://");

    if( url.startsWith("noticia://") )
    {
      Intent articleIntent = new Intent(mContext, ArticleActivity.class);
      articleIntent.putExtra("url", url);
      mContext.startActivity(articleIntent);
      return true;
    }
    
    if( url.startsWith("video://"))
    {
      Intent youtubeIntent;
      if( MobiPaperApp.isYoutubeInstalled() )
        youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + url.substring(url.indexOf("v=")+2)));
      else {
        youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.substring(8)));
      }
     
      mContext.startActivity(youtubeIntent);
      return true;
    }

    if( url.startsWith("audio://"))
    {
      Intent audioIntent = new Intent(android.content.Intent.ACTION_VIEW);   
      audioIntent.setDataAndType(Uri.parse(url.substring(8)), "audio/*");   
      audioIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      mContext.startActivity(audioIntent); 
      return true;
    }

    if( url.startsWith("galeria://") )
    {
      String[] urls = url.substring(10).split(";");        
      Intent galleryIntent = new Intent(mContext, GalleryActivity.class);
      galleryIntent.putExtra("urls", urls);
      mContext.startActivity(galleryIntent);
      return true;
    }
    
    return false;
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
          //Log.e(TAG, "Aca va un tap");
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

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    mGestureDetector.onTouchEvent(paramMotionEvent);
    return super.onTouchEvent(paramMotionEvent);
  }

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