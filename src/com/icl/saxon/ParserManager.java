package com.icl.saxon;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.SecurityException;
import java.lang.ClassCastException;

import java.io.*;
import java.util.*;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;

/**
  * ParserManager is used internally by SAXON to discover the
  * preferred parser and instantiate it. <P>
  *
  * ParserManager reads the file ParserManager.properties to determine
  * which SAX parser and DOM implementation to use. You can edit this
  * configuration file to establish your preferred parser. <P>
  *
  * @author Michael H. Kay (Michael.Kay@icl.com)
  * @version 25 Sep 1998: add facility to select DOM builder
  */

  // It is also used to determine the preferred DOM implementation
  // (function no longer used - commented out).
  
public class ParserManager {

    private static String parserClass;

    /**
    * Read resources from the properties file ParserManager.properties
    */

    private synchronized static ResourceBundle readProperties() throws SAXException {
        try {
            InputStream in =
                ParserManager.class.getResourceAsStream("/ParserManager.properties");
            if (in==null) {
                //System.err.println("Cannot find ParserManager.properties - using built-in parser");
                return null;
            }
            return new PropertyResourceBundle(in);
            
        } catch (MissingResourceException mre) {
            //System.err.println("Cannot find ParserManager.properties - will use built-in parser");
            return null;
        } catch (java.io.IOException e) {
            throw new SAXException("Failure trying to read ParserManager.properties");  
        }
    }

    /**
    * Instantiate a SAX parser. The parser chosen is determined
    * by reading the properties file ParserManager.properties.
    * @return a XMLReader, or null if no parser can be located
    */
        
    public synchronized static XMLReader makeParser () throws SAXException
    {
        String className = "";
        XMLReader parser = null;
        String productName = null;

        if (parserClass!=null) {
            return makeParser(parserClass);
        }
        
        ResourceBundle resources = readProperties();

        if (resources==null) return defaultParser();

        try {            
            productName = resources.getString("defaultParser");
            className = resources.getString(productName);
            parserClass = className;
            parser = makeParser(className);
        } catch (MissingResourceException e) {
            throw new SAXException("Cannot load parser " + className, e);
        }
        
        if (parser != null) return parser;
        
        String parserPath;
        try {            
            parserPath = resources.getString("parserPath");
        }
        catch (MissingResourceException e) {
            throw new SAXException("No parserPath in ParserManager.properties file");
        }

        StringTokenizer path = new StringTokenizer(parserPath, ";");
        while (path.hasMoreTokens()) {
            try {
                productName = path.nextToken();
                className = resources.getString(productName);
                parserClass = className;
                parser = makeParser(className);
                return parser;
            } catch (Exception e2) {
                // try the next one
            }
        }
 
        return defaultParser();
    }

    /**
    * Return the default parser (AElfred)
    */

    public static XMLReader defaultParser() {
        return new com.icl.saxon.aelfred.SAXDriver();
    }
        
  /**
    * Create a new SAX XMLReader object using the class name provided.<br>
    *
    * The named class must exist and must implement the
    * org.xml.sax.XMLReader or Parser interface.<br>
    *
    * This method returns an instance of the parser named.
    *
    * @param className A string containing the name of the
    *   SAX parser class, for example "com.microstar.sax.LarkDriver"
    * @return an instance of the Parser class named, or null if it is not
    * loadable or is not a Parser.
    * 
    */
    public static XMLReader makeParser (String className) throws SAXException
    {
        Object obj = Loader.getInstance(className);
        if (obj instanceof XMLReader) {
            return (XMLReader)obj;
        }        
        if (obj instanceof Parser) {
            return new ParserAdapter((Parser)obj);
        }
        throw new SAXException("Class " + className +
                                 " is neither a SAX1 Parser nor a SAX2 XMLReader");
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
// The Original Code is: all this file, other than fragments copied from the SAX distribution
// made available by David Megginson, and the line marked PB-SYNC.
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved. 
//
// Contributor(s): Michael Kay, Peter Bryant, David Megginson 
//
