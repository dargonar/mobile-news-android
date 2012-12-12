package com.diventi.mobipaper.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SectionXmlParser extends DefaultHandler {

  public ArrayList<String> mNames;
  public ArrayList<String> mIds;
  
  private boolean mInItem; 
  private boolean mInTitle; 
  private boolean mInGuid; 
  
  public SectionXmlParser() {
    mNames = new ArrayList<String>();
    mIds   = new ArrayList<String>();
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {

    if( mInItem && mInTitle ) {
      mNames.add( (new String(ch,start,length)).trim() );
      return;
    }

    if( mInItem && mInGuid ) {
      mIds.add( (new String(ch,start,length)).trim() );
      return;
    }
    
  };
  
  
  @Override
  public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {

    if(localName == "item")
      mInItem = true;

    if(localName == "title")
      mInTitle = true;

    if(localName == "guid")
      mInGuid = true;
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {

    if(localName == "item")
      mInItem = false;

    if(localName == "title")
      mInTitle = false;

    if(localName == "guid")
      mInGuid = false;
    
  }

}
