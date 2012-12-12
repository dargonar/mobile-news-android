package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* An enumeration representing a nodeset that is a union of two other NodeSets.
*/

public class UnionEnumeration implements NodeEnumeration {

    private NodeEnumeration p1;
    private NodeEnumeration p2;
    private NodeEnumeration e1;
    private NodeEnumeration e2;
    private NodeInfo nextNode1 = null;
    private NodeInfo nextNode2 = null;
    private long nextKey1 = -1;
    private long nextKey2 = -1;

    public UnionEnumeration(NodeEnumeration p1, NodeEnumeration p2) throws SAXException {
        this.p1 = p1;
        this.p2 = p2;
        e1 = p1;
        e2 = p2;
        if (!e1.isSorted()) {
            e1 = (new NodeSetExtent(e1)).sort().enumerate();
        }
        if (!e2.isSorted()) {
            e2 = (new NodeSetExtent(e2)).sort().enumerate();
        }
        
        if (e1.hasMoreElements()) {
            nextNode1 = e1.nextElement();
            nextKey1 = nextNode1.getSequenceNumber();
        }
        if (e2.hasMoreElements()) {
            nextNode2 = e2.nextElement();
            nextKey2 = nextNode2.getSequenceNumber();
        }            
    }

    public boolean hasMoreElements() throws SAXException {
        return nextNode1!=null || nextNode2!=null;
    }

    public NodeInfo nextElement() throws SAXException {

        // main merge loop: take a value from whichever set has the lower value

        if (nextNode1 != null && nextNode2 != null) {
            long c = nextKey1 - nextKey2;
            if (c<0) {
                NodeInfo next = nextNode1;
                if (e1.hasMoreElements()) {
                    nextNode1 = e1.nextElement();
                    nextKey1 = nextNode1.getSequenceNumber();
                } else {
                    nextNode1 = null;
                }
                return next;
            
            } else if (c>0) {
                NodeInfo next = nextNode2;
                if (e2.hasMoreElements()) {
                    nextNode2 = e2.nextElement();
                    nextKey2 = nextNode2.getSequenceNumber();
                } else {
                    nextNode2 = null;
                }
                return next;
            
            } else {
                NodeInfo next = nextNode2;
                if (e2.hasMoreElements()) {
                    nextNode2 = e2.nextElement();
                    nextKey2 = nextNode2.getSequenceNumber();
                } else {
                    nextNode2 = null;
                }
                if (e1.hasMoreElements()) {
                    nextNode1 = e1.nextElement();
                    nextKey1 = nextNode1.getSequenceNumber();
                } else {
                    nextNode1 = null;
                }
                return next;
            }
        } 

        // collect the remaining nodes from whichever set has a residue
    
        if (nextNode1!=null) {
            NodeInfo next = nextNode1;
            if (e1.hasMoreElements()) {
                nextNode1 = e1.nextElement();
            } else {
                nextNode1 = null;
            }
            return next;
        }
        if (nextNode2!=null) {
            NodeInfo next = nextNode2;
            if (e2.hasMoreElements()) {
                nextNode2 = e2.nextElement();
            } else {
                nextNode2 = null;
            }
            return next;
        }
        return null;
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted() {
        return false;
    }

    public boolean isPeer() {
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
