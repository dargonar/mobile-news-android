package com.diventi.mobipaper.gallery;

import com.diventi.eldia.R;

import com.diventi.mobipaper.BaseActivity;
import com.diventi.mobipaper.ScreenManager;
import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.utils.SHA1;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class GalleryActivity extends BaseActivity {

  private static final String TAG = "GalleryActivity";
  
  private ViewPager       mImagePager;
  private TextView        mCurrentImage;
  private AsyncHttpClient mClient = new AsyncHttpClient();
  private String[]        mUrls;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.image_gallery);
      
      mUrls = getIntent().getStringArrayExtra("urls");
      
      setupViews();
  }
  
  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(this, 0, R.anim.slide_down);
  }
  
  private void setupViews() {

    mImagePager   = (ViewPager) findViewById(R.id.image_pager);
    
    mCurrentImage = (TextView) findViewById(R.id.image_number);
    mCurrentImage.setText(String.format("%d/%d", 1, mUrls.length));
    
    RemoteImagesPagerAdapter imageAdapter = new RemoteImagesPagerAdapter(this, mUrls);
    mImagePager.setAdapter(imageAdapter);
    mImagePager.setOnPageChangeListener(new OnPageChangeListener() {
      
      @Override
      public void onPageSelected(int page) {
        mCurrentImage.setText(String.format("%d/%d", page+1, mUrls.length));
      }
      
      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
      }
      
      @Override
      public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub        
      }
    });
  }


  class RemoteImagesPagerAdapter extends PagerAdapter {

    private LayoutInflater  mInflater;
    private Context         mContext;
    private String[]        mUrls;
    
    public RemoteImagesPagerAdapter(Context ctx, String[] urls) {

      mContext  = ctx;
      mUrls     = urls;
      mInflater = (LayoutInflater)mContext.getSystemService
          (Context.LAYOUT_INFLATER_SERVICE);

    }
    
    @Override
    public int getCount() {
      return mUrls.length;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
      //Log.e(TAG, "destroyItem");      
      ((ViewPager) collection).removeView((RemoteImageView) view);
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
      //Log.e(TAG, "isViewFromObject");
      return view==((RemoteImageView)object);
    }
    
    @Override
    public Object instantiateItem(View collection, final int position) {
      
      //Log.e(TAG, "instantiateItem");
      
      final RemoteImageView remoteImage = (RemoteImageView) mInflater.inflate(R.layout.remote_image_view, null);
      remoteImage.setupViews(mContext);
      
      final DiskCache cache = DiskCache.getInstance();
      final String    key   = SHA1.sha1(mUrls[position]); 
      
      if( !cache.exists(key, ScreenManager.IMAGE_PREFIX) ) {

        mClient.get(mUrls[position], new BinaryHttpResponseHandler() {

          @Override
          public void onSuccess(int statusCode, final byte[] binaryData) {
            super.onSuccess(statusCode, binaryData);
            cache.put(key, binaryData, ScreenManager.IMAGE_PREFIX);
            setBinaryOnImage(binaryData);
          }
          
          @Override
          public void onFailure(Throwable error) {
            setBinaryOnImage(null);
          }
          
          public void setBinaryOnImage(final byte[] binaryData) {
            runOnUiThread( new Runnable() {
              @Override
              public void run() {
                remoteImage.setRemoteImage(bitmapFromByteArray(binaryData));
              }
            });            
          }
          
        });
        
      } else {
        remoteImage.setRemoteImage(bitmapFromByteArray(cache.get(key, ScreenManager.IMAGE_PREFIX)));
      }
      
      ((ViewPager) collection).addView(remoteImage,0);
      return remoteImage;
    }

    @Override
    public void finishUpdate(View arg0) {}
    

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {}

    @Override
    public Parcelable saveState() {
            return null;
    }

    @Override
    public void startUpdate(View arg0) {}
    
    private Bitmap bitmapFromByteArray(byte[] bs) {
      if(bs == null)
        return null;
      
      return BitmapFactory.decodeByteArray(bs, 0, bs.length);
    }
    
  }
}
