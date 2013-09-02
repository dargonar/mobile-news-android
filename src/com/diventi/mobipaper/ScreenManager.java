package com.diventi.mobipaper;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import android.net.Uri;
import com.diventi.mobipaper.cache.DiskCache;
import com.diventi.utils.Network;
import com.diventi.utils.NoNetwork;
import com.diventi.utils.SHA1;

public class ScreenManager {

  @SuppressWarnings("unused")
  private static final String TAG = "ScreenManager"; 
  
  private Boolean m_isBig = false;
  public Boolean IsBig() { return m_isBig; }
  public void IsBig(Boolean isBig) { m_isBig = isBig; } 

  private Boolean m_isLandScape = false;
  public Boolean IsLandscape() { return m_isLandScape; }
  public void IsLandscape(Boolean isBig) { m_isLandScape = isBig; } 
  
  public static final String IMAGE_GROUP_PREFIX  = "mi";
  public static final String ARTICLE_PREFIX      = "a";
  public static final String SECTION_PREFIX      = "s";
  public static final String MENU_PREFIX         = "m";
  public static final String IMAGE_PREFIX        = "i";
  public static final String CLASSIFIED_PREFIX   = "s";
  public static final String FUNEBRES_PREFIX     = "fun";
  public static final String FARMACIAS_PREFIX    = "far";
  public static final String CARTELERA_PREFIX    = "car";  
  
//  private static String MAIN_STYLESHEET          = "1_main_list.xsl";
//  private static String NOTICIA_STYLESHEET       = "3_new.xsl";
//  private static String SECTIONS_STYLESHEET      = "2_section_list.xsl";
//  private static String MENU_STYLESHEET          = "4_menu.xsl";
//  private static String CLASIFICADOS_STYLESHEET  = "5_clasificados.xsl";
//  private static String FUNEBRES_STYLESHEET      = "6_funebres.xsl";
//  private static String FARMACIAS_STYLESHEET     = "7_farmacias.xsl";
//  private static String CARTELERA_STYLESHEET     = "8_cartelera.xsl";
//  
//  
//  private static String BIG_MAIN_STYLESHEET                 = "1_tablet_main_list.xsl";
//  private static String BIG_SECTION_STYLESHEET              = "1_tablet_section_list.xsl";
//  private static String BIG_SECTION_NEWS_PT_STYLESHEET      = "2_tablet_noticias_seccion_portrait.xsl";
//  private static String BIG_SECTION_NEWS_LS_STYLESHEET      = "2_tablet_noticias_seccion_landscape.xsl";
//  private static String BIG_MAIN_NEWS_PT_STYLESHEET         = "2_tablet_noticias_index_portrait.xsl";
//  private static String BIG_MAIN_NEWS_LS_STYLESHEET         = "2_tablet_noticias_index_landscape.xsl";
//  private static String BIG_NOTICIA_PT_STYLESHEET           = "3_tablet_new_global.xsl";
//  private static String BIG_NOTICIA_LS_STYLESHEET           = "3_tablet_new_landscape.xsl";
//  private static String BIG_MENU_STYLESHEET                 = "4_tablet_menu_secciones.xsl";
//  private static String BIG_CLASIFICADOS_STYLESHEET         = "5_tablet_clasificados.xsl";
//  private static String BIG_FUNEBRES_STYLESHEET             = "6_tablet_funebres.xsl";
//  private static String BIG_FARMACIAS_STYLESHEET            = "7_tablet_farmacias.xsl";
//  private static String BIG_CARTELERA_STYLESHEET            = "8_tablet_cartelera.xsl";
//  
//  
//  private static String MAIN_URL          = "http://www.eldia.com.ar/rss/index.aspx";
//  private static String NOTICIA_URL       = "http://www.eldia.com.ar/rss/noticia.aspx?id=%s";
//  private static String SECTIONS_URL      = "http://www.eldia.com.ar/rss/index.aspx?seccion=%s";
//  private static String MENU_URL          = "http://www.eldia.com.ar/rss/secciones.aspx";
//  private static String CLASIFICADOS_URL  = "http://www.eldia.com.ar/mc/clasi_rss_utf8.aspx?idr=%s&app=1";
//  private static String FUNEBRES_URL      = "http://www.eldia.com.ar/mc/fune_rss_utf8.aspx";
//  private static String CARTELERA_URL     = "http://www.eldia.com.ar/extras/carteleradecine_txt.aspx";
//  private static String FARMACIAS_URL     = "http://www.eldia.com.ar/extras/farmacias_txt.aspx";
  
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

