package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* An enumeration representing a nodeset that is an intersection of two other NodeSets.
* There is currently no operator in XPath to create such an expression, but it is used
* by the extension function difference(). The code is derived from the analagous UnionEnumeration,
* an inner class of UnionExpression.
*/


public class DifferenceEnumeration implements NodeEnumeration {


    private NodeEnumeration p1;
    private NodeEnumeration p2;
    private NodeEnumeration e1;
    private NodeEnumeration e2;
    private NodeInfo nextNode1 = null;
    private NodeInfo nextNode2 = null;
    private long nextKey1 = -1;
    private long nextKey2 = -1;

    NodeInfo nextNode = null;

    /**
    * Form an enumeration of the difference of two nodesets, that is, the nodes
    * that are in p1 and that are not in p2.
    * @param p1 the first operand
    * @param p2 the second operand
    */

    public DifferenceEnumeration(NodeEnumeration p1, NodeEnumeration p2) throws SAXException {
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
        
        // move to the first node in each input nodeset

        if (e1.hasMoreElements()) {
            nextNode1 = e1.nextElement();
            nextKey1 = nextNode1.getSequenceNumber();
        }
        if (e2.hasMoreElements()) {
            nextNode2 = e2.nextElement();
            nextKey2 = nextNode2.getSequenceNumber();
        }            

        // move to the first node in p1 that isn't in p2
        
        advance();
        
    }
    
    public boolean hasMoreElements() throws SAXException {
        return nextNode!=null;
    }

    public NodeInfo nextElement() throws SAXException {
        NodeInfo current = nextNode;
        advance();
        return current;
    }

    private void advance() throws SAXException {

        // main merge loop: if the node in p1 has a lower key value that that in p2, return it;
        // if they are equal, advance both nodesets; if p1 is higher, advance p2.

        while (nextNode1 != null && nextNode2 != null) {
            long c = nextKey1 - nextKey2;
            if (c<0) {                              // p1 is lower
                NodeInfo next = nextNode1;
                if (e1.hasMoreElements()) {
                    nextNode1 = e1.nextElement();
                    nextKey1 = nextNode1.getSequenceNumber();
                } else {
                    nextNode1 = null;
                    nextNode = null;
                }
                nextNode = next;
                return;
            
            } else if (c>0) {                       // p1 is higher
                NodeInfo next = nextNode2;
                if (e2.hasMoreElements()) {
                    nextNode2 = e2.nextElement();
                    nextKey2 = nextNode2.getSequenceNumber();
                } else {
                    nextNode2 = null;
                    nextNode = null;
                }
            
            } else {                                // keys are equal

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
            }
        } 

        // collect the remaining nodes from the residue of p1
    
        if (nextNode1!=null) {
            nextNode = nextNode1;
            if (e1.hasMoreElements()) {
                nextNode1 = e1.nextElement();
            } else {
                nextNode1 = null;
            }
            return;
        }
        
        nextNode = null;

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
