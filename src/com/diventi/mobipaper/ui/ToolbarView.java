package com.diventi.mobipaper.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class ToolbarView extends RelativeLayout implements Animation.AnimationListener
{
  private static final int ANIM_AUTO_HIDE_DELAY = 3000;
  private static final int ANIM_STATE_DEFAULT = 1;
  private static final int ANIM_STATE_HIDDEN = 1;
  private static final int ANIM_STATE_VISIBLE = 0;
  
  private static final int ENTER_DOWN_ANIM = 0;
  private static final int ENTER_UP_ANIM = 1;
  private static final int EXIT_DOWN_ANIM = 2;
  private static final int EXIT_UP_ANIM = 3;
 
  private static final int MSG_AUTO_HIDE_TOOLBAR = 0;
  
  private int mAnimState = ANIM_STATE_DEFAULT;
  private final int mAnnimationDuration = 250;
  private boolean mAutoHideEnabled = true;
  
  private final String TAG = "ToolbarView";
  
  private final Handler mHandler = new Handler()
  {
    public void handleMessage(Message msg)
    {
      if(msg.what == MSG_AUTO_HIDE_TOOLBAR)
        showToolbar(false);
    }
  };
  
  private final Interpolator mInterpolator = new AccelerateInterpolator();

  public ToolbarView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public ToolbarView(Context context, AttributeSet attrs, int paramInt)
  {
    super(context, attrs, paramInt);
    initView();
  }

  private void animate(int paramInt)
  {
    float f1 = 0.0F;
    float f2 = 0.0F;
    float f3 = 0.0F;
    float f4 = 0.0F;
    
    switch (paramInt)
    {
      case ENTER_DOWN_ANIM:
        f1 = -1.0F;
        f2 = 0.0F;
        f3 = 0.0F;
        f4 = 1.0F;
        break;
      
      case ENTER_UP_ANIM:
        f1 = 1.0F;
        f2 = 0.0F;
        f3 = 0.0F;
        f4 = 1.0F;
        break;
        
      case EXIT_DOWN_ANIM:
        f1 = 0.0F;
        f2 = 1.0F;
        f3 = 1.0F;
        f4 = 0.0F;
        break;
        
      case EXIT_UP_ANIM:
        f1 = 0.0F;
        f2 = -1.0F;
        f3 = 1.0F;
        f4 = 0.0F;
        break;
    }
    
    TranslateAnimation localTranslateAnimation = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, f1, 1, f2);
    AlphaAnimation localAlphaAnimation = new AlphaAnimation(f3, f4);
    AnimationSet localAnimationSet = new AnimationSet(true);
    localAnimationSet.addAnimation(localTranslateAnimation);
    localAnimationSet.addAnimation(localAlphaAnimation);
    localAnimationSet.setInterpolator(this.mInterpolator);
    localAnimationSet.setDuration(mAnnimationDuration);
    localAnimationSet.setAnimationListener(this);
    startAnimation(localAnimationSet);
  }

  private void initView()
  {
    setDefaultVisibility();
  }

  private void setDefaultVisibility()
  {
    
    int i = View.INVISIBLE;

    switch (this.mAnimState)
    {
      case ANIM_STATE_HIDDEN:
        i = View.INVISIBLE;
        break;
      case ANIM_STATE_VISIBLE:
        i = View.VISIBLE;
        break;
    }
    
    setVisibility(i);

  }


  public void onTap()
  {
    String str = "Unknown";
    switch (this.mAnimState)
    {
      case ANIM_STATE_HIDDEN:
        str = "ANIM_STATE_HIDDEN";
        break;
  
      case ANIM_STATE_VISIBLE:
        str = "ANIM_STATE_VISIBLE";
        break;
    }

    switch (getVisibility())
    {
      case 0:
        new StringBuilder(String.valueOf(str)).append(" visibility:Visible").toString();
      case 4:
        new StringBuilder(String.valueOf(str)).append(" visiblity:Invisible").toString();
    }

    //Log.e(TAG, str);
    
    invalidate();
    switch (this.mAnimState)
    {
      case 1:
        showToolbar(true);
        break;
      case 0:
        showToolbar(false);
        break;
    }
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    resetAutoHideTimer();
    return true;
  }

  public void resetAutoHideTimer()
  {
    if (mAutoHideEnabled)
    {
      mHandler.removeMessages(MSG_AUTO_HIDE_TOOLBAR);
      mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_HIDE_TOOLBAR), ANIM_AUTO_HIDE_DELAY);
    }
  }

  public void showToolbar(boolean show)
  {
    //Log.e(TAG, "aca vamos " + Boolean.toString(show));
    if (show && mAnimState == ANIM_STATE_HIDDEN)
    {
      mAnimState = ANIM_STATE_VISIBLE;
      setVisibility(View.VISIBLE);
      animate(ENTER_UP_ANIM);
      
      resetAutoHideTimer();
    }
    else
    if (!show && mAnimState == ANIM_STATE_VISIBLE)
    {
      mAnimState = ANIM_STATE_HIDDEN;
      setVisibility(View.INVISIBLE);
      animate(EXIT_DOWN_ANIM);
    }
  }
  
  @Override
  public void onAnimationEnd(Animation animation) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onAnimationRepeat(Animation animation) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onAnimationStart(Animation animation) {
    // TODO Auto-generated method stub
    
  }
}