package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
* A DummyStaticContext provides a minimal context for parsing an expression or pattern
*/

public class DummyStaticContext implements StaticContext {

    /**
    * Get the system id (URL) of the container of the expression
    */

    public String getSystemId() {
        return null;
    }

    /**
    * Get the line number of the expression within that container
    * Returns -1 if no line number is available
    */

    public int getLineNumber() {
        return -1;
    }

    /**
    * Make a Name object from a [prefix:]name source tag
    */

    public Name makeName(String tag, boolean useDefault) throws SAXException {
        int colon = tag.indexOf(":");
        if (colon < 0) {
            return new Name(tag);
        } else if (colon==0) {
            throw new SAXException("Name " + tag + " cannot start with a colon");
        } else if (colon==tag.length()-1) {
            throw new SAXException("Name " + tag + " cannot end with a colon");
        } else {
            String prefix = tag.substring(0, colon).intern();
            String localName = tag.substring(colon+1).intern();
            String uri = getURIforPrefix(prefix).intern();
            return new Name(prefix, uri, localName);
        }
    }


    /**
    * Make a NameTest object for an Element from a prefix:* wildcard
    */

    public NameTest makePrefixTest(String wildcard) throws SAXException {
        throw new SAXException("Wildcard tests not supported in this context");
    }
    
   /**
    * Search the NamespaceList for a given prefix, returning the corresponding URI.
    * @param prefix The prefix to be matched. To find the default namespace, supply ""
    * @return The URI corresponding to this namespace. If it is an unnamed default namespace,
    * return "".
    * @throws SAXException if the prefix has not been declared on this element or a containing
    * element.
    */

    private String getURIforPrefix(String prefix) throws SAXException {
        if (prefix.equals("xml")) return Namespace.XML;
        if (prefix.equals("xsl")) return Namespace.XSLT;
        if (prefix.equals("saxon")) return Namespace.SAXON;
        throw new SAXException("No context exists for resolving namespace prefix " + prefix);
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    */

    public Binding bindVariable(String name) throws SAXException {
        throw new SAXException("Variables are not allowed in this context");
    }

    /**
    * Determine whether a given URI identifies an extension element namespace
    */

    public boolean isExtensionNamespace(String uri) {
        return false;
    }

    /**
    * Determine whether forwards-compatible mode is enabled
    */

    public boolean forwardsCompatibleModeIsEnabled() {
        return false;
    }

    /**
    * Bind an XSLT function name: return null if not found
    */

    public Function getStyleSheetFunction(Name name) throws SAXException {
        return null;
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
