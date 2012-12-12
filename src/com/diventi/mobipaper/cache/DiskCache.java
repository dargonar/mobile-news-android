package com.diventi.mobipaper.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.diventi.mobipaper.ScreenManager;

public class DiskCache {

  private static DiskCache mInstance = new DiskCache();
  
  public static DiskCache getInstance()
  {
    synchronized(mInstance) {
      DiskCache localDiskCache = mInstance;
      return localDiskCache;
    }
  }

  private File    mCacheFolder;
  private double  mCacheSizeMB;
  
  private boolean mInitialized;
  
  private static final String CACHE_FOLDER          = "mobipaper_cache";
  private static final double BYTES_IN_ONE_MEGABYTE = 1024.0*1024.0;
  
  public DiskCache() {
    mInitialized = false;
  }
  
  public void configure(File rootFolder, double cacheSizeMB) {

    mCacheFolder = new File(rootFolder, CACHE_FOLDER);
    mCacheSizeMB = cacheSizeMB;
    
    mInitialized = true;
    
    if( mCacheFolder.exists() == true )
      return;
    
    if( mCacheFolder.mkdirs() == true )
      return;

    mInitialized = false;
  }
  
  public byte[] get(String key, String prefix) {
    if(!mInitialized) return null;
    
    File file = buildFile(key, prefix);
    if(!file.exists())
      return null;
    
    try {
      return IOUtils.toByteArray(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }
  
  public boolean put(String key, byte[] data, String prefix) {
    
    if(!mInitialized) return false;
    
    File file = buildFile(key, prefix);
    try {
      FileOutputStream fs = new FileOutputStream(file);
      fs.write(data);
      fs.close();
      return true;
    } catch (Exception e) {
      return false;
    }

  }
  
  public boolean remove(String key, String prefix) {
    if(!mInitialized) return false;
    
    File file = buildFile(key, prefix);
    return file.delete();
  }
  
  public boolean exists(String key, String prefix) {
    if(!mInitialized) return false;
    
    File file = buildFile(key, prefix);
    return file.exists();
  }

  public long createdAt(String key, String prefix) {
    if(!mInitialized) return 0;
    
    File file = buildFile(key, prefix);
    return file.lastModified();
  }

  public double maxSize() {
    return mCacheSizeMB;
  }
  
  public double size() {
    if(!mInitialized) return 0;
    
    long total = 0;
    
    Iterator<File> iter = FileUtils.iterateFiles(mCacheFolder, new String[] {ScreenManager.IMAGE_PREFIX,ScreenManager.ARTICLE_PREFIX,ScreenManager.IMAGE_GROUP_PREFIX}, false);
    while(iter.hasNext()) {
      File file = (File) iter.next();
      total += file.length();
    }
    
    return ((double)total)/BYTES_IN_ONE_MEGABYTE;
  }
  
  public void purge() {
    if(!mInitialized) return;
    
    double actual_size = size();
    double max_size    = maxSize();
    
    if(actual_size <= max_size)
      return;
    
    //Get to 25% of max size
    double removeMB = actual_size - max_size*0.25;
    
    shrinkCache(removeMB);
  }

  public File getFolder() {
    return mCacheFolder;
  }
  
  private void shrinkCache(double removeMB) {

    if(removeMB < 0)
      return;
    
    File[] files = mCacheFolder.listFiles( new RemovableFilesFilter(new String[] {ScreenManager.ARTICLE_PREFIX, ScreenManager.IMAGE_GROUP_PREFIX, ScreenManager.IMAGE_PREFIX , ScreenManager.CLASSIFIED_PREFIX}) );
    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2)
      {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      }
    });

    for(int i=0; i<files.length && removeMB > 0; i++) {
      File file = (File) files[i];
      removeMB -= ((double)file.length())/BYTES_IN_ONE_MEGABYTE;
      file.delete();
    }

    return;
  }
  
  private File buildFile(String key, String prefix) {
    return new File(String.format("%s/%s.%s", mCacheFolder, key, prefix));
  }

  class RemovableFilesFilter implements FilenameFilter {
    
    private String[] extentions;
    RemovableFilesFilter(String[] extentions) {
      this.extentions = extentions;
    }
    
    public boolean accept(File dir, String name) {
      
      for(String ext : extentions) {
        if( name.endsWith("."+ext) )
          return true;
      }
      
      return false;
    }
}  
}
