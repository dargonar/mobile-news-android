package com.diventi.mobipaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.content.res.AssetManager;

import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.utils.SHA1;

public class ResourceManager {
  
  private static final String MARK_FILE = ".htmlfolderscopied";

  public boolean foldersExists() {
    File folder = DiskCache.getInstance().getFolder();
    
    File fileMark = new File(folder, MARK_FILE);
    return fileMark.exists();
  }
  
  public void copyResources() throws IOException {
    
    DiskCache cache = DiskCache.getInstance();
    File folder = cache.getFolder();
    
    //Remove mark
    File fileMark = new File(folder, MARK_FILE);
    fileMark.delete();
    
    //Copy folders
    for(String assetFolder : new String[] {"css", "js", "img", "pages"}) {
      copyAssetFolderTo(assetFolder, folder);
    }

    //Set mark
    FileUtils.writeByteArrayToFile(fileMark, new byte[] {100});
  }
  
  private void copyAssetFolderTo(String assetFolder, File to) throws IOException {
    
    File destFolder = new File(to, assetFolder);
    
    if(!destFolder.exists() && !destFolder.mkdirs() )
      throw new IOException("unable to create folder");
    
    AssetManager assets = MobiPaperApp.getContext().getAssets();
    
    for( String fileName : assets.list(assetFolder) ) {
      
      InputStream is = assets.open(assetFolder + "/" + fileName);
      
      String destName = fileName;
      if(assetFolder == "pages")
        destName = SHA1.sha1(fileName);
      
      FileUtils.writeByteArrayToFile(new File(destFolder, destName), 
          IOUtils.toByteArray(is));

      is.close();
    }

  }
  
}
