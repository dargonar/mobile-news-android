package com.icl.saxon.om;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.output.*;
import com.icl.saxon.Context;

import java.util.*;
import java.io.Writer;
import java.net.*;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

import org.w3c.dom.*;


/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top class in the interface hierarchy for nodes; see NodeImpl for the implementation
  * hierarchy.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 20 July 1999
  */

public interface NodeInfo extends Node {

    // Node types. "NODE" means any type.
    // These node numbers should be kept aligned with those defined in the DOM.

    public static final short NODE = 0;       // matches any kind of node
    public static final short ELEMENT = 1;
    public static final short ATTRIBUTE = 2;
    public static final short TEXT = 3;
    public static final short DOCUMENT = 4;
    public static final short PI = 7;
    public static final short COMMENT = 8;
    public static final short NAMESPACE = 9;
    public static final short NUMBER_OF_TYPES = 10;
    public static final short NONE = 9999;    // a test for this node type will never be satisfied


    /**
    * Return the type of node.
    * @return one of the values Node.ELEMENT, Node.TEXT, Node.ATTRIBUTE, etc.
    */

    public short getNodeType();

    /**
    * Determine whether the node is of a given type. <P>
    * Note, this can also be done by testing the node using "instanceof". But this is
    * inconvenient when passing the class as a parameter to another routine.
    * @param nodeType One of the specific node types such as ELEMENT or TEXT, or the general
    * node type NODE
    * @return true if the node is an instance of the specified node type
    */

    public boolean isa(int nodeType);

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNode(NodeInfo other);

    /**
    * Get the base URL for the node.
    * @return the System Identifier of the entity in the source document containing the node,
    * or null if not known
    */

    public String getSystemId();

    /**
    * Get line number
    * @return the line number of the node in its original source document; or -1 if not available
    */

    public int getLineNumber();

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    */

    public long getSequenceNumber(); 
    
    /**
    * Return the character value of the node. The interpretation of this depends on the type
    * of node. For an element it is the accumulated character content of the element,
    * including descendant elements.
    * @return the string value of the node
    */

    public String getValue();
    
    /**
    * Get the expanded name of this node, following the XPath naming rules 
    * @return The name of the node. For an element this is the element name, for an attribute
    * it is the attribute name, complete with namespace information. Other node types return
    * null.
    */

    public Name getExpandedName();

    /**
    * Get the name of this node, following the DOM rules 
    * @return The name of the node. For an element this is the element name, for an attribute
    * it is the attribute name, as a QName. Other node types return conventional names such
    * as "#text" or "#comment"
    */

    public String getNodeName();

    /**
    * Get the local part of the name of this node. This is the name after the ":" if any.
    * @return the local part of the name. For an unnamed node, return an empty string.
    */

    public String getLocalName();

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return an empty string.
    */

    public String getPrefix();

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, return null.
    * For a node with an empty prefix, return an empty string.
    */

    public String getURI();

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node. 
    * For a node with no name, return an empty string.
    */

    public String getDisplayName();

    /**
    * Get the absolute name of this node. For elements and attributes this is [uri^]localname.
    * For other nodes, it is the same as the display name
    * @return The absolute name of this node. 
    * For a node with no name, return an empty string.
    */

    public String getAbsoluteName();

    /**
    * Test if the name of the node (including namespaces) is equivalent to the given name
    */

    public boolean hasName(Name name); 


    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param name the name of an attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue(Name name); 

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param name the name of an attribute. This must be an unqualified attribute name,
     * i.e. one with no namespace.
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue(String name); 

     /**
     * Get the nearest ancestor element with a given element name
     * @param name The name of the required ancestor
     * @return The NodeInfo for the nearest ancestor with the
     * given tag; null if there is no such ancestor
     */

    public ElementInfo getAncestor(Name name);
    
     /**
     * Get the nearest ancestor node that matches the given pattern
     * @param pattern A pattern that the ancestor node must satisfy.
     * @return The NodeInfo for the nearest ancestor node that matches the
     * given pattern; null if there is no such ancestor
     */

    public NodeInfo getAncestor(Pattern pat, Context context) throws SAXException;
       
    /**
     * Determine whether this element is the outermost element.
     * @return True if this element is the outermost element.
     */
     
    public boolean isDocumentElement(); 

    /**
    * Determine whether this node is an ancestor of another node
    * @param other the other node (the putative descendant of this node)
    * @return true of this node is an ancestor of the other node
    */

    public boolean isAncestor(NodeInfo other);
    
    /**
     * Get the outermost element.
     * @return the Element for the outermost element of the document.
     */
     
    public Element getDocumentElement(); 

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot(); 

