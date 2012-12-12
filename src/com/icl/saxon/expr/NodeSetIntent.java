package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

//import com.icl.saxon.sort.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A node-set value implemented intensionally. It is a wrapper round an Expression which
* can be evaluated independently of context, that is it has been reduced so there are
* no remaining context-dependencies. The first time the expression is evaluated, it is
* replaced with its (extensional) value.
*/

public class NodeSetIntent extends NodeSetValue {
    private NodeSetExpression expression;
    private NodeSetExtent extent = null;
    private boolean sorted = false;

    /**
    * Construct a node-set containing all the nodes in a NodeEnumeration
    */

    public NodeSetIntent(NodeSetExpression exp) throws SAXException {
        if (exp.getDependencies()!=0) {
            throw new SAXException("Cannot create intensional node-set with context dependencies: " + exp.getClass() + ":" + exp.getDependencies());
        }
        expression = exp;
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes will be delivered
    * in document order and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {
        sorted = isSorted;
    }

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() throws SAXException {
        return (sorted || expression.enumerate(null).isSorted());
    }
    
    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() throws SAXException {
        NodeInfo first = getFirst();
        return (first==null ? "" : first.getValue());
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() throws SAXException {
        if (extent == null) {
            NodeEnumeration enumeration = expression.enumerate(null);
            if (enumeration instanceof LastPositionFinder && enumeration.isSorted()) {
                return ((LastPositionFinder)enumeration).getLastPosition();
            } 
            extent = new NodeSetExtent(enumeration);
        }
        return extent.getCount();
    }

    /**
    * Determine whether the node-set is empty. 
    */

    public boolean isEmpty() throws SAXException {
        if (extent != null) return extent.isEmpty();
        NodeEnumeration enumeration = expression.enumerate(null);
        return !enumeration.hasMoreElements();
    }
   
    /**
    * Determine whether a particular node is present in the nodeset.
    */

    public boolean contains (NodeInfo node) throws SAXException {        
        fix();
        return extent.contains(node);
    }

    private void fix()  throws SAXException {
        if (extent == null) {
            NodeEnumeration enumeration = expression.enumerate(null);
            extent = new NodeSetExtent(enumeration);
        }
    }

    
    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return a Vector containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    * @deprecated It is more efficient to retrieve the nodes as an array
    */

    public Vector getVector() throws SAXException {
        fix();
        return extent.getVector();
    }

    /**
    * Return the nodes in the node-set as an array. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return an array containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public NodeInfo[] getNodes() throws SAXException {
        fix();
        return extent.getNodes();
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. 
    */

    public NodeSetValue sort() throws SAXException {
        if (sorted) return this;
        fix();
        return extent.sort();
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public NodeInfo getFirst() throws SAXException {
        if (extent!=null) return extent.getFirst();

        NodeEnumeration enumeration = expression.enumerate(null);
        if (sorted || enumeration.isSorted()) {
            sorted = true;            
            if (enumeration.hasMoreElements()) {
                return enumeration.nextElement();
            }
            return null;
        } else {
            NodeInfo first = null;
            long minseq = Long.MAX_VALUE;
            while (enumeration.hasMoreElements()) {
                NodeInfo node = enumeration.nextElement();
                long seq = node.getSequenceNumber();
                if (seq < minseq) {
                    first = node;
                    minseq = seq;
                }
            }
            return first;
        }
    }

    /**
    * Return the first node in the nodeset (in document order)
    * @param context The context for the evaluation: not used
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context) throws SAXException {
        return getFirst();
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws SAXException {
        if (extent!=null) {
            return extent.enumerate();
        } else {
            return expression.enumerate(null);
        }
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

