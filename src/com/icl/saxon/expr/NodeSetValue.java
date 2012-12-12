package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A node-set value. We use this both for node-sets and node-lists. The node set will only
* be sorted into document order when requested (using sort() or evaluate()). This is an abstract
* class with a number of concrete implementations including NodeSetExtent (for extensional node-sets)
* and NodeSetIntent (for intensional node-sets).
*/

public abstract class NodeSetValue extends Value {

    /**
    * Evaluate the Node Set. This guarantees to return the result in sorted order.
    * @param context The context for evaluation (not used)
    */

    public Value evaluate(Context context) throws SAXException {
        sort();
        return this;
    }
    
    /**
    * Evaluate an expression as a NodeSet. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws SAXException {
        sort();
        return this;
    }  

    /**
    * Return an enumeration of this nodeset value.
    */

    public abstract NodeEnumeration enumerate() throws SAXException;

    /**
    * Return an enumeration of this nodeset value. This is to satisfy the interface for
    * Expression. The context is ignored.
    */

    public NodeEnumeration enumerate(Context c) throws SAXException {
        return enumerate();
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public abstract void setSorted(boolean isSorted);
    
    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public abstract boolean isSorted() throws SAXException;
    
    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public abstract String asString() throws SAXException;

    /**
    * Evaluate as a number.
    * @return the number obtained by evaluating as a String and converting the string to a number
    */

    public double asNumber() throws SAXException {
        return (new StringValue(asString())).asNumber();
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public boolean asBoolean() throws SAXException {
        return !isEmpty();
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public abstract int getCount() throws SAXException;

    /**
    * Determine whether the node-set is empty. This is more efficient than testing getCount()==0,
    * because it doesn't risk triggering a sort.
    */

    public abstract boolean isEmpty() throws SAXException;

    /**
    * Determine whether the node-set is singular, that is, whether it has a single member.
    * This is more efficient that testing getCount()==1, because it doesn't risk triggering a sort.
    */

    public boolean isSingular() throws SAXException {
        return getCount()==1;
    }
    
    /**
    * Determine whether a particular node is present in the nodeset.
    */

    public abstract boolean contains (NodeInfo node) throws SAXException;

    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return a Vector containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public abstract Vector getVector() throws SAXException;

    /**
    * Return the nodes in the node-set as an array. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return an array containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public abstract NodeInfo[] getNodes() throws SAXException;

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (The reason for returning this is that
    * it makes life easier for the XSL compiler).
    */

    public abstract NodeSetValue sort() throws SAXException;
    
    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public abstract NodeInfo getFirst() throws SAXException;

    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws SAXException {
                
        if (other instanceof NodeSetValue) {

            // see if there is a node in A with the same string value as a node in B
            
            NodeEnumeration e1 = this.enumerate();
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            Hashtable table = new Hashtable();
            while (e1.hasMoreElements()) {
                table.put(e1.nextElement().getValue(), "x");
            }
            while (e2.hasMoreElements()) {
                if (table.get(e2.nextElement().getValue())!=null) return true;
            }
            return false;

        } else {
            if (other instanceof NumericValue) {
                NodeEnumeration e1 = this.enumerate();
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (Value.stringToNumber(node.getValue())==other.asNumber()) return true;
                }
                return false;
                
            } else if (other instanceof StringValue) {
                NodeEnumeration e1 = this.enumerate();
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (node.getValue().equals(other.asString())) return true;
                }
                return false;
                
            } else if (other instanceof BooleanValue) {
                                // fix bug 4.5/010
                return (asBoolean()==other.asBoolean());
                
            } else if (other instanceof FragmentValue || other instanceof ObjectValue) {
                return equals(new StringValue(other.asString()));
                
            } else {
                throw new SAXException("Unknown data type in a relational expression");
            }
        }
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws SAXException {
                
        if (other instanceof NodeSetValue) {

            // see if there is a node in A with a different string value as a node in B
            // use a nested loop: it will usually finish very quickly!

            NodeEnumeration e1 = this.enumerate();            
            while (e1.hasMoreElements()) {
                String s1 = e1.nextElement().getValue();
                NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
                while (e2.hasMoreElements()) {
                    String s2 = e2.nextElement().getValue();
                    if (!s1.equals(s2)) return true;
                }
            }
            return false;

        } else {
            if (other instanceof NumericValue) {
                NodeEnumeration e1 = this.enumerate();            
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (Value.stringToNumber(node.getValue())!=other.asNumber()) return true;
                }
                return false;
            } else if (other instanceof StringValue) {
                NodeEnumeration e1 = this.enumerate();            
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (!(node.getValue().equals(other.asString()))) return true;
                }
                return false;
            } else if (other instanceof BooleanValue) {
                // bug 4.5/010
                return (asBoolean()!=other.asBoolean());
            } else if (other instanceof FragmentValue || other instanceof ObjectValue) {
                return notEquals(new StringValue(other.asString())); 
            } else {
                throw new SAXException("Unknown data type in a relational expression");
            }
        }
    }

    /**
    * Test how a nodeset compares to another Value under a relational comparison
    * @param operator The comparison operator, one of Tokenizer.LE, Tokenizer.LT,
    * Tokenizer.GE, Tokenizer.GT, 
    */

    public boolean compare(int operator, Value other) throws SAXException {
        if (operator==Tokenizer.EQUALS) return equals(other);
        if (operator==Tokenizer.NE) return notEquals(other);
        
        if (other instanceof NodeSetValue) {

            // find the min and max values in this nodeset
        
            double thismax = Double.NEGATIVE_INFINITY;
            double thismin = Double.POSITIVE_INFINITY;
            boolean thisIsEmpty = true;

            NodeEnumeration e1 = enumerate();
            while (e1.hasMoreElements()) {
                double val = Value.stringToNumber(e1.nextElement().getValue());
                if (val<thismin) thismin = val;
                if (val>thismax) thismax = val;
                thisIsEmpty = false;            
            }
        
            if (thisIsEmpty) return false;
                        
            // find the minimum and maximum values in the other nodeset

            double othermax = Double.NEGATIVE_INFINITY;
            double othermin = Double.POSITIVE_INFINITY;
            boolean otherIsEmpty = true;
            
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                double val = Value.stringToNumber(e2.nextElement().getValue());
                if (val<othermin) othermin = val;
                if (val>othermax) othermax = val;
                otherIsEmpty = false;
            }

            if (otherIsEmpty) return false;

            switch(operator) {
                case Tokenizer.LT:
                    return thismin < othermax;
                case Tokenizer.LE:
                    return thismin <= othermax;                    
                case Tokenizer.GT:
                    return thismax > othermin;
                case Tokenizer.GE:
                    return thismax >= othermin;
                default:
                    return false;
            }

        } else {
            if (other instanceof NumericValue || other instanceof StringValue) {
                NodeEnumeration e1 = enumerate();
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (numericCompare(operator,
                                 Value.stringToNumber(node.getValue()),
                                 other.asNumber()))
                        return true;
                }
                return false;
            } else if (other instanceof BooleanValue) {
                return numericCompare(operator,
                                    new BooleanValue(this.asBoolean()).asNumber(),
                                    new BooleanValue(other.asBoolean()).asNumber());
            } else if (other instanceof FragmentValue || other instanceof ObjectValue) {
                return compare(operator, new StringValue(other.asString())); 
            } else {
                throw new SAXException("Unknown data type in a relational expression");
            }
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

