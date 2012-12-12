package com.icl.saxon;
import org.xml.sax.*;
import java.util.*;
import java.net.*;
import com.icl.saxon.trax.URIResolver;
import com.icl.saxon.trax.TransformException;
import org.w3c.dom.Node;

/**
* This class provides the service of converting a URI into an InputSource.
* It is used to get stylesheet modules referenced by xsl:import and xsl:include,
* and source documents referenced by the document() function. The standard version
* handles anything that the java URL class will handle. 
* You can write a subclass to handle other kinds of URI, e.g. references to things in
* a database.
* @author Michael H. Kay
*/

public class StandardURIResolver implements URIResolver {

    private URL theURL;
    private String parserClass;

    /**
    * Set the name of the class to be used for parsing
    */

    public void setParserClass(String name) {
        parserClass = name;
    }

    /**
    * Set a relative URI, given a base URI to resolve it
    * @param baseURI The base URI that should be used. May be null if uri is absolute.
    * @params uri The relative or absolute URI. May be an empty string.
    */

    public void setURI(String baseURI, String uri)
        throws TransformException {
      
        try {
            if (baseURI==null || baseURI == "[unidentified data stream]") {
                theURL = new URL(uri);
            } else {        
                URL baseURL = toURL(baseURI);
                theURL = (uri.length()==0 ?
                                 baseURL :
                                 toURL(baseURL, uri)
                             );
            }
        } catch (java.net.MalformedURLException err) {
            throw new TransformException("Malformed URL " + uri, err);
        }
    }

    /**
    * Get the absolute URI
    */

    public String getURI() {
        return theURL.toString();
    }

    /**
    * This will be called by the processor when it encounters 
    * an xsl:include, xsl:import, or document() function, if it needs 
    * a DOM tree.
    *
    * This method is never called by Saxon.
    * 
    * @param base The base URI that should be used.
    * @param uri Value from an xsl:import or xsl:include's href attribute, 
    * or a URI specified in the document() function.
    * @returns null (always). 
    */

    public Node getDOMNode() {
        return null;
    }

    /**
    * Return an InputSource corresponding to the URL
    */

    public InputSource getInputSource() {
        return new InputSource(theURL.toString());
    }

    /**
    * Return a SAX2 Parser to be used with this InputSource.
    * @return an instance of the class supplied to setParserClass(), if called
    */

    public XMLReader getXMLReader() throws TransformException {
        try {
            if (parserClass==null) {
                return null;
            } else {
                return ParserManager.makeParser(parserClass);
            }
        } catch (SAXException err) {
            throw new TransformException(err);
        }
    }

    /**
    * Parse an absolute URI
    */

    protected static URL toURL(String systemID) throws TransformException {
        URL url;
        try {
            url = new URL(systemID);
        } catch (MalformedURLException err) {
            throw new TransformException("Malformed URL " + systemID);
        }
        return url;
    }

    /**
    * Parse a relative URI
    */

    protected static URL toURL(URL baseURL, String systemID) throws TransformException {
        URL url;
        try {
            url = new URL(baseURL, systemID);
        } catch (MalformedURLException err) {
            throw new TransformException("Malformed URL " + systemID);
        }
        return url;
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
