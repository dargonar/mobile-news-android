package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.sort.*;
import org.xml.sax.SAXException;
import java.util.*;
import org.w3c.dom.*;

/**
* A node-set value implemented extensionally.
*/

public class NodeSetExtent extends NodeSetValue implements org.w3c.dom.NodeList {
    private NodeInfo[] value;
    private int length;
    private boolean sorted;     // true only if values are known to be in document order
    private boolean reverseSorted;  // true if known to be in reverse document order

    /**
    * Default constructor creates an empty node set
    */

    public NodeSetExtent() {
        this.value = new NodeInfo[0];
        length = 0;
        sorted = true;
        reverseSorted = true;
    }

    /**
    * Construct a node-set given the set of nodes as an array
    * @param nodes An array whose elements must be NodeInfo objects
    * @param length The number of significant elements in the array; any excess is ignored
    */

    public NodeSetExtent(NodeInfo[] nodes) {
        this.value = nodes;
        this.length = nodes.length;
        sorted = length<2;
        reverseSorted = length<2;
    }


    /**
    * Construct a node-set given the set of nodes as a Vector
    * @param nodes a Vector whose elements must be NodeInfo objects
    * @deprecated It is more efficient to supply an array NodeInfo[]
    */

    public NodeSetExtent(Vector nodes) {
        value = new NodeInfo[nodes.size()];
        for (int i=0; i<nodes.size(); i++) {
            value[i] = (NodeInfo)nodes.elementAt(i);
        }
        length = nodes.size();
        sorted = length<2;
        reverseSorted = length<2;
    }

    /**
    * Construct a node-set containing all the nodes in a NodeEnumeration
    */

    public NodeSetExtent(NodeEnumeration enuma) throws SAXException {
        int size;
        if (enuma instanceof LastPositionFinder) {
            size = ((LastPositionFinder)enuma).getLastPosition();
        } else {
            size = 20;
        }
        value = new NodeInfo[size];
        int i = 0;
        while (enuma.hasMoreElements()) {
            if (i>=size) {
                size *= 2;
                NodeInfo newarray[] = new NodeInfo[size];
                System.arraycopy(value, 0, newarray, 0, i);
                value = newarray;
            }
            value[i++] = enuma.nextElement();
        }
        sorted = enuma.isSorted() || i<2;
        reverseSorted = enuma.isReverseSorted() || i<2;
        length = i;
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() throws SAXException {
        if (length==0) {
            return new EmptyNodeSet();
        } else if (length==1) {
            return new SingletonNodeSet(value[0]);
        } else {
            return this;
        }
    }


    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {
        sorted = isSorted;
    }

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() {
        return sorted;
    }
    
    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() throws SAXException {
        return (length>0 ? getFirst().getValue() : "");
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() throws SAXException {
        sort();
        return length;
    }

    /**
    * Determine whether the node-set is empty. This is more efficient than testing getCount()==0,
    * because it doesn't risk triggering a sort.
    */

    public boolean isEmpty() throws SAXException {
        return length==0;
    }

    /**
    * Determine whether the node-set is singular, that is, whether it has a single member.
    * This is more efficient that testing getCount()==1, because it doesn't risk triggering a sort.
    */

    public boolean isSingular() {
        return length==1;
    }
    

    /**
    * Determine whether a particular node is present in the nodeset.
    */

    public boolean contains (NodeInfo node) {
        for (int i=0; i<length; i++) {
            if (value[i]==node) return true;
        }
        return false;
    }

    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return a Vector containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    * @deprecated It is more efficient to retrieve the nodes as an array
    */

    public Vector getVector() {
        Vector vec = new Vector(length);
        for (int i=0; i<length; i++) {
            vec.addElement(value[i]);
        }
        return vec;
    }

    /**
    * Return the nodes in the node-set as a Vector. Note that they will only be in sorted
    * order (with duplicates eliminated) if sort() is called first.
    * @return an array containing the NodeInfo objects representing the nodes (possibly unsorted
    * and including duplicates)
    */

    public NodeInfo[] getNodes() {
        if (value.length == length) {
            return value;
        } else {
            // lose any excess space in the array
            NodeInfo[] newarray = new NodeInfo[length];
            System.arraycopy(value, 0, newarray, 0, length);
            value = newarray;
            return value;
        }
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (The reason for returning this is that
    * it makes life easier for the XSL compiler).
    */

    public NodeSetValue sort() throws SAXException {

        if (length<2) sorted=true;
        if (sorted) return this;

        if (reverseSorted) {
            
            NodeEnumeration enuma = enumerate();
            NodeInfo[] array = new NodeInfo[length];
            for (int n=0; n<length; n++) {
                array[n] = value[length-n-1];
            }
            value = array;
            sorted = true;
            reverseSorted = false;
            
        } else {
                  
            // sort the array

            DocumentOrderComparer comp = new DocumentOrderComparer();

            QuickSort q = new QuickSort(comp);
            q.sort(value, length);

            // need to eliminate duplicate nodes. Note that we cannot compare the node
            // objects directly, because with attributes and namespaces there might be
            // two objects representing the same node: but they will have the same
            // sequence number

            int j=1;
            for(int i=1; i<length; i++) {
                            //if (value[i].getSequenceNumber()!=value[i-1].getSequenceNumber()) {
                if (!value[i].isSameNode(value[i-1])) {
                    value[j++] = value[i];
                }
            }
            length = j;

            sorted = true;
            reverseSorted = false;
        }
        return this;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node, or null if the nodeset is empty
    */

    public NodeInfo getFirst() throws SAXException {
        if (length==0) return null;
        NodeInfo first = null;
        long minseq = Long.MAX_VALUE;
        for(int i=0; i<length; i++) {
            long seq = value[i].getSequenceNumber();
            if (seq < minseq) {
                first = value[i];
                minseq = seq;
            }
        }
        return first;
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
        return new NodeSetValueEnumeration();
    }

    /**
    * Diagnostic output: get a string representation of the nodeset
    */

    public String toString() {
        return "NODESET " + value.toString();
    }

    // implement DOM NodeList

    /**
    * return the number of nodes in the list (DOM method)
    */

    public int getLength() {
        try {
            return getCount();
        } catch (SAXException err) {
            return 0;
        }
    }

    /**
    * Return the n'th item in the list (DOM method)
    */

    public Node item(int index) {
        try {
            sort();
            if (length>index) {
                return value[index];
            } else {
                return null;
            }
        } catch (SAXException err) {
            return null;
        }
    }

    /**
    * Inner class NodeSetValueEnumeration
    */

    private class NodeSetValueEnumeration implements NodeEnumeration, LastPositionFinder {

        int index=0;

        public NodeSetValueEnumeration() {
            index = 0;
            //System.err.println("NSV enumeration: " + length);
        }

        public boolean hasMoreElements() {
            return index<length;
        }

        public NodeInfo nextElement() {
            //System.err.println("NSV enumeration: " + index + " of " + length + " = " + value[index]);
            return value[index++];
        }

        public boolean isSorted() {
            return sorted;
        }

        public boolean isReverseSorted() {
            return reverseSorted;
        }

        public boolean isPeer() {
            return false;
        }

        public int getLastPosition() {
            return length;
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

