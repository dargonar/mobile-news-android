package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
* A StaticContext contains the information needed while an expression or patter is being parsed.
*/

public interface StaticContext {

    /**
    * Get the system id (URL) of the container of the expression
    */

    public String getSystemId();

    /**
    * Get the line number of the expression within that container
    * Returns -1 if no line number is available
    */

    public int getLineNumber();

    /**
    * Make an Name, using this Element as the context for namespace resolution
    * @param tag The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    */

    public Name makeName(String tag, boolean useDefault) throws SAXException;

    /**
    * Make a NameTest object for a prefix:* wildcard
    */

    public NameTest makePrefixTest(String wildcard) throws SAXException;

    /**
    * Bind a variable to an object that can be used to refer to it
    * @return a Binding object that can be used to identify it in the Bindery
    * @throws SAXException if the variable has not been declared, or if the context
    * does not allow the use of variables
    */

    public Binding bindVariable(String name) throws SAXException;    

    /**
    * Determine whether a given URI identifies an extension element namespace
    */

    public boolean isExtensionNamespace(String uri) throws SAXException;

    /**
    * Determine whether forwards-compatible mode is enabled
    */

    public boolean forwardsCompatibleModeIsEnabled() throws SAXException;

    /**
    * Bind an XSLT function name: return null if not found
    */

    public Function getStyleSheetFunction(Name name) throws SAXException;
    
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
