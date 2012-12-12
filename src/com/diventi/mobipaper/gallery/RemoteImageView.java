package com.diventi.mobipaper.gallery;

import com.diventi.eldia.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RemoteImageView extends RelativeLayout {

  private ImageView mImage;
  private ImageView mError;
  private ImageView mLoadingImage;
  
  public RemoteImageView(Context context) {
    this(context, null);
  }

  public RemoteImageView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public RemoteImageView(Context context, AttributeSet attrs, int paramInt)
  {
    super(context, attrs, paramInt);
    
    if(isInEditMode())
      return;
  }

  public void setupViews(Context ctx) {
    mImage = (ImageView)findViewById(R.id.remote_image_view_image);
    
    mError = (ImageView)findViewById(R.id.remote_image_view_error);

    mLoadingImage = (ImageView)findViewById(R.id.remote_image_view_loading);
    mLoadingImage.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.rotate_indefinitely));
  }

  public void setRemoteImage(Bitmap bitmap) {

    mLoadingImage.setVisibility(View.INVISIBLE);
    mLoadingImage.clearAnimation();
    
    if(bitmap == null ) {
      mError.setVisibility(View.VISIBLE);
      return;
    }

    mImage.setImageBitmap(bitmap);
    mImage.setVisibility(View.VISIBLE);
  }
    
}
