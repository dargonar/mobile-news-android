package com.diventi.mobipaper.ui;

import java.lang.ref.WeakReference;

public class ToolbarProvider
{
  private static final ToolbarProvider mInstance = new ToolbarProvider();
  private static WeakReference<ToolbarView> mToolbarRef;

  public static ToolbarProvider getInstance()
  {
    synchronized(mInstance) {
      ToolbarProvider localToolbarProvider = mInstance;
      return localToolbarProvider;
    }
  }

  public ToolbarView getToolbar()
  {
    synchronized (this) {

      ToolbarView localToolbarView = null;
      if (mToolbarRef != null)
        localToolbarView = (ToolbarView)mToolbarRef.get();
        
      return localToolbarView;
    }
  }

  public void setToolbar(ToolbarView paramToolbarView)
  {
    synchronized (this) {
      mToolbarRef = new WeakReference(paramToolbarView);
      return;
    }
  }
}

/* Location:           /Users/matias/Downloads/bbc-app/bbc/out_dex2jar.jar
 * Qualified Name:     bbc.mobile.news.view.ToolbarProvider
 * JD-Core Version:    0.6.0
 */