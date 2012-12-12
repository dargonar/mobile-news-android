package com.diventi.mobipaper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.mobipaper.xml.ExtractAndRebuildXmlParser;
import com.diventi.mobipaper.xml.MobiImage;
import com.diventi.utils.Network;
import com.diventi.utils.NoNetwork;
import com.diventi.utils.SHA1;

public class ScreenManager {

  private static final String TAG = "ScreenManager"; 
  
  public static final String IMAGE_GROUP_PREFIX  = "mi";
  public static final String ARTICLE_PREFIX      = "a";
  public static final String SECTION_PREFIX      = "s";
  public static final String MENU_PREFIX         = "m";
  public static final String IMAGE_PREFIX        = "i";
  public static final String CLASSIFIED_PREFIX   = "cf";
  
  private static String MAIN_STYLESHEET          = "1_main_list.xsl";
  private static String NOTICIA_STYLESHEET       = "3_new.xsl";
  private static String SECTIONS_STYLESHEET      = "2_section_list.xsl";
  private static String MENU_STYLESHEET          = "4_menu.xsl";
  private static String CLASIFICADOS_STYLESHEET  = "5_clasificados.xsl";

  private static String MAIN_URL          = "http://www.eldia.com.ar/rss/index.aspx";
  private static String NOTICIA_URL       = "http://www.eldia.com.ar/rss/noticia.aspx?id=%s";
  private static String SECTIONS_URL      = "http://www.eldia.com.ar/rss/index.aspx?seccion=%s";
  private static String MENU_URL          = "http://www.eldia.com.ar/rss/secciones.aspx";
  private static String CLASIFICADOS_URL  = "http://www.eldia.com.ar/mc/clasi_rss.aspx?idr=%s&app=1";
  
