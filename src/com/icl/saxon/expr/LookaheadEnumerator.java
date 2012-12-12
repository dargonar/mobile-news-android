package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A LookaheadEnumerator passes the nodes from a base enumerator throgh unchanged.
* The complication is that on request, it must determine the value of the last() position,
* which requires a lookahead.
*
* A LookaheadEnumerator should only be used to wrap a NodeEnumeration that cannot
* determine the last() position for itself, i.e. one that is not a LastPositionFinder.
*/


public class LookaheadEnumerator implements NodeEnumeration, LastPositionFinder {

    // The way this class works is that all calls to hasMoreElements() and nextElement() are
    // simply delegated to the underlying enumeration, until such time as the client calls
    // getLastPosition() to find out how many nodes there are. At this point the remaining nodes
    // are read from the underlying enumeration into a reservoir to find out how many there are;
    // and from this point on, requests for more nodes are met from the reservoir rather than
    // from the underlying enumeration. The reason for all this is to avoid allocating temporary
    // storage for the nodes unless the user actually calls last() to find out how many there are.


    private NodeEnumeration base;
    private Vector reservoir = null;
    private int reservoirPosition = -1;
    private int position = 0;
    private int last = -1;

    /**
    * Constructor
    * @param base An NodeEnumerator that delivers the nodes, but that cannot determine the
    * last position count.
    */

    public LookaheadEnumerator(NodeEnumeration base) throws SAXException {
        this.base = base;
    }

    /**
    * Determine whether there are any more nodes to hand to the client
    */

    public boolean hasMoreElements() throws SAXException {
        if (reservoir==null) {
            return base.hasMoreElements();
        } else {
            return reservoirPosition < reservoir.size();
        }
    }

    /**
    * Hand the next node to the client
    */

    public NodeInfo nextElement() throws SAXException {
        if (reservoir==null) {
            position++;
            return base.nextElement();
        } else {
            if (reservoirPosition<reservoir.size()) {
                position++;
                return (NodeInfo)reservoir.elementAt(reservoirPosition++);
            } else {
                return null;
            }
        }
    }

    /**
    * Do lookahead to find the last position, if required
    */

    public int getLastPosition() throws SAXException {
        if (last>0) {
            return last;
        } else {
            // load the reservoir with all remaining input nodes
            reservoir = new Vector();
            reservoirPosition = 0;
            last = position;
            while (base.hasMoreElements()) {
                reservoir.addElement(base.nextElement());
                last++;
            }
            return last;
        }
    }

    /**
    * Determine whether the nodes are guaranteed to be in document order
    */

    public boolean isSorted() throws SAXException {
        return base.isSorted();
    }

    public boolean isReverseSorted() throws SAXException {
        return base.isReverseSorted();
    }


    /**
    * Determine whether the nodes are guaranteed to be peers
    */

    public boolean isPeer() throws SAXException {
        return base.isPeer();
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
