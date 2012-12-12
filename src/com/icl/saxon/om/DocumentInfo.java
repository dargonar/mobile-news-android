package com.icl.saxon.om;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.KeyManager;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.Context;

import java.util.*;
import java.net.*;
import java.io.Writer;
import org.xml.sax.*;


/**
  * The root node of an XPath tree. (Or equivalently, the tree itself).<P>
  * This class should have been named Root; it is used not only for the root of a document,
  * but also for the root of a result tree fragment, which is not constrained to contain a
  * single top-level element.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 17 June 1999 - changed to an interface
  */

public interface DocumentInfo extends NodeInfo {
    
    /**
    * Get the element with a given ID, if any
    * @param id the required ID value
    * @return the element with the given ID, or null if there is no such ID present (or if the parser
    * has not notified attributes as being of type ID)
    */
    
    public ElementInfo selectID(String id) throws SAXException;

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param absname The absolute name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value. If no index
    * has been built, returns null.
    * @throws SAXExcetpion If the index is under construction, throws an
    * exception, as this implies a key defined in terms of itself.
    */

    public Hashtable getKeyIndex(KeyManager keymanager, String absname) throws SAXException;

    /**
    * Set the index for a given key
    * @param keymanager The key manager managing this key
    * @param absname The absolute name of the key (unique with the key manager)
    * @param index the index, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value; or the string
    * "under construction" to indicate that the index is under construction.
    */

    public void setKeyIndex(KeyManager keymanager, String absname, Object index) throws SAXException;

    /**
    * Get a unique number identifying this document
    */
    
    public int getDocumentNumber();

    /**
    * Set an unparsed entity URI associated with this document
    */

    //public void setUnparsedEntity(String name, String uri);

    /**
    * Get the unparsed entity with a given name
    * @param name the name of the entity
    * @return the URI of the entity if there is one, or null if not
    */

    public String getUnparsedEntity(String name);

    /**
    * Get the URIs of the stylesheets associated with this document by means of an xml-stylesheet
    * processing instruction.
    * @param media The required medium, or null to match any medium
    * @param title The required title, or null to match the preferred stylesheet
    * @return null if there is no such processing instruction
    * @throws SAXException if there is such a processing instruction and it is invalid
    */

    public String[] getAssociatedStylesheets(String media, String title) throws SAXException;

    /**
    * Prepare an embedded stylesheet within this document
    * @param id The id of the required embedded stylesheet
    * @return the prepared Stylesheet if there is one, or null.
    */

    public PreparedStyleSheet getEmbeddedStylesheet(String id) throws SAXException;

    
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