  public String getArticle(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, true, ScreenManager.ARTICLE_PREFIX);
  }

  public String getSection(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, true, ScreenManager.SECTION_PREFIX);
  }

  public String getMenu(boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen("menu://left", useCache, false, ScreenManager.MENU_PREFIX);
  }

  public String getClasifieds(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, false, ScreenManager.CLASSIFIED_PREFIX);
  }
  
  public long sectionDate(String url) {

    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
    
    return cache.createdAt(key, ScreenManager.SECTION_PREFIX);
  }
  
  public String getScreen(String url, boolean useCache, boolean processImages, String prefix) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    long t0 = System.currentTimeMillis();
    String tmp = getScreenPlain(url, useCache, processImages, prefix);
    long t1 = System.currentTimeMillis();

    //Log.d(TAG, "----------> ELAPSED <---------");
    //Log.d(TAG, 
    //String.format("----------> %.2f s <---------", (t1 - t0)/1000.0 ));
    
    //System.out.println("That took " + (endTime - startTime) + " milliseconds");

    return tmp;
  }
  
  public String getScreenPlain(String url, boolean useCache, boolean processImages, String prefix) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
  
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
        
    if ( useCache == true )
    {
      byte[] html = cache.get(key, prefix);
      if(html != null)
        return new String(html,"utf-8");
    }

    if( !Network.hasConnection() ) {
      throw new NoNetwork();
    }
      

    long t0 = System.currentTimeMillis();
    byte[] xml = downloadUrl( getXmlHttpUrl(url) );
    long t1 = System.currentTimeMillis();

    //Log.d(TAG, "----------> ELAPSED NETWORK <---------");
    //Log.d(TAG, 
    //String.format("----------> %.2f s <---------", (t1 - t0)/1000.0 ));

    //hack: eldia
    String encoding = "UTF-8";
    
    String packageName = MobiPaperApp.getContext().getPackageName();
    if(url.startsWith("clasificados") && packageName.equals("com.diventi.eldia"))
      encoding = "ISO-8859-1";
    
    //FileUtils.writeByteArrayToFile(new File(DiskCache.getInstance().getFolder(), String.format("pre-beto-%s.xml",encoding)), xml);
    
    //TODO:Sanitize
    //
    
    if(processImages) {
      t0 = System.currentTimeMillis();
      
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      
      InputSource reader = new InputSource( new InputStreamReader( new ByteArrayInputStream(xml), encoding) );
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlSerializer serializer = Xml.newSerializer();
      serializer.setOutput(out, "utf-8");
      
      ExtractAndRebuildXmlParser extractor = new ExtractAndRebuildXmlParser(serializer, url); 
      parser.parse(reader, extractor);
      
      serializer.flush();
      out.close();
      
      xml = out.toByteArray();
      
      //FileUtils.writeByteArrayToFile(new File(DiskCache.getInstance().getFolder(), String.format("post-beto-%s.xml",encoding)), xml);
      
      ByteArrayOutputStream mout = new ByteArrayOutputStream();
      ObjectOutputStream oout = new ObjectOutputStream(mout);
      oout.writeObject(extractor.getImages());
      byte[] mobiimgs = mout.toByteArray();
      cache.put(key, mobiimgs, ScreenManager.IMAGE_GROUP_PREFIX);
      
      t1 = System.currentTimeMillis();

      //Log.d(TAG, "----------> ELAPSED IMAGES <---------");
      //Log.d(TAG, 
      //String.format("----------> %.2f s <---------", (t1 - t0)/1000.0 ));

    }

    t0 = System.currentTimeMillis();
    HtmlGenerator generator = new HtmlGenerator();
    byte[] html = generator.generate(xml, getStyleSheet(url), encoding);
    t1 = System.currentTimeMillis();
    
    //Log.d(TAG, "----------> ELAPSED HTML <---------");
    //Log.d(TAG, 
    //String.format("----------> %.2f s <---------", (t1 - t0)/1000.0 ));

    cache.put(key, html, prefix);
    return new String(html,"utf-8");
  }

  public ArrayList<MobiImage> getPendingImages(String url) throws StreamCorruptedException, IOException, ClassNotFoundException {
    
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
    
    byte[] mis = cache.get(key, ScreenManager.IMAGE_GROUP_PREFIX);
    ByteArrayInputStream min = new ByteArrayInputStream(mis);
    ObjectInputStream oin = new ObjectInputStream(min);
    
    @SuppressWarnings("unchecked")
    ArrayList<MobiImage> images = (ArrayList<MobiImage>)oin.readObject();
    
    Iterator<MobiImage> iter = images.iterator();
    while(iter.hasNext()) {
      MobiImage image = iter.next();
      if(cache.exists(image.localUrl, ScreenManager.IMAGE_PREFIX)) {
        iter.remove();
      }
    }
    
    if(images.size() == 0)
      cache.remove(key, ScreenManager.IMAGE_GROUP_PREFIX);
    
    return images;
  }
  
  public boolean sectionExists(String url) {
    return screenExists(url, ScreenManager.SECTION_PREFIX);
  }
  
  public boolean articleExists(String url) {
    return screenExists(url, ScreenManager.ARTICLE_PREFIX);
  }
  
  public boolean menuExists() {
    return screenExists("menu://left", ScreenManager.MENU_PREFIX);
  }
  
  public boolean classifiedExists(String url) {
    return screenExists(url, ScreenManager.CLASSIFIED_PREFIX);
  }

  public boolean screenExists(String url, String prefix) {
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);

    return cache.exists(key, prefix);
  }
  
  
  private String getStyleSheet(String url) throws MalformedURLException {

    if( url.startsWith("section://main") ) {
      return MAIN_STYLESHEET;
    }
    
    if( url.startsWith("noticia://") ) {  
      return NOTICIA_STYLESHEET;
    }
    
    if( url.startsWith("clasificados://") ) {
      return CLASIFICADOS_STYLESHEET;
    }
    
    if( url.startsWith("section://") ) {  
      return SECTIONS_STYLESHEET;
    }

    if( url.startsWith("menu://") ) {
      return MENU_STYLESHEET;
    }

    throw new MalformedURLException();
  }
  
  private String getXmlHttpUrl(String url) throws URISyntaxException {
    
    Uri tmp = Uri.parse(url);
    
    if( url.startsWith("section://main") ) {
      return MAIN_URL;
    }
    
    if( url.startsWith("noticia://") ) {  
      return String.format(NOTICIA_URL, tmp.getHost());
    }
    
    if( url.startsWith("section://") ) {
      return String.format(SECTIONS_URL, tmp.getHost());
    }
  
    if( url.startsWith("clasificados://") ) {
      return String.format(CLASIFICADOS_URL, tmp.getHost());
    }
  
    if( url.startsWith("menu://") ) {
      return String.format(MENU_URL);
    }
  
    throw new URISyntaxException("","");
  }

  private byte[] downloadUrl(String uri) throws MalformedURLException, IOException {
    
    URL url = new URL(uri);
    URLConnection con = url.openConnection();

    con.setConnectTimeout(10*1000);
    con.setReadTimeout(10*1000);

    InputStream is = con.getInputStream();
        
    byte[] tmp = IOUtils.toByteArray(is);
    
    if(tmp == null || tmp.length == 0) {
      throw new SocketException("invalid response");
    }
    
    //SKIP BOM
    if(tmp[0] ==  -17 && tmp[1] == -69 && tmp[2] == -65)
    {
      byte[] tmp2 = new byte[tmp.length-3];
      System.arraycopy(tmp, 3, tmp2, 0, tmp.length-3);
      tmp = tmp2;
    }
    
    //FileUtils.writeByteArrayToFile(new File( DiskCache.getInstance().getFolder(), "down.xml" ), tmp);
    
    return tmp;
  }
  
  public File getPage(String url) {
    DiskCache cache = DiskCache.getInstance();

    Uri uri = Uri.parse(url);
    String key = SHA1.sha1(uri.getHost());
    
    return new File(cache.getFolder() + "/pages", key );
  }

  public long classifiedDate(String url) {
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
    
    return cache.createdAt(key, ScreenManager.CLASSIFIED_PREFIX);
  }
  
}
