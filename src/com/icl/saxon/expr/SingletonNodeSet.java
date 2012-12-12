package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A node-set value containing zero or one nodes
*/

public class SingletonNodeSet extends NodeSetValue {

    protected NodeInfo node = null;

    /**
    * Create an empty node-set
    */

    public SingletonNodeSet() {
        node = null;
    }

    /**
    * Create a node-set containing one node
    */

    public SingletonNodeSet(NodeInfo node) {
        this.node = node;
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() throws SAXException {
        if (node==null) {
            return new EmptyNodeSet();
        } else {
            return this;
        }
    }

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
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() throws SAXException {
        if (node==null) {
            return "";
        } else {
            return node.getValue();
        }
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public boolean asBoolean() throws SAXException {
        return node!=null;
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() throws SAXException {
        return (node==null ? 0 : 1);
    }

    /**
    * Determine whether the node-set is empty. This is more efficient than testing getCount()==0,
    * because it doesn't risk triggering a sort.
    */

    public boolean isEmpty() throws SAXException {
        return node==null;
    }

    /**
    * Determine whether the node-set is singular, that is, whether it has a single member.
    * This is more efficient that testing getCount()==1, because it doesn't risk triggering a sort.
    */

    public boolean isSingular() throws SAXException {
        return node!=null;
    }
    
    /**
    * Determine whether a particular node is present in the nodeset.
    */

    public boolean contains (NodeInfo node) throws SAXException {
        return node==this.node;
    }

    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return a Vector containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public Vector getVector() throws SAXException {
        Vector v = new Vector();
        if (node!=null) {
            v.addElement(node);
        }
        return v;
    }

    /**
    * Return the nodes in the node-set as an array. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return an array containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public NodeInfo[] getNodes() throws SAXException {
        if (node==null) {
            return new NodeInfo[0];
        } else {
            NodeInfo[] nodes = new NodeInfo[1];
            nodes[0] = node;
            return nodes;
        }
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
    * @return the first node
    */

    public NodeInfo getFirst() throws SAXException {
        return node;
    }
        

    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws SAXException {

        if (node==null) {
            if (other instanceof BooleanValue) {
                return !other.asBoolean();
            } else {
                return false;
            }
        }

        if (other instanceof NodeSetValue) {

            // see if there is a node in A with the same string value as a node in B
            
            String value = node.getValue();
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                if (e2.nextElement().getValue().equals(value)) return true;
            }
            return false;

        } else if (other instanceof NumericValue) {
                 return Value.stringToNumber(node.getValue())==other.asNumber();
                
        } else if (other instanceof StringValue) {
                 return node.getValue().equals(other.asString());

        } else if (other instanceof BooleanValue) {                                
                 return other.asBoolean();
                
        } else if (other instanceof FragmentValue || other instanceof ObjectValue) {
                return equals(new StringValue(other.asString()));
                
        } else {
                throw new SAXException("Unknown data type in a relational expression");
        }
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws SAXException {
                
        if (node==null) {
            if (other instanceof BooleanValue) {
                return other.asBoolean();
            } else {
                return false;
            }
        }
                
        if (other instanceof NodeSetValue) {

            String value = node.getValue();
            
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                if (!e2.nextElement().getValue().equals(value)) return true;
            }
            return false;

        } else if (other instanceof NumericValue) {
             return Value.stringToNumber(node.getValue())!=other.asNumber();
                
        } else if (other instanceof StringValue) {
             return !node.getValue().equals(other.asString());

        } else if (other instanceof BooleanValue) {                                
             return !other.asBoolean();
                
        } else if (other instanceof FragmentValue || other instanceof ObjectValue) {
            return !equals(new StringValue(other.asString()));
                
        } else {
                throw new SAXException("Unknown data type in a relational expression");

        }
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws SAXException {
        return new SingletonEnumeration(node);
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

