package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* An IDPattern is a pattern of the form id(literal)
*/

public class IDPattern extends Pattern {

    private String id;                      // the id value supplied

    public IDPattern(String idvalue) {
        id = idvalue;
    }

    /**
    * Determine whether this Pattern matches the given Node
    * @param e The NodeInfo representing the Element or other node to be tested against the Pattern
    * @return true if the node matches the Pattern, false otherwise
    */

    public boolean matches(NodeInfo e, Context c) throws SAXException {
        if (!(e instanceof ElementInfo)) return false;
        DocumentInfo doc = e.getDocumentRoot();
        return (doc.selectID(id)==e);
    }
        
    /**
    * Determine if the pattern uses positional filters
    * @return false (always)
    */

    public boolean isRelative() {
        return false;
    }

    /**
    * Return the pattern as a string (for diagnostic output)
    */

    public String toString() {
        return "id(\'" + id + "\')";
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
