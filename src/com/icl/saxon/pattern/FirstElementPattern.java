package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeInfo;
import org.xml.sax.SAXException;

/**
* FirstElementPattern is a specialisation of LocationPathPattern to handle the common case
* of a pattern with a single qualifier, the constant integer [1].
*/

public class FirstElementPattern extends LocationPathPattern {

    /**
    * This testFilters() method simply tests whether the node is the first
    * element matching the name test
    */

    protected boolean testFilters(NodeInfo node, Context c) throws SAXException {
        Pattern p = new NamedNodePattern(NodeInfo.ELEMENT, nameTest);
        return node.getPreviousSibling(p, c)==null;
    }

    public String toString() {
        return super.toString() + "[1]";
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
