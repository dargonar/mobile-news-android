package com.icl.saxon.tree;
import com.icl.saxon.om.*;
//import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.Context;
import com.icl.saxon.pattern.Pattern;

import java.util.*;
import java.io.Writer;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.w3c.dom.*;

/**
  * A node in the XML parse tree representing a Namespace. Note that this is
  * generated only "on demand", when the namespace axis is expanded.<P>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 3 November 1999
  */

public class NamespaceImpl extends NodeImpl implements NamespaceInfo {

    private String prefix;
    private String uri;
    private int index;

    private static Vector emptyVector = new Vector();

    /**
    * Construct a Namespace node 
    * @param element The element containing the relevant attribute
    * @param prefix The namespace prefix (empty string for the default namespace)
    * @param uri The namespace uri
    * @param index Integer identifying this namespace node among the nodes for its parent
    */

    public NamespaceImpl(ElementImpl element, String prefix, String uri, int index) throws SAXException {
        this.parent = element;
        this.prefix = prefix;
        this.uri = uri;
        this.index = index;
    }

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNode(NodeInfo other) {
        if (!(other instanceof NamespaceImpl)) return false;
        if (this==other) return true;
        NamespaceImpl otherN = (NamespaceImpl)other;
        return (parent.isSameNode(otherN.parent) && this.prefix.equals(otherN.prefix));
    }

    /**
    * Get the prefix of the namespace that this node relates to
    */

    public String getNamespacePrefix() {
        return prefix;
    }

    /**
    * Get the uri of the namespace that this node relates to
    */

    public String getNamespaceURI() {
        return uri;
    }
    
    /**
    * Set the prefix of the namespace that this node relates to
    */

    public void setNamespacePrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
    * Set the uri of the namespace that this node relates to
    */

    public void setNamespaceURI(String uri) {
        this.uri = uri;
    }
    
    /**
    * Return the type of node.
    * @return NodeInfo.NAMESPACE
    */

    public final short getNodeType() {
        return NAMESPACE;
    }

    /**
    * Return the string value of the node. 
    * @return the namespace uri
    */

    public String getValue() {
        return uri;
    }
    
    /**
    * Get the name of this namespace node
    * @return The namespace name. This is always a simple name with no prefix or uri.
    */

    public Name getExpandedName() {
        return new Name(prefix);
    }

    /**
    * Get the name of this node, following the DOM rules (which aren't actually defined
    * for Namespace nodes...)
    * @return the namespace prefix
    */

    public String getNodeName() {
        return prefix;
    }

    /**
    * Get next sibling - not defined for namespace nodes
    */

    public Node getNextSibling() {
        return null;
    }

    /**
    * Get previous sibling - not defined for namespace nodes
    */

    public Node getPreviousSibling() {
        return null;
    }

    /**
    * Get the previous node in document order. Not supported for namespace nodes.
    */

    public NodeInfo getPreviousInDocument() {
        return null;
    }

    /**
    * Get the next node in document order. Not supported for namespace nodes.
    * @throws SAXException, always
    */

    public NodeInfo getNextInDocument() {
        return null;
    }
    
    /**
    * Get node number, level=single. Not supported for namespace nodes.
    * @throws SAXException
    */

    public int getNumberSingle(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberSingle() is not supported for namespace nodes");
    }

    /**
    * Get node number, level=any. Not supported for namespace nodes.
    * @throws SAXException
    */

    public int getNumberAny(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberAny() is not supported for namespace nodes");
    }

    /**
    * Get node number, level=multi. Not supported for namespace nodes.
    * @throws SAXException
    */

    public Vector getNumberMulti(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberMulti() is not supported for namespace nodes");
    }

    /**
    * Get sequential key. Returns key of owning element with the namespace prefix as a suffix
    */

    public String getSequentialKey() {
        return parent.getSequentialKey() + "^" + prefix;
    }
    
    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        
    }

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c)  {
        // do nothing
    }


    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    */

    public long getSequenceNumber() {
        return parent.getSequenceNumber() + index;
    }

    /**
    * Generate a path to this node
    */

    public String getPath() {
        return parent.getPath() + "/namespace::" + prefix;
    }


    /**
    * Diagnostic output
    */

    public String toString() {
        return "Namespace " + prefix;
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
