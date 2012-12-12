package com.diventi.mobipaper.xml;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

import com.diventi.utils.SHA1;

public class ExtractAndRebuildXmlParser extends DefaultHandler {

  private XmlSerializer           mSerializer;
  private ArrayList<MobiImage>    mImages;
  private String                  mNoticiaUrl;
  
  public ExtractAndRebuildXmlParser(XmlSerializer serializer, String noticia) {
    mSerializer = serializer;
    mImages     = new ArrayList<MobiImage>();
    mNoticiaUrl = noticia;
  }
  
  public ArrayList<MobiImage> getImages() {
    return mImages;
  }
  
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    
  };
  
  @Override
  public void startDocument() throws org.xml.sax.SAXException {
    try {
      mSerializer.startDocument("utf-8", false);
    } catch (IOException e) {
      //Log.e("ExtractAndBuildParser", e.toString());
    }  
  };

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      mSerializer.text(ch, start, length);
    } catch (IOException e) {
      //Log.e("ExtractAndBuildParser", e.toString());
    }
  };
  
  @Override
  public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws org.xml.sax.SAXException {
    
    
    try {
      mSerializer.startTag("", localName);

      for(int i=0; i<atts.getLength(); i++) {
        
        String a_uri   = atts.getURI(i);
        if(a_uri == "xmlns")
          continue;
        
        String a_name  = atts.getLocalName(i);
        String a_value = atts.getValue(i);
        
        if( localName == "thumbnail" && a_name == "url") {
          String localUrl   = SHA1.sha1(a_value);  
          String noticiaUrl = mNoticiaUrl;
          mImages.add( new MobiImage(a_value, localUrl, noticiaUrl) );
          a_value = localUrl; 
        }
          
        mSerializer.attribute(a_uri, a_name, a_value);
      }
      
    } catch (IOException e) {
      //Log.e("ExtractAndBuildParser", e.toString());
    }
    
  } 

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    try {
      mSerializer.endTag("", localName);
    } catch (IOException e) {
      //Log.e("ExtractAndBuildParser", e.toString());
    }
  }
}
