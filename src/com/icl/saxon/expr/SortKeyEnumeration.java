package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

import com.icl.saxon.sort.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A SortKeyEnumeration is NodeEnumeration that delivers the nodes sorted according to
* a specified sort key. <BR>
*
*/


public class SortKeyEnumeration implements NodeEnumeration, LastPositionFinder {

    protected NodeEnumeration base;     // the nodes to be sorted
    private Vector sortkeys;            // vector of SortKeyDefinition's
    private CompositeKey[] nodeKeys;    // the sort keys of all the nodes to be sorted
    private int count = -1;
    private int index = 0;
    private Context context;
    private MultiKeyComparer comparer;

    public SortKeyEnumeration(NodeEnumeration base) throws SAXException {
        this.base = base;
    }

    /**
    * Set the list of sort key definitions, in major-to-minor order
    */

    public void setSortKeys(Vector v) throws SAXException {
        sortkeys = v;
        // If any sortkey depends on position(), we must ensure the base enumeration is
        // in document order. If it uses last() (unlikely), we must ensure that the number
        // of nodes is known, so we sort it in this case also
        if (!base.isSorted()) {
            boolean mustBeSorted = false;
            for (int i=0; i<sortkeys.size(); i++) {
                SortKeyDefinition sk = (SortKeyDefinition)sortkeys.elementAt(i);
                Expression k = sk.getSortKey();
                if ((k.getDependencies() & (Context.POSITION | Context.LAST)) != 0) {
                    mustBeSorted = true;
                    break;
                }
            }
            if (mustBeSorted) {
                NodeSetExtent nsv = new NodeSetExtent(base);
                nsv.sort();
                base = nsv.enumerate();
            }
        }                
    }

    /**
    * Set the comparer to be used
    */

    public void setComparer(MultiKeyComparer c) {
        comparer = c;
    }

    /**
    * Get the comparer
    */

    public Comparer getComparer() {
        return comparer;
    }

    /**
    * Set the base context for evaluating the sort keys
    */

    public void setContext(Context c) {
        if (c==null) {
            context = new Context();
        } else {
            context = c.newContext();
        }
    }

    /**
    * Determine whether there are more nodes
    */

    public boolean hasMoreElements() throws SAXException {
        if (count<0) doSort();
        return index<count;
    }

    /**
    * Get the next node, in sorted order
    */

    public NodeInfo nextElement() {
        return nodeKeys[index++].getNode();
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted()  {
        return false;
    }

    public boolean isPeer() throws SAXException {
        return base.isPeer();
    }

    public int getLastPosition() throws SAXException {
        if (base instanceof LastPositionFinder && !(base instanceof LookaheadEnumerator)) {
            return ((LastPositionFinder)base).getLastPosition();
        }
        if (count<0) doSort();
        return count;
    }

    private void buildArray() throws SAXException {
        int allocated;
        if (base instanceof LastPositionFinder && !(base instanceof LookaheadEnumerator)) {
            allocated = ((LastPositionFinder)base).getLastPosition();
            context.setLast(allocated);
        } else {
            allocated = 100;
            context.setLast(-1);    // shouldn't be used
        }
        nodeKeys = new CompositeKey[allocated];
        count = 0;

        // initialise the array with data
        
        while (base.hasMoreElements()) {
            NodeInfo node = base.nextElement();
            if (count==allocated) {
                allocated *= 2;
                CompositeKey[] newnodes = new CompositeKey[allocated];
                System.arraycopy(nodeKeys, 0, newnodes, 0, count);
                nodeKeys = newnodes;
            }
            context.setCurrentNode(node);
            context.setContextNode(node);
            context.setPosition(count+1);

            nodeKeys[count] = new CompositeKey(sortkeys, node, context);
            count++;
        }
    }

    private void doSort() throws SAXException {
        buildArray();
        if (count<2) return;
        
        // sort the array

        QuickSort q = new QuickSort(comparer);
        q.sort(nodeKeys, count);
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
