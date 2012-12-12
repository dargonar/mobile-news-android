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
  * A node in the XML parse tree representing an XML element.<P>
  * This class is an implementation of ElementInfo
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 8 August 2000: separated from ElementImpl
  */

// The name of the element and its attributes are now namespace-resolved by the
// parser. However, this class retains the ability to do namespace resolution for other
// names, for example variable and template names in a stylesheet.

public class ElementWithAttributes extends ElementImpl
    implements ElementInfo, Element, NamedNodeMap {

    private static AttributeCollection emptyAtts = new AttributeCollection();
    
    protected AttributeCollection attributeList;      // this excludes namespace attributes
    protected String[] namespaceList = null;          // organised as prefix/uri pairs
            // note that this namespace list includes only the namespaces actually defined on
            // this element, not those inherited from outer elements.

    /**
    * Construct an empty ElementWithAttributes

    public ElementWithAttributes() {}

    /**
    * Initialise a new ElementWithAttributes with an element name and attribute list
    * @param name The element name, with namespaces resolved
    * @param atts The attribute list, after namespace processing
    * @param parent The parent node    
    */

    public void initialise(Name name, AttributeCollection atts, NodeInfo parent,
                            String baseURI, int lineNumber, int sequenceNumber)
    throws SAXException {
        this.fullName = name;
        this.attributeList = atts;
        this.parent = (ParentNodeImpl)parent;
        this.sequence = sequenceNumber;
        DocumentImpl doc = (DocumentImpl)getDocumentRoot();
        doc.setLineNumber(sequenceNumber, lineNumber);
        doc.setBaseURI(sequenceNumber, baseURI);
    }

    /**
    * Set the namespace declarations to the element
    */

    public void setNamespaceDeclarations(String[] namespaces, int namespacesUsed) throws SAXException {
        namespaceList = new String[namespacesUsed];
        System.arraycopy(namespaces, 0, namespaceList, 0, namespacesUsed);
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
        if (namespaceList!=null) {
            for (int i=0; i<namespaceList.length; i+=2) {
                if (namespaceList[i].equals(prefix)) {
                    return namespaceList[i+1];
                }
            }
        }
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
        if (namespaceList!=null) {
            for (int i=0; i<namespaceList.length; i+=2) {
                if (namespaceList[i+1].equals(uri)) {
                    return namespaceList[i];
                }
            }
        }
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

        if (namespaceList!=null) {
            int max = list.size();
            for (int i=0; i<namespaceList.length; ) {
                String prefix = namespaceList[i++];
                String uri = namespaceList[i++];

                boolean found = false;

                // Don't add a node if the prefix is already in the list
                for (int j=0; j<max; ) {
                    NamespaceInfo ns = (NamespaceInfo)list.elementAt(j++);
                    if (ns.getNamespacePrefix().equals(prefix)) {
                        found=true;
                        break;
                    }
                }
                if (!found) {
                    list.addElement(
                        new NamespaceImpl(
                            (ElementImpl)owner, prefix, uri, list.size()+1));
                }
            }
        }

        // now add the namespaces defined on the ancestor nodes
        
        if (!(parent instanceof DocumentInfo || parent==stop)) {
            ((ElementInfo)parent).addNamespaceNodes(owner, list, stop);
        }
                
    }
    
    /**
    * Output all namespace nodes associated with this element.
    * @param out The relevant outputter
    */

    public void outputNamespaceNodes(Outputter out) throws SAXException {

        if (namespaceList!=null) {
            for (int i=0; i<namespaceList.length; ) {
                String prefix = namespaceList[i++];
                String uri = namespaceList[i++];

                out.writeNamespaceDeclaration(prefix, uri, false);

            }
        }

        // now add the namespaces defined on the ancestor nodes. We rely on the outputter
        // to eliminate multiple declarations of the same prefix
        
        if (!(parent instanceof DocumentInfo)) {
            ((ElementInfo)parent).outputNamespaceNodes(out);
        }
    }
    
    
    /**
    * Get the attribute list for this element.
    * @return The attribute list. This will not include any
    * namespace attributes. The attribute names will be in expanded form, with prefixes
    * replaced by URIs
    */
    
    public AttributeCollection getAttributeList() {
        return attributeList;
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
        return attributeList.getValue(name);
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
        return attributeList.getValue(name);
    }

    /**
    * Make an attribute node for a given attribute of this element
    * @param name The attribute name
    */

    public AttributeInfo makeAttributeNode(Name attributeName) {
        return new AttributeImpl(this, attributeName);
    }

    
    /**
    * Set the value of an attribute on the current element. This affects subsequent calls
    * of getAttribute() for that element. 
    * @param name The name of the attribute to be set. Any prefix is interpreted relative
    * to the namespaces defined for this element. 
    * @param value The new value of the attribute. Set this to null to remove the attribute.
    */
    
    public void setAttribute(String name, String value ) throws DOMException {
        if (attributeList.getLength()==0) {
            // empty attribute lists can be shared between different elements
            attributeList = new AttributeCollection();
        }
        try {
            Name attname = new Name(name, this, false);
            attributeList.setAttribute(attname, value);
        } catch (SAXException err) {
            throw new DOMExceptionImpl((short)9999, err.getMessage());
        }       
    }
    
    /**
    * Copy this node to a given outputter (supporting xsl:copy-of)
    */

    public void copy(Outputter out) throws SAXException {
        out.writeStartTag(fullName);

        // output the namespaces
        
        outputNamespaceNodes(out);

        // output the attributes
        
        for (int i=0; i<attributeList.getLength(); i++) {
            out.writeAttribute(attributeList.getExpandedName(i),
                               attributeList.getValue(i));
        }

        // output the children

        NodeInfo next = (NodeInfo)getFirstChild();
        while (next!=null) {
            next.copy(out);
            next = (NodeInfo)next.getNextSibling();
        }

        out.writeEndTag(fullName);
    }    

    ////////////////////////////////////////////////////////////////////////////
    // Following interfaces are provided to implement the DOM Element interface
    ////////////////////////////////////////////////////////////////////////////
    

    /**
     * Retrieves an attribute value by name. This is a DOM method, so namespace
     * declarations count as attributes.
     * @param name  The name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     */
     
    public String getAttribute(String name) {
        try {
            if (name.equals("xmlns")) {
                return getURIforPrefix("");
            } else if (name.startsWith("xmlns:")) {
                return getURIforPrefix(name.substring(6));
            } else {
                Name fullname = makeName(name, false);
                String val = getAttributeValue(fullname);
                return (val==null ? "" : val);
            }
        } catch (SAXException err) {
            return "";
        }
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
        try {
            if (name.equals("xmlns")) {
                for(int j=0; j<namespaceList.length; j+=2) {
                    if ((namespaceList[j]).equals("")) {
                        return new NamespaceAttribute("", namespaceList[j+1]);
                    }
                }
                return null;
            } else if (name.startsWith("xmlns:")) {
                String prefix = name.substring(6);
                for(int j=0; j<namespaceList.length; j+=2) {
                    if (namespaceList[j].equals(prefix)) {
                        return new NamespaceAttribute(prefix, namespaceList[j+1]);
                    }
                }
                return null;
            } else {
                Name fullname = makeName(name, false);
                return (AttributeImpl)makeAttributeNode(fullname);
            }
        } catch (SAXException err) {
            return null;
        }
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
     * Retrieves an attribute value by local name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     * @since DOM Level 2
     */
     
    public String getAttributeNS(String namespaceURI, String localName) {
        String prefix = getPrefixForURI(namespaceURI);
        if (prefix==null) return "";        
        Name name = Name.reconstruct(prefix, namespaceURI, localName);
        return getAttribute(name.getDisplayName());      
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
        String prefix = getPrefixForURI(namespaceURI);
        if (prefix==null) return null;
        Name name = Name.reconstruct(prefix, namespaceURI, localName);
        return getAttributeNode(name.getDisplayName());
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
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes, 
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
     
    public boolean hasAttributes() {
        return attributeList.getLength()>0;
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
        try {
            if (name.equals("xmlns")) {
                getURIforPrefix("");                    // throws SAXException if absent
                return true;
            } else if (name.startsWith("xmlns:")) {
                getURIforPrefix(name.substring(6));     // throws SAXException if absent
                return true;
            } else {
                Name fullname = makeName(name, false);
                return getAttributeValue(fullname) != null;
            }
        } catch (SAXException err) {
            return false;
        }
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
        String prefix = getPrefixForURI(namespaceURI);
        if (prefix==null) return false;
        Name name = Name.reconstruct(prefix, namespaceURI, localName);
        return hasAttribute(name.getDisplayName());
    }    

    //////////////////////////////////////////////////////////////////////
    // Methods to implement DOM NamedNodeMap (the set of attributes)
    //////////////////////////////////////////////////////////////////////

    /**
    * Get named attribute (DOM NamedNodeMap method)
    * Treats namespace declarations as attributes.    
    */

    public Node getNamedItem(String name) {
        return getAttributeNode(name);
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
        if (index<0) {
            return null;
        }
        if (index>=attributeList.getLength()) {
            if (namespaceList==null) {
                return null;
            }
            int j = index - attributeList.getLength();
            if (j*2 < namespaceList.length) {
                String prefix = namespaceList[j*2];
                String uri = namespaceList[j*2+1];
                return new NamespaceAttribute(prefix, uri);
            }
            return null;
        }
        return makeAttributeNode(attributeList.getExpandedName(index));
    }

    /**
    * Get number of attributes (DOM NamedNodeMap method). 
    * Treats namespace declarations as attributes.
    */

    public int getLength() {
        return attributeList.getLength() +
            (namespaceList==null ? 0 : namespaceList.length/2);
    }

    /**
    * Get named attribute (DOM NamedNodeMap method)
    * Treats namespace declarations as attributes.
    */

    public Node getNamedItemNS(String uri, String localName) {
        return getAttributeNodeNS(uri, localName);
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

    /////////////////////////////////////////////////////////////////////////
    // A DOM Attr node that represents a namespace declaration
    /////////////////////////////////////////////////////////////////////////

    class NamespaceAttribute extends AttributeImpl {

        public NamespaceAttribute(String prefix, String uri) {
            try {
                parent = ElementWithAttributes.this;
                if (prefix.equals("")) {
                    name = new Name("xmlns");
                } else {
                    name = new Name("xmlns", Namespace.XMLNS, prefix);
                }
                value = uri;
            } catch (SAXException err) {}
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
