package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
* SingletonEnumeration: an enumeration of zero or one nodes
*/

public class SingletonEnumeration implements NodeEnumeration, LastPositionFinder {

    private NodeInfo theNode;
    private boolean gone;
    private int count;
    
    public SingletonEnumeration(NodeInfo node) {
        theNode = node;
        gone = (node==null);
        count = (node==null ? 0 : 1);
    }

    /**
    * Get another enumeration which will return the same nodes as the original. The new
    * enumeration will be positioned at the start of the sequence, regardless where the
    * original enumeration is positioned.
    */

    public NodeEnumeration getAnother() throws SAXException {
        return new SingletonEnumeration(theNode);
    }

    public boolean hasMoreElements() {
        return !gone;
    }

    public NodeInfo nextElement() {
        gone = true;
        return theNode;
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted() {
        return true;
    }

    public boolean isPeer() {
        return true;           
    }

    public int getLastPosition() {
        return count;
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
