package com.icl.saxon.pattern;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A KeyPattern is a pattern of the form key(keyname, keyvalue)
*/

public class KeyPattern extends Pattern {

    private String keyname;                 // the name of the key
    private String keyvalue;                // the value of the key

    /**
    * Constructor
    * @param name the name of the key
    * @param value the value of the key
    */

    public KeyPattern(String name, String value) {
        keyname = name;
        keyvalue = value;
    }

    /**
    * Determine whether this Pattern matches the given Node.
    * Note that it might match different nodes in different source documents!
    * @param e The NodeInfo representing the Element or other node to be tested against the Pattern
    * @return true if the node matches the Pattern, false otherwise
    */

    public boolean matches(NodeInfo e, Context c) throws SAXException {
        DocumentInfo doc = e.getDocumentRoot();
        Controller controller = c.getController();
        KeyManager km = controller.getKeyManager();
        Vector nodes = km.selectByKey(keyname, doc, keyvalue, controller);
        return nodes.contains(e);
    }

    /**
    * Determine if the pattern uses positional filters
    * @return false (always)
    */

    public boolean isRelative() {
        return false;
    }

    /**
    * Return pattern as a string (for diagnostic output)
    */

    public String toString() {
        return "key(\'" + keyname + "\', \'" + keyvalue + "\')";
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