    /**
    * Get the next sibling node that matches a given pattern.
    * @param pattern The match-pattern that the required sibling must match
    * @return The NodeInfo object describing the next node at the same level.
    * Returns null if there is no subsequent child of the same parent that matches the supplied
    * pattern.
    */
    
    public NodeInfo getNextSibling(Pattern pattern, Context context) throws SAXException;

    /**
    * Get the next node in document order
    * @param anchor: the scan stops when it reaches a node that is not a descendant of the specified
    * anchor node
    * @return the next node in the document, or null if there is no such node
    */

    public NodeInfo getNextInDocument(NodeInfo anchor); 

    /**
    * Get the first child node of a given type.
    * @param pattern: identifies the pattern to be matched by the required child
    * @return the the first child node of the required type, or null if there is no such
    * child
    */

    public abstract NodeInfo getFirstChild(Pattern pattern, Context context) throws SAXException;

    /**
    * Get the previous sibling of the node that matches a given pattern.
    * @param pattern The match-pattern that the required sibling must match
    * @return The NodeInfo object describing the previous node at the same level that
    * matches the pattern. Returns null if there is no previous child of the same
    * parent that matches the supplied pattern.
    */
    
    public NodeInfo getPreviousSibling(Pattern pattern, Context context) throws SAXException;

    /**
    * Get the previous node in document order
    * @return the previous node in the document, or null if there is no such node
    */

    public NodeInfo getPreviousInDocument(); 

    /**
    * Get the previous node in document order
    * @param pattern: identifies a pattern the required node
    * @return the previous node in the document of the required type, or null if there is no
    * such node
    */

    public NodeInfo getPreviousInDocument(Pattern pattern, Context context) throws SAXException;

    /**
    * Get the last child node of a given type
    * @param pattern: identifies the pattern to be matched by the required child
    * @return the the last child node of the required type, or null if there is no such
    * child
    */

    public NodeInfo getLastChild(Pattern pattern, Context context) throws SAXException;

    /**
    * Get the number of children.
    */

    public int getNumberOfChildren();

    /**
    * Get all child nodes of the element (child elements and character nodes)
    * @return an array containing a NodeInfo for each child node
    */

    public NodeInfo[] getAllChildNodes(); 

    /**
    * Get index: that is, the number of preceding sibling nodes at the same level.
    * Note this is not defined for attribute nodes.
    * @return the number of preceding sibling nodes at the same level
    */

    public int getIndex(); 

    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL. The context is used to
    * remember information from one call to the next, for performance benefits.
    */

    public int getNumberSimple(Context context);

    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL.
    */

    public int getNumberSimple() throws SAXException;

    /**
    * Get node number (level="single"). If the current node matches the supplied pattern, the returned
    * number is one plus the number of previous siblings that match the pattern. Otherwise,
    * return the element number of the nearest ancestor that matches the supplied pattern.
    * @param count Pattern that identifies which nodes should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * (This parameter does not seem useful but is included for the sake of XSLT conformance.)
    * @return the node number established as follows: go to the nearest ancestor-or-self that
    * matches the 'count' pattern and that is a descendant of the nearest ancestor that matches the
    * 'from' pattern. Return one plus the nunber of preceding siblings of that ancestor that match
    * the 'count' pattern. If there is no such ancestor, return 0.
    */

    public int getNumberSingle(Pattern count, Pattern from, Context c) throws SAXException;
    
    /**
    * Get node number (level="any").
    * Return one plus the number of previous nodes in the
    * document that match the supplied pattern
    * @param count Pattern that identifies which nodes should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * Only nodes after the first (most recent) node that matches the 'from' pattern are counted.
    * @return one plus the number of nodes that precede the current node, that match the count pattern,
    * and that follow the first node that matches the from pattern if specified.
    */

    public int getNumberAny(Pattern count, Pattern from, Context c) throws SAXException;
    
    /**
    * Get node number (level="multi").
    * Return a vector giving the hierarchic position of this node. See the XSLT spec for details.
    * @param count Pattern that identifies which nodes (ancestors and their previous siblings)
    * should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * Only nodes below the first (most recent) node that matches the 'from' pattern are counted.
    * @return a vector containing for each ancestor-or-self that matches the count pattern and that
    * is below the nearest node that matches the from pattern, an Integer which is one greater than
    * the number of previous siblings that match the count pattern.
    */

    public Vector getNumberMulti(Pattern count, Pattern from, Context c) throws SAXException;
    
    /**
    * Get a character string that uniquely identifies this node and that collates nodes
    * into document order
    * @return a string. The string is always interned so keys can be compared using "==".
    */

    public String getSequentialKey() ; //throws SAXException;

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c) throws SAXException;

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException;

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws SAXException;

    /**
    * Generate a path to this node
    */

    public String getPath(); 
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
