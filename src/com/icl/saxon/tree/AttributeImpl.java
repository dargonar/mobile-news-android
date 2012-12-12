package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.AttributeCollection;
import com.icl.saxon.Context;

//import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.pattern.Pattern;

import java.util.*;
import java.io.Writer;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.w3c.dom.*;


/**
  * A node in the XML parse tree representing an attribute. Note that this is
  * generated only "on demand", when the attribute is selected by a select pattern.<P>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 17 June 1999
  */

class AttributeImpl extends NodeImpl implements AttributeInfo, Attr {

    protected Name name;
    protected String value;

    public AttributeImpl(){}

    /**
    * Construct an Attribute node for an attribute with a given name
    * @param element The element containing the relevant attribute
    * @param attname The (structured) name of the attribute. There must be
    * at attribute with this name.
    * @param sequenceBase The sequence number to be allocated to the first attribute node. Note
    * that attributes appear in document order AFTER namespace nodes, so we can't deduce
    * this from the index alone.
    */

    public AttributeImpl(ElementImpl element, Name attname) {
        parent = element;
        AttributeCollection ac = (AttributeCollection)element.getAttributeList();
        index = ac.getPosition(attname);
        this.name = ac.getExpandedName(index);
        this.value = ac.getValue(index);
    }

    /**
    * Construct an Attribute node for the n'th attribute of a given element
    * @param element The element containing the relevant attribute
    * @param index The index position of the attribute starting at zero
    * @param sequenceBase The sequence number to be allocated to the first attribute node. Note
    * that attributes appear in document order AFTER namespace nodes, so we can't deduce
    * this from the index alone.
    */

    public AttributeImpl(ElementImpl element, int index) throws SAXException {
        parent = element;
        this.index = index;
        this.name = element.getAttributeList().getExpandedName(index);
        this.value = element.getAttributeList().getValue(index);
    }

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNode(NodeInfo other) {
        if (!(other instanceof AttributeImpl)) return false;
        if (this==other) return true;
        AttributeImpl otherAtt = (AttributeImpl)other;
        return (parent.isSameNode(otherAtt.parent) && this.name.equals(otherAtt.name));
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    */

    public long getSequenceNumber() {
        return parent.getSequenceNumber() + 0x8000 + index;
        // note the 0x8000 is to leave room for namespace nodes
    }

    /**
    * Return the type of node.
    * @return Node.ATTRIBUTE
    */

    public final short getNodeType() {
        return ATTRIBUTE;
    }

    /**
    * Return the character value of the node. 
    * @return the attribute value
    */

    public String getValue() {
        return value;
    }
    
    /**
    * Get the name of this attribute node
    * @return The attribute name
    */

    public Name getExpandedName() {
        return name;
    }

    /**
    * Get the URI part of the name of this node. 
    * @return The URI of the namespace of this node. For the default namespace, return an
    * empty string
    */

    public final String getURI() {
        return name.getURI();
    }

    /**
    * Get next sibling - not defined for attributes
    */

    public Node getNextSibling() {
        return null;
    }

    /**
    * Get previous sibling - not defined for attributes
    */

    public Node getPreviousSibling() {
        return null;
    }

    /**
    * Get the previous node in document order. Not supported for attribute nodes.
    */

    public NodeInfo getPreviousInDocument() {
        return null;
    }

    /**
    * Get the next node in document order. Not supported for attribute nodes.
    */

    public NodeInfo getNextInDocument() {
        return null;
    }
    
    /**
    * Get node number, level=single. Not supported for attribute nodes.
    * @throws SAXException
    */

    public int getNumberSingle(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberSingle() is not supported for attribute nodes");
    }

    /**
    * Get node number, level=any. Not supported for attribute nodes.
    * @throws SAXException
    */

    public int getNumberAny(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberAny() is not supported for attribute nodes");
    }

    /**
    * Get node number, level=multi. Not supported for attribute nodes.
    * @throws SAXException
    */

    public Vector getNumberMulti(Pattern count, Pattern from) throws SAXException {
        throw new SAXException("getNumberMulti() is not supported for attribute nodes");
    }

    /**
    * Get sequential key. Returns key of owning element with the attribute name as a suffix
    */

    public String getSequentialKey() {
        return parent.getSequentialKey() + "_" + name;
    }
    
    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        out.writeAttribute(name, value);
    }

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c) throws SAXException {
        c.getOutputter().writeContent(value);
    }
    
    /**
    * Generate a path to this node
    */

    public String getPath() {
        return parent.getPath() + "/@" + name;
    }

    /**
    * Diagnostic output
    */

    public String toString() {
        return name.getDisplayName() + "=\"" + value + "\"";
    }

    // DOM methods

    /**
    * Get the attribute name (the QName)
    */

    public String getName() {
        return getDisplayName();
    }
    
    /**
     *  If this attribute was explicitly given a value in the original 
     * document, this is <code>true</code> ; otherwise, it is 
     * <code>false</code>.
     * Always true in this implementation.
     */
     
    public boolean getSpecified() {
        return true;
    }

    /**
    * Set the attribute value. Always fails (readonly)
    */

    public void setValue(String value) throws DOMException {
        disallowUpdate();
    }

    /**
     *  The <code>Element</code> node this attribute is attached to or 
     * <code>null</code> if this attribute is not in use.
     * @since DOM Level 2
     */
     
    public Element getOwnerElement() {
        return (ElementImpl)parent;
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
