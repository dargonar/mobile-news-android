package com.icl.saxon.axis;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

abstract class AxisEnumeration implements NodeEnumeration {

    protected NodeInfo start;
	protected NodeInfo next;
	protected int nodeType;
	protected NameTest nodeName;

	/**
	* Create an axis enumeration for a given type and name of node, from a given
	* origin node
	*/

	public AxisEnumeration(NodeInfo node, int nodeType, NameTest nodeName)
	throws SAXException {
	    next = node;
	    start = node;
	    this.nodeType = nodeType;
	    this.nodeName = nodeName;
	}

	/**
	* Test whether a node conforms to the node type and name constraints.
	* Note that this returns true if the supplied node is null, this is a way of
	* terminating a loop.
	*/

	protected boolean conforms(NodeInfo node) throws SAXException {
	    if (node==null) return true;
	    if (nodeName==null) return node.isa(nodeType);
	    return (node.isa(nodeType) && nodeName.isNameOf(node));
	}

	/**
	* Advance along the axis until a node is found that matches the required criteria
	*/

	protected final void advance() throws SAXException {
	    do {
	        step();
	    } while (!conforms(next));
	}

	/**
	* Advance one step along the axis: the resulting node might not meet the required
	* criteria for inclusion
	*/

	protected abstract void step() throws SAXException;

	/**
	* Determine if there are more nodes to be returned
	*/

	public final boolean hasMoreElements() throws SAXException {
	    return next!=null;
	}

	/**
	* Return the next node in the enumeration
	*/

	public final NodeInfo nextElement() throws SAXException {
	    NodeInfo n = next;
	    advance();
	    return n;
	}

	/**
	* Determine if the nodes are guaranteed to be sorted in document order
	*/

	public boolean isSorted() throws SAXException {
	    return false;           // unless otherwise specified
	}

	/**
	* Determine if the nodes are guaranteed to be sorted in reverse document order
	*/

    public boolean isReverseSorted() throws SAXException {
        return !isSorted();
    }

	/**
	* Determine if the nodes are guaranteed to be peers (i.e. no node is a descendant of
	* another node)
	*/

	public boolean isPeer() throws SAXException {
	    return false;           // unless otherwise specified
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
