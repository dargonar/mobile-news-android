package com.diventi.mobipaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BaseWebView extends WebView {

    private static final String TAG  = "BaseWebView";

    public class MyWebViewClient extends WebViewClient {

    private BaseWebView mBase;
    public MyWebViewClient(BaseWebView base) {
      mBase = base;
    }
    
    public void onLoadResource(WebView view, String url) {
      mBase.onLoadResource(view, url);
    }
    
    public boolean shouldOverrideUrlLoading(WebView view, String url) 
    {
      return mBase.shouldOverrideUrlLoading(view, url);
    }
    
    public void onPageFinished(WebView view, String url)
    {
      mBase.onPageFinished(view, url);
    }

    public void onPageStarted(WebView paramWebView, String paramString, Bitmap paramBitmap)
    {
      
    }

    public void onReceivedError(WebView paramWebView, int paramInt, String paramString1, String paramString2)
    {
      
    }    
  }
  
  public BaseWebView(Context context) {
    super(context);
    SetParams();
  }

  public BaseWebView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
    SetParams();
  }
  
  public BaseWebView(Context context, AttributeSet attrs, int paramInt)
  {
    super(context, attrs, paramInt);
    SetParams();
  }
  
  public void onLoadResource(WebView view, String url) {
    
  }
  
  public void onPageFinished(WebView view, String url)
  {

  }
  
  //NOTA: Cuando se llama  onSizeChanged el layout esta terminado y tengo width
  // Ahora si puedo llamar a la funcion de JS que llama a android nuevamente con el width del body
  // Para luego escalar ya que tengo los dos datos. => el width del layout (android) + el width del doc
  @Override
  protected void onSizeChanged(int w, int h, int ow, int oh) {
    super.onSizeChanged(w, h, ow, oh);
    
    if(isInEditMode())
      return;
    
    if(w==0 || h ==0)
      return;
    
    TryToResize();
  };

  
  private Integer count;
  public void TryToResize()
  {
    synchronized (count) {
      count++;
      //Log.e(TAG, "===>Dentro de TryToResize " + count.toString());
      if(count==2)
      {  
        count--;
        ((Activity)getContext()).runOnUiThread( new Runnable() {
          @Override
          public void run() {
            loadUrl("javascript:resize()");    
          }
        });
        
      }
      else
      {
        //Log.e(TAG, "===> NO->CAMINA->NO");
      }
    }    
  }
  
  public boolean shouldOverrideUrlLoading(WebView view, String url) 
  {
    return false;
  }
    
  @SuppressLint("SetJavaScriptEnabled")
  public void SetParams()
  {
    if(isInEditMode())
      return;
    
    count = Integer.valueOf(0);
    
    WebSettings settings = this.getSettings();
    settings.setBuiltInZoomControls(false);
    settings.setSupportZoom(false); 
    setPadding(0, 0, 0, 0);
    
    getSettings().setJavaScriptEnabled(true);
    setWebViewClient(new MyWebViewClient(this));
    
    initJavascriptInterface();
    setWebChromeClient(new WebChromeClient() {
      public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        /*Log.e(TAG, message + " -- From line "
                             + lineNumber + " of "
                             + sourceID);
                             */
      }
    });
  }

  private void initJavascriptInterface()
  {
    addJavascriptInterface(new JavaScriptInterface(this, (Activity)this.getContext()), "jsinterface");
  }

  class JavaScriptInterface
  {
    BaseWebView mWebView;
    Activity    mActivity;
    
    JavaScriptInterface(BaseWebView webview, Activity activity)
    {
      mWebView = webview;
      mActivity = activity;
    }

    Integer resized = Integer.valueOf(0);
    public void onResize(int doc_width)
    {
      int width = mWebView.getWidth();
      final Double val = 100d * (Double.valueOf(width)/Double.valueOf(doc_width));
      
      //Log.e(TAG, String.format("Voy a cambiar el view en %d/%d = %d", width, doc_width, val.intValue()));
      
      mActivity.runOnUiThread( new Runnable() {
        @Override
        public void run() {
          mWebView.setInitialScale(val.intValue());
          synchronized (resized) {
            resized++;
            if(resized < 2)
              loadUrl("javascript:resize()");
          }
        }
      });
    }

    public void onLoad()
    {
      //Log.e(TAG, "---->ONLOAD<-----(the maku)");
      mWebView.TryToResize();
      
    }
  }

  
}
