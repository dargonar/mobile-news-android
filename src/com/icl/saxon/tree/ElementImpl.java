package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.AttributeCollection;
import com.icl.saxon.NameTest;

import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;

import java.util.*;
import java.io.Writer;

import org.xml.sax.SAXException;
import org.w3c.dom.*;

/**
  * ElementImpl implements an element with no attributes or namespace declarations.<P>
  * This class is an implementation of ElementInfo. For elements with attributes or
  * namespace declarations, class ElementWithAttributes is used.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 8 August 2000
  */

// The name of the element and its attributes are now namespace-resolved by the
// parser. However, this class retains the ability to do namespace resolution for other
// names, for example variable and template names in a stylesheet.

public class ElementImpl extends ParentNodeImpl
    implements ElementInfo, Element, NamedNodeMap {

    private static AttributeCollection emptyAtts = new AttributeCollection();
    
    protected Name fullName;

    /**
    * Construct an empty ElementImpl

    public ElementImpl() {}

    /**
    * Initialise a new ElementImpl with an element name
    * @param name The element name, with namespaces resolved
    * @param atts The attribute list: always null
    * @param parent The parent node    
    */

    public void initialise(Name name, AttributeCollection atts, NodeInfo parent,
                            String baseURI, int lineNumber, int sequenceNumber)
    throws SAXException {
        this.fullName = name;
        this.parent = (ParentNodeImpl)parent;
        this.sequence = sequenceNumber;
        DocumentImpl doc = (DocumentImpl)getDocumentRoot();
        doc.setLineNumber(sequenceNumber, lineNumber);
        doc.setBaseURI(sequenceNumber, baseURI);
    }

    /**
    * Get the base URI for the node.
    */

    public final String getSystemId() {
        return ((DocumentImpl)getDocumentRoot()).getBaseURI(sequence);
    }

    /**
    * Set the line number of the element within its source document entity
    */

    public void setLineNumber(int line) {
        ((DocumentImpl)getDocumentRoot()).setLineNumber(sequence, line);
    }


    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return ((DocumentImpl)getDocumentRoot()).getLineNumber(sequence);
    }

    /**
    * Return the name of the node. 
    * @return the element name, with its namespace prefix if there is one.
    */

    public final Name getExpandedName() {
        return fullName;
    }

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return an empty string.
    */

    public final String getPrefix() {
        return fullName.getPrefix();
    }

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For the default namespace, return an
    * empty string
    */

    public final String getURI() {
        return fullName.getURI();
    }

    /**
    * Search the NamespaceList for a given prefix, returning the corresponding URI.
    * @param prefix The prefix to be matched. To find the default namespace, supply ""
    * @return The URI corresponding to this namespace. If it is an unnamed default namespace,
    * return "". 
    * @throws SAXException if the prefix has not been declared on this NamespaceList.
    */

    public String getURIforPrefix(String prefix) throws SAXException {
        if (prefix.equals("xml")) return Namespace.XML;
        if (parent instanceof DocumentInfo) {
            if (prefix.equals("")) return "";            
            throw new SAXException("Namespace for prefix \"" + prefix + "\" has not been declared");
        } else {
            return ((ElementInfo)parent).getURIforPrefix(prefix);
        }
    }

    /**
    * Search the NamespaceList for a given URI, returning the corresponding prefix.
    * @param uri The URI to be matched. 
    * @return The prefix corresponding to this URI. If not found, return null. If there is
    * more than one prefix matching the URI, the first one found is returned. If the URI matches
    * the default namespace, return an empty string.
    */

    public String getPrefixForURI(String uri) {
        if (parent instanceof DocumentInfo) {
            return null;
        } else {
            return ((ElementInfo)parent).getPrefixForURI(uri);
        }
    }

    /**
    * Make the set of all namespace nodes associated with this element.
    * @param owner The element owning these namespace nodes.
    * @param list a Vector containing NamespaceInfo objects representing the namespaces
    * in scope for this element; the method appends nodes to this Vector, which should
    * initially be empty. Note that the returned list will never contain the XML namespace
    * (to get this, the NamespaceEnumeration class adds it itself). The list WILL include
    * an entry for the undeclaration xmlns=""; again it is the job of NamespaceEnumeration
    * to ignore this, since it doesn't represent a true namespace node.
    * @param stop the ancestor node to stop at. Supply null to go all the way back to
    * the root node. [I believe all calls on this method now supply null: the functionality
    * was provided for Literal Result Elements in the stylesheet but is no longer used].
    */

    public void addNamespaceNodes(ElementInfo owner, Vector list, NodeInfo stop) throws SAXException {
        // just add the namespaces defined on the ancestor nodes
        
        if (!(parent instanceof DocumentInfo || parent==stop)) {
            ((ElementInfo)parent).addNamespaceNodes(owner, list, stop);
        }
                
    }
    
    /**
    * Output all namespace nodes associated with this element.
    * @param out The relevant outputter
    */

    public void outputNamespaceNodes(Outputter out) throws SAXException {

        // just add the namespaces defined on the ancestor nodes. We rely on the outputter
        // to eliminate multiple declarations of the same prefix
        
        if (!(parent instanceof DocumentInfo)) {
            ((ElementInfo)parent).outputNamespaceNodes(out);
        }
    }
    
    /**
    * Make a Name, using this Element as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    */

    public final Name makeName(String qname, boolean useDefault) throws SAXException {
        return new Name(qname, this, useDefault);
    }

    /**
    * Make a NameTest object for a prefix:* wildcard
    */

    public final NameTest makePrefixTest(String wildcard) throws SAXException {
        String uri = getURIforPrefix(wildcard);
        return new PrefixTest(uri);
    }

    /**
    * Return the type of node.
    * @return NodeInfo.ELEMENT
    */

    public final short getNodeType() {
        return ELEMENT;
    }
    
    /**
    * Get the attribute list for this element.
    * @return The attribute list. This will not include any
    * namespace attributes. The attribute names will be in expanded form, with prefixes
    * replaced by URIs
    */
    
    public AttributeCollection getAttributeList() {
        return emptyAtts;
    }
    
    /**
     *  Find the value of a given attribute of this element. <BR>
     *  This is a short-cut method; the full capability to examine
     *  attributes is offered via the getAttributeList() method. <BR>
     *  The attribute may either be one that was present in the original XML document,
     *  or one that has been set by the application using setAttribute(). <BR>
     *  @param name the name of an attribute. Any prefix in the name is interpreted
     *  in the context of the namespaces applying to the current element
     *  @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue( Name name ) {
        return null;
    }

    /**
     *  Find the value of a given attribute of this element. <BR>
     *  This is a short-cut method; the full capability to examine
     *  attributes is offered via the getAttributeList() method. <BR>
     *  The attribute may either be one that was present in the original XML document,
     *  or one that has been set by the application using setAttribute(). <BR>
     *  @param name the name of an attribute. There must be no prefix in the name.
     *  @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue( String name ) {
        return null;
    }

    /**
    * Make an attribute node for a given attribute of this element
    * @param name The attribute name
    */

    public AttributeInfo makeAttributeNode(Name attributeName) {
        return null;
    }
    
    /**
    * Find the value of an inherited attribute. The current element, its parent,
    * and its ancestors are searched recursively to find an attribute with the given
    * name.<br>
    * @param name the name of the attribute, as a Name object
    * @return the value of the attribute, if it is defined on this element or 
    * on an ancestor element; otherwise null
    */

    public String getInheritedAttribute(Name name ) throws SAXException
    {
        NodeInfo p = this;
        while (p instanceof ElementInfo) {
            String v = p.getAttributeValue(name);
            if (v!=null) return v;
            else p=(NodeInfo)p.getParentNode();
        }
        return null;
    }
    
    
    /**
    * Set the value of an attribute on the current element. This affects subsequent calls
    * of getAttribute() for that element. 
    * @param name The name of the attribute to be set. Any prefix is interpreted relative
    * to the namespaces defined for this element. 
    * @param value The new value of the attribute. Set this to null to remove the attribute.
    */
    
    public void setAttribute(String name, String value ) throws DOMException {
        disallowUpdate();
    }
       
    /**
     * Determine whether this element is the first in a consecutive
     * group. A consecutive group is a group of elements of the same
     * type subordinate to the same parent element; there can be intervening
     * character data, white space, or processing instructions, but no
     * elements of a different type.
     * @return True if this is the first child of its parent, or if the
     * previous child was a different element type, or if this element 
     * is the root.
     */

    public boolean isFirstInGroup() {
        NodeInfo prev = (NodeInfo)getPreviousSibling();
        while(prev!=null && !(prev instanceof ElementInfo))
            prev = (NodeInfo)prev.getPreviousSibling();
        if (prev==null) return true;
        return !prev.hasName(this.getExpandedName());
    }

    /**
     * Determine whether this element is the last in a consecutive
     * group. A consecutive group is a group of elements of the same
     * type subordinate to the same parent element; there can be intervening
     * character data, white space, or processing instructions, but no
     * elements of a different type.
     * @return True if this is the last child of its parent, or if the
     * next child is a different element type, or if this element 
     * is the root.
     */

     public boolean isLastInGroup() {
        NodeInfo next = (NodeInfo)getNextSibling();
        while(next!=null && !(next instanceof ElementInfo))
            next = (NodeInfo)next.getNextSibling();
        if (next==null) return true;
        return !next.hasName(this.getExpandedName());
    }
    
    /**
     * Determine whether this element is the first element child of its parent.
     * @return True if this element is the first element child of its parent, or
     * if it is the document element.
     */
     
    public boolean isFirstChild() {
        NodeInfo prev = (NodeInfo)getPreviousSibling();
        while(prev!=null && !(prev instanceof ElementInfo))
            prev = (NodeInfo)prev.getPreviousSibling();
        return (prev==null);
    }

    /**
     * Determine whether this element is the last child element of its parent.
     * @return True if this element is the last child element of its parent, or
     * if it is the root element.
     */
     
    public boolean isLastChild() {
        NodeInfo next = (NodeInfo)getNextSibling();
        while(next!=null && !(next instanceof ElementInfo))
            next = (NodeInfo)next.getNextSibling();
        return (next==null);
    }
    
    /**
    * Copy this node to a given outputter (supporting xsl:copy-of)
    */

    public void copy(Outputter out) throws SAXException {
        out.writeStartTag(fullName);

        // output the children

        NodeInfo next = (NodeInfo)getFirstChild();
        while (next!=null) {
            next.copy(out);
            next = (NodeInfo)next.getNextSibling();
        }

        out.writeEndTag(fullName);
    }    

    /**
    * Generate a path to this node
    */

    public String getPath() {
        String pre = ((NodeInfo)getParentNode()).getPath();
        return (pre.equals("/") ? "" : pre) +
               "/" + getDisplayName() + "[" + getNumberSimple() + "]";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Following interfaces are provided to implement the DOM Element interface
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *  The name of the element (DOM interface). 
     */
     
    public String getTagName() {
        return getDisplayName();
    }

    /**
     *  The value of this node (as defined in DOM). Always null.
     */

    public String getNodeValue() {
        return null;
    }
    
    /**
     * Retrieves an attribute value by name. This is a DOM method, so namespace
     * declarations count as attributes.
     * @param name  The name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     */
     
    public String getAttribute(String name) {
        return "";
    }

    /**
     * A <code>NamedNodeMap</code> containing the attributes of this element. This
     * is a DOM method, so the list of attributes includes namespace declarations.
     */
     
    public NamedNodeMap getAttributes() {
        return this;
    }

    /**
     * Removes an attribute by name.
     * @param name  The name of the attribute to remove.
     */
     
    public void removeAttribute(String name) {
        setAttribute(name, null);
    }
    
    /**
     * Retrieves an attribute node by name. This is a DOM method, so namespace
     * declarations are treated as attributes.
     * <br> To retrieve an attribute node by qualified name and namespace URI, 
     * use the <code>getAttributeNodeNS</code> method.
     * @param name  The name (<code>nodeName</code> ) of the attribute to 
     *   retrieve.
     * @return  The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code> ) or <code>null</code> if there is no such 
     *   attribute.
     */
     
    public Attr getAttributeNode(String name) {
        return null;
    }

    /**
     * Adds a new attribute node. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Removes the specified attribute node. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Returns a <code>NodeList</code> of all descendant <code>Elements</code>
     *  with a given tag name, in the order in which they are encountered in 
     * a preorder traversal of this <code>Element</code> tree.
     * @param name  The name of the tag to match on. The special value "*" 
     *   matches all tags.
     * @return  A list of matching <code>Element</code> nodes.
     */
     
    public NodeList getElementsByTagName(String name) {
        Vector v = new Vector();
        NodeInfo next = this;
        while(next!=null) {
            if (next instanceof ElementInfo) {
                if (name.equals("*") || name.equals(next.getDisplayName())) {
                    v.addElement(next);
                }
            }
            next = next.getNextInDocument(this);
        }
        return new NodeSetExtent(v);        
    }

    /**
     * Retrieves an attribute value by local name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     * @since DOM Level 2
     */
     
    public String getAttributeNS(String namespaceURI, String localName) {
        return "";
    }                                           

    /**
     * Adds a new attribute. Always fails.
     * @param namespaceURI  The  namespace URI of the attribute to create or 
     *   alter.
     * @param qualifiedName  The  qualified name of the attribute to create or 
     *   alter.
     * @param value  The value to set in string form.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public void setAttributeNS(String namespaceURI, 
                               String qualifiedName, 
                               String value)
                               throws DOMException {
        disallowUpdate();
    }

    /**
     * Removes an attribute by local name and namespace URI. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     */
     
    public void removeAttributeNS(String namespaceURI, 
                                  String localName)
                                  throws DOMException{
        disallowUpdate();
    }
    
    /**
     * Retrieves an <code>Attr</code> node by local name and namespace URI. 
     * DOM method, so namespace declarations count as attributes.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> node with the specified attribute local 
     *   name and namespace URI or <code>null</code> if there is no such 
     *   attribute.
     * @since DOM Level 2
     */
     
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return null;
    }                                    

    /**
     * Add a new attribute. Always fails.
     * @param newAttr  The <code>Attr</code> node to add to the attribute list.
     * @return  If the <code>newAttr</code> attribute replaces an existing 
     *   attribute with the same  local name and  namespace URI , the 
     *   replaced <code>Attr</code> node is returned, otherwise 
     *   <code>null</code> is returned.
     * @exception DOMException
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     */
     
    public Attr setAttributeNodeNS(Attr newAttr)
                                   throws DOMException{
        disallowUpdate();
        return null;
    }

    /**
     * Returns a <code>NodeList</code> of all the descendant 
     * <code>Elements</code> with a given local name and namespace URI in the 
     * order in which they are encountered in a preorder traversal of this 
     * <code>Element</code> tree.
     * @param namespaceURI  The  namespace URI of the elements to match on. 
     *   The special value "*" matches all namespaces.
     * @param localName  The  local name of the elements to match on. The 
     *   special value "*" matches all local names.
     * @return  A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code> .
     * @since DOM Level 2
     */
     
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        Vector v = new Vector();
        NodeInfo next = this;
        while(next!=null) {
            if (next instanceof ElementInfo) {
                if (namespaceURI.equals("*") || namespaceURI.equals(next.getURI()) &&
                    (localName.equals("*") || localName.equals(next.getLocalName()))) {
                    v.addElement(next);
                }
            }
            next = next.getNextInDocument(this);
        }
        return new NodeSetExtent(v);   
    }

    /**
     * Returns <code>true</code> when an attribute with a given name is 
     * specified on this element or has a default value, <code>false</code> 
     * otherwise. This is a DOM method, so namespace declarations are treated as
     * attributes.
     * @param name  The name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given name is 
     *   specified on this element or has a default value, <code>false</code> 
     *   otherwise.
     * @since DOM Level 2
     */
     
    public boolean hasAttribute(String name) {
        return false;
    }

    /**
     * Returns <code>true</code> when an attribute with a given local name 
     * and namespace URI is specified on this element or has a default value, 
     * <code>false</code> otherwise. This is a DOM method so namespace declarations
     * are treated as attributes.
     * @param namespaceURI  The  namespace URI of the attribute to look for.
     * @param localName  The  local name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given local name and 
     *   namespace URI is specified or has a default value on this element, 
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
     
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return false;
    }    

    //////////////////////////////////////////////////////////////////////
    // Methods to implement DOM NamedNodeMap (the set of attributes)
    //////////////////////////////////////////////////////////////////////

    /**
    * Get named attribute (DOM NamedNodeMap method)
    * Treats namespace declarations as attributes.    
    */

    public Node getNamedItem(String name) {
        return null;
    }

    /**
    * Set named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node setNamedItem(Node arg) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
    * Remove named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node removeNamedItem(String name) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
    * Get n'th attribute (DOM NamedNodeMap method). Namespace declarations are treated
    * as attributes.
    */

    public Node item(int index) {
        return null;
    }

    /**
    * Get number of attributes (DOM NamedNodeMap method). 
    * Treats namespace declarations as attributes.
    */

    public int getLength() {
        return 0;
    }

    /**
    * Get named attribute (DOM NamedNodeMap method)
    * Treats namespace declarations as attributes.
    */

    public Node getNamedItemNS(String uri, String localName) {
        return null;
    }

    /**
    * Set named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node setNamedItemNS(Node arg) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
    * Remove named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node removeNamedItemNS(String uri, String localName) throws DOMException {
        disallowUpdate();
        return null;
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
