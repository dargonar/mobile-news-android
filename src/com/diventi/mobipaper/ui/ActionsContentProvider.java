package com.diventi.mobipaper.ui;

import java.lang.ref.WeakReference;

public class ActionsContentProvider
{
  private static final ActionsContentProvider mInstance = new ActionsContentProvider();
  private static WeakReference<ActionsContentView> mToolbarRef;

  public static ActionsContentProvider getInstance()
  {
    synchronized(mInstance) {
      ActionsContentProvider local = mInstance;
      return local;
    }
  }

  public ActionsContentView getActionView()
  {
    synchronized (this) {

      ActionsContentView local = null;
      if (mToolbarRef != null)
        local = (ActionsContentView)mToolbarRef.get();
        
      return local;
    }
  }

  public void setActionView(ActionsContentView param)
  {
    synchronized (this) {
      mToolbarRef = new WeakReference(param);
      return;
    }
  }
}

/* Location:           /Users/matias/Downloads/bbc-app/bbc/out_dex2jar.jar
 * Qualified Name:     bbc.mobile.news.view.ToolbarProvider
 * JD-Core Version:    0.6.0
 */