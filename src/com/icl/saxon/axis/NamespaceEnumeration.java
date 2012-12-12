package com.icl.saxon.axis;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.ElementInfo;
import com.icl.saxon.om.NamespaceInfo;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.tree.NamespaceImpl;
import com.icl.saxon.tree.ElementImpl;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

class NamespaceEnumeration extends AxisEnumeration {
    
    private ElementInfo element;
    private Vector nslist;
    private int index;
    private int length;

    public NamespaceEnumeration(NodeInfo node, int nodeType, NameTest nodeName)
    throws SAXException {
        super(node, nodeType, nodeName);        
        
        // nodeType must be NodeInfo.NAMESPACE - ignore it

        if (node instanceof ElementInfo) {
            element = (ElementInfo)node;
            nslist = new Vector(10);
            element.addNamespaceNodes(element, nslist, null);
            nslist.addElement(new NamespaceImpl((ElementImpl)element, "xml", Namespace.XML, nslist.size()+1));
            index = -1;
            length = nslist.size();
            advance();
        } else {      // if it's not an element then there are no namespace nodes
            next = null;
        }

    }

    public void step() throws SAXException {
        index++;
        if (index<length) {
            next = (NamespaceInfo)nslist.elementAt(index);
        } else {
            next = null;
        }
    }

	/**
	* Test whether a node conforms. Reject a node with prefix="", uri="" since
	* this represents a namespace undeclaration and not a true namespace node.
	*/

	protected boolean conforms(NodeInfo node) throws SAXException {
	    if (node==null) return true;
        NamespaceInfo ns = (NamespaceInfo)node;
        if (ns.getNamespacePrefix().equals("") && ns.getNamespaceURI().equals("")) {
            return false;
        }
        return nodeName.isNameOf(node);
	}

    public boolean isSorted() {
        return true;            // in the sense that there is no need to sort them again
    }

    public boolean isPeer() throws SAXException {
        return true;           
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
