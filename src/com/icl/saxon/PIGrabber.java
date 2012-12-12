package com.icl.saxon;
import com.icl.saxon.om.ProcInstParser;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.net.URL;
import java.util.Vector;

/**
  * The PIGrabber class is a SAX ContentHandler that looks for xml-stylesheet processing
  * instructions and tests whether they match specified criteria; for those that do, it creates
  * an InputSource object referring to the relevant stylesheet
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class PIGrabber extends DefaultHandler {

    private String reqMedia = null;
    private String reqTitle = null;
    private String baseURI = null;
    private Vector stylesheets = new Vector();

    public void setCriteria(String media, String title, String charset) {
        this.reqMedia = media;
        this.reqTitle = title;
    }

    public void setBaseURI(String uri) {
        baseURI = uri;
    }

    /**
    * Abort the parse when the first start element tag is found
    */

    public void startElement (String uri, String localName,
			      String qName, Attributes attributes) throws SAXException {

	    // abort the parse when the first start element tag is found
        throw new SAXException("#start#");
    }

    /**
    * Handle xml-stylesheet PI
    */

    public void processingInstruction (String target, String data)
	throws SAXException
    {
        if (target.equals("xml-stylesheet")) {

            String piMedia = ProcInstParser.getPseudoAttribute(data, "media");
            String piTitle = ProcInstParser.getPseudoAttribute(data, "title");
            String piType = ProcInstParser.getPseudoAttribute(data, "type");
            String piAlternate = ProcInstParser.getPseudoAttribute(data, "alternate");

            if ( (piType.equals("text/xml") || piType.equals("application/xml") ||
                    piType.equals("text/xsl") || piType.equals("applicaton/xsl")) &&
                    
                    (reqMedia==null || piMedia==null || reqMedia.equals(piMedia)) &&

                    ( ( piTitle==null && (piAlternate==null || piAlternate.equals("no"))) ||
                      ( reqTitle==null ) ||
                      ( piTitle!=null && piTitle.equals(reqTitle) ) ) )
            {
                String href = ProcInstParser.getPseudoAttribute(data, "href");
                if (href==null) {
                    throw new SAXException("xml-stylesheet PI has no href attribute");
                }
                if (piTitle==null && (piAlternate==null || piAlternate.equals("no"))) {
                    stylesheets.insertElementAt(href, 0);
                } else {
                    stylesheets.addElement(href);
                }
            }
        }
    }

    /**
    * Return list of stylesheets that matched, as an array of InputSource objects
    * @return null if there were no matching stylesheets.
    * @throws SAXException if any stylesheet includes a fragment identifier, as
    * this cannot be mapped to an InputSource, or if a URI cannot be resolved
    */

    public InputSource[] getAssociatedStylesheets() throws SAXException {
        if (stylesheets.size()==0) {
            return null;
        }
        InputSource[] result = new InputSource[stylesheets.size()];
        for (int i=0; i<stylesheets.size(); i++) {
            String href = (String)stylesheets.elementAt(i);
            if (href.indexOf('#')>=0) {
                throw new SAXException("xml-stylesheet PI href attribute may not contain fragment identifier");
            }
            URL ss;
            try {
                ss = new URL(new URL(baseURI), href);
            } catch (java.net.MalformedURLException err) {
                throw new SAXException("Cannot resolve URL in xml-stylesheet PI: " + href, err);
            }
            result[i] = new InputSource(ss.toString());
        }
        return result;
    }

    /**
    * Get the stylesheet URIs as an array of Strings
    */

    public String[] getStylesheetURIs() throws SAXException {
        if (stylesheets.size()==0) {
            return null;
        }
        String[] result = new String[stylesheets.size()];
        for (int i=0; i<stylesheets.size(); i++) {
            result[i] = (String)stylesheets.elementAt(i);
        }
        return result;
    }    

}   
//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/ 
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License. 
//
// The Original Code is: all this file. 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved. 
//
// Contributor(s): none. 
//