  public String getFunebres(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, false, ScreenManager.FUNEBRES_PREFIX);
  }

  public String getCartelera(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, false, ScreenManager.CARTELERA_PREFIX);
  }

  public String getFarmacias(String url, boolean useCache) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    return getScreen(url, useCache, false, ScreenManager.FARMACIAS_PREFIX);
  }

  
  public long sectionDate(String url) {

    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
    
    return cache.createdAt(key, ScreenManager.SECTION_PREFIX);
  }
  
  public String getScreen(String url, boolean useCache, boolean processImages, String prefix) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
    String tmp = getScreenPlain(url, useCache, processImages, prefix);
    return tmp;
  }
  
  public String getScreenPlain(String url, boolean useCache, boolean processImages, String prefix) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, NoNetwork {
  
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
        
    if( useCache == true )
    {
      byte[] html = cache.get(key, prefix);
      if(html != null)
        return new String(html,"utf-8");
    }

    if( !Network.hasConnection() ) {
      throw new NoNetwork();
    }
      
    downloadHtml(url, key, prefix);
    byte[] html = cache.get(key, prefix);
    return new String(html,"utf-8");
  }

  void downloadHtml(String iurl, String key, String prefix) throws IOException
  {

    String urlParameters = String.format("url=%s&appid=%s&size=%s&ptls=%s&net=%s&ver=%s", 
                iurl, 
                MobiPaperApp.getAppId(), 
                IsBig() ? "big" : "small",
                IsLandscape() ? "ls" : "pt",
                Network.connectionType(),
                MobiPaperApp.getMediaVersion()
    );

    URL url = new URL("http://www.diariosmoviles.com.ar/ws/screen");
    
    //URL url = new URL("http://192.168.1.14:8080/ws/screen");
    
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    con.setRequestProperty("charset", "utf-8");
    con.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
    con.setRequestMethod("POST");
    
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();

    InputStream is = con.getInputStream();

    ZipInputStream zis = new ZipInputStream(is);
    

    DiskCache cache = DiskCache.getInstance();
    ZipEntry entry = zis.getNextEntry();
    while (entry != null)
    {
      String name = entry.getName();
      cache.put(name, IOUtils.toByteArray(zis));
      entry = zis.getNextEntry();
    }
    
    return;
  }
  
  public ArrayList<String> getPendingImages(String url) throws StreamCorruptedException, IOException, ClassNotFoundException {
    
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);
    
    byte[] mis = cache.get(key, ScreenManager.IMAGE_GROUP_PREFIX);

    ArrayList<String> images = new ArrayList<String>();
    String tmp = new String(mis);
    
    for(String i : tmp.split(",") )
      images.add(i);
    
    Iterator<String> iter = images.iterator();
    while(iter.hasNext()) {
      String image = iter.next();
      if(cache.exists(image, ScreenManager.IMAGE_PREFIX)) {
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
    return screenExists("menu://", ScreenManager.MENU_PREFIX);
  }
  
  public boolean classifiedExists(String url) {
    return screenExists(url, ScreenManager.CLASSIFIED_PREFIX);
  }

  public boolean funebresExists(String url) {
    return screenExists(url, ScreenManager.FUNEBRES_PREFIX);
  }

  public boolean carteleraExists(String url) {
    return screenExists(url, ScreenManager.CARTELERA_PREFIX);
  }

  public boolean farmaciasExists(String url) {
    return screenExists(url, ScreenManager.FARMACIAS_PREFIX);
  }

  
  public boolean screenExists(String url, String prefix) {
    DiskCache cache = DiskCache.getInstance();
    String key = SHA1.sha1(url);

    return cache.exists(key, prefix);
  }
  
  
//  private String getStyleSheetBig(String url) throws MalformedURLException {
//    
//    if( url.startsWith("menu_section://main") ) {
//      return BIG_MAIN_NEWS_PT_STYLESHEET;
//    }
//
//    if( url.startsWith("menu_section://") ) {
//      return IsLandscape() ? BIG_SECTION_NEWS_LS_STYLESHEET : BIG_SECTION_NEWS_PT_STYLESHEET;
//    }
//
//    if( url.startsWith("ls_menu_section://main") ) {
//      return BIG_MAIN_NEWS_LS_STYLESHEET;
//    }
//    
//    if( url.startsWith("ls_menu_section://") ) {
//      return BIG_SECTION_NEWS_LS_STYLESHEET;
//    }
//
//    if( url.startsWith("section://main") ) {
//      return BIG_MAIN_STYLESHEET;
//    }
//    
//    if( url.startsWith("section://") ) {
//      return BIG_SECTION_STYLESHEET;
//    }    
//        
//    if( url.startsWith("ls_section://") ) {
//      return SECTIONS_STYLESHEET;
//    }    
//
//    if( url.startsWith("noticia://") ) {
//      return BIG_NOTICIA_PT_STYLESHEET;
//    }    
//
//    if( url.startsWith("ls_noticia://") ) {
//      return BIG_NOTICIA_LS_STYLESHEET;
//    }    
//    
//    if( url.startsWith("clasificados://") ) {
//      return BIG_CLASIFICADOS_STYLESHEET;
//    }    
//
//    if( url.startsWith("funebres://") ) {
//      return BIG_FUNEBRES_STYLESHEET;
//    }    
//        
//    if( url.startsWith("menu://") ) {
//      return BIG_MENU_STYLESHEET;
//    }  
//
//    if( url.startsWith("farmacia://") ) {
//      return BIG_FARMACIAS_STYLESHEET;
//    }  
//        
//    if( url.startsWith("cartelera://") ) {
//      return BIG_CARTELERA_STYLESHEET;
//    }  
//        
//    throw new MalformedURLException();
//    
//  }
//  
//  private String getStyleSheet(String url) throws MalformedURLException {
//
//    if( IsBig() )
//      return getStyleSheetBig(url);
//    
//    if( url.startsWith("section://main") ) {
//      return MAIN_STYLESHEET;
//    }
//    
//    if( url.startsWith("noticia://") ) {  
//      return NOTICIA_STYLESHEET;
//    }
//    
//    if( url.startsWith("clasificados://") ) {
//      return CLASIFICADOS_STYLESHEET;
//    }
//    
//    if( url.startsWith("section://") ) {  
//      return SECTIONS_STYLESHEET;
//    }
//
//    if( url.startsWith("menu://") ) {
//      return MENU_STYLESHEET;
//    }
//    
//    if( url.startsWith("cartelera://") ) {  
//      return CARTELERA_STYLESHEET;
//    }
//
//    if( url.startsWith("funebres://") ) {  
//      return FUNEBRES_STYLESHEET;
//    }
//
//    if( url.startsWith("farmacia://") ) {  
//      return FARMACIAS_STYLESHEET;
//    }
//
//    throw new MalformedURLException();
//  }
// 
//  
//  private String getXmlHttpUrl(String url) throws URISyntaxException {
//    
//    Uri tmp = Uri.parse(url);
//    
//    if( url.startsWith("section://main") ) {
//      return MAIN_URL;
//    }
//    
//    if( url.startsWith("noticia://") ) {  
//      return String.format(NOTICIA_URL, tmp.getHost());
//    }
//    
//    if( url.startsWith("section://") ) {
//      return String.format(SECTIONS_URL, tmp.getHost());
//    }
//  
//    if( url.startsWith("clasificados://") ) {
//      return String.format(CLASIFICADOS_URL, tmp.getHost());
//    }
//  
//    if( url.startsWith("menu://") ) {
//      return String.format(MENU_URL);
//    }
//
//    if( url.startsWith("funebres://") ) {
//      return String.format(FUNEBRES_URL);
//    }
//
//    if( url.startsWith("farmacia://") ) {
//      return String.format(FARMACIAS_URL);
//    }
//
//    if( url.startsWith("cartelera://") ) {
//      return String.format(CARTELERA_URL);
//    }
//    
//    throw new URISyntaxException("","");
//  }

  /*
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
    
    //HACK -- skip bom
    if(tmp[0] ==  -17 && tmp[1] == -69 && tmp[2] == -65)
    {
      byte[] tmp2 = new byte[tmp.length-3];
      System.arraycopy(tmp, 3, tmp2, 0, tmp.length-3);
      tmp = tmp2;
    }

    //HACK -- wrap xml inside "*_txt.aspx" urls
    if(uri.endsWith("_txt.aspx"))
    {
      SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZ");
      String fake_xml = String.format("<rss xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:news=\"http://www.diariosmoviles.com.ar/news-rss/\" version=\"2.0\"><channel><pubDate>%s -0300</pubDate><item><![CDATA[%s]]></item></channel></rss>",
          df.format(new Date()), new String(tmp));
      
      tmp = fake_xml.getBytes();
    }

    //FileUtils.writeByteArrayToFile(new File( DiskCache.getInstance().getFolder(), "down.xml" ), tmp);
    
    return tmp;
  }
  */
  
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
