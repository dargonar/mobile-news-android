package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;

import org.xml.sax.SAXException;

/**
* A DocumentPattern is a pattern that matches only the root node of a document
*/

public class DocumentPattern extends Pattern {

    /**
    * Determine whether a node matches the pattern
    */

    public boolean matches(NodeInfo node, Context c) throws SAXException {
        return (node instanceof DocumentInfo);
    }

    /**
    * Determine the type of nodes to which this pattern applies. 
    * @return NodeInfo.DOCUMENT 
    */

    public int getType() {
        return NodeInfo.DOCUMENT;
    }
    
    /**
    * If this pattern will match only nodes of a single name, return the relevant node name.
    * This is used for quick elimination of patterns that will never match.
    */

    public Name getName() {
        return null;
    }               

    /**
    * Return the pattern as a string
    */

    public String toString() {
        return "/";
    }

    /**
    * Determine if the pattern uses positional filters
    * @return false always
    */

    public boolean isRelative() {
        return false;
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
