package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.Vector;

/**
* A node-set value no nodes
*/

public final class EmptyNodeSet extends NodeSetValue {

    private static Vector emptyVector = new Vector(0);
    private static NodeInfo[] emptyArray = new NodeInfo[0];

    /**
    * Evaluate the Node Set. This guarantees to return the result in sorted order.
    * @param context The context for evaluation (not used)
    */

    public Value evaluate(Context context) throws SAXException {
        return this;
    }
    
    /**
    * Evaluate an expression as a NodeSet. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws SAXException {
        return this;
    }  

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {}
    
    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() {
        return true;
    }
    
    /**
    * Convert to string value
    * @return an empty string
    */

    public String asString() throws SAXException {
        return "";
    }

    /**
    * Evaluate as a boolean.
    * @return false
    */

    public boolean asBoolean() throws SAXException {
        return false;
    }

    /**
    * Count the nodes in the node-set.
    * @return zero
    */

    public int getCount() throws SAXException {
        return 0;
    }

    /**
    * Determine whether the node-set is empty.
    * @return true
    */

    public boolean isEmpty() throws SAXException {
        return true;
    }

    /**
    * Determine whether the node-set is singular, that is, whether it has a single member.
    * @return false
    */

    public boolean isSingular() throws SAXException {
        return false;
    }
    
    /**
    * Determine whether a particular node is present in the nodeset.
    * @return false
    */

    public boolean contains (NodeInfo node) throws SAXException {
        return false;
    }

    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return a Vector containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public Vector getVector() throws SAXException {
        return emptyVector;
    }

    /**
    * Return the nodes in the node-set as an array. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return an empty array 
    */

    public NodeInfo[] getNodes() throws SAXException {
        return emptyArray;
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (Historic)
    */

    public NodeSetValue sort() {
        return this;
    }
    
    /**
    * Get the first node in the nodeset (in document order)
    * @return null
    */

    public NodeInfo getFirst() {
        return null;
    }
        

    /**
    * Test whether this nodeset "equals" another Value
    */

    public boolean equals(Value other) throws SAXException {
        if (other instanceof BooleanValue) {
            return !other.asBoolean();
        } else {
            return false;
        }
    }

    /**
    * Test whether this nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws SAXException {                
        if (other instanceof BooleanValue) {
            return other.asBoolean();
        } else {
            return false;
        }
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws SAXException {
        return new SingletonEnumeration(null);
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

