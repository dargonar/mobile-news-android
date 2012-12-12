package com.icl.saxon.om;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.AttributeCollection;

import java.util.*;
import java.io.Writer;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;


/**
  * A node in the XML parse tree representing an XML element.<P>
  * The ElementInfo provides information about the element and its context.
  * Information available includes the tag and attributes of the element, and
  * pointers to the parent element and the previous element at the same level.<P>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 17 June 1999 - becomes an interface
  */

public interface ElementInfo extends NodeInfo {

    
    /**
    * Get the attribute list for this element.
    * @return The attribute list (as in the SAX interface). If you are using the Distributor and
    * you have not called setOptions(RETAIN_ATTRIBUTES) and this is not the startElement() call
    * for the element, the attribute list will be null.
    */
    
    public AttributeCollection getAttributeList();
    
    /**
    * Return the immediate character content of the element. 
    * @return the accumulated character content of this element, excluding child elements
    */

    //public String getContent() throws SAXException;
   
    /**
    * Find the value of an inherited attribute. The current element, its parent,
    * and its ancestors are searched recursively to find an attribute with the given
    * name.<br>
    * @param name the name of the attribute
    * @return the value of the attribute, if it is defined on this element or 
    * on an ancestor element; otherwise null
    */

    public String getInheritedAttribute( Name name ) throws SAXException;
    
    /**
    * Set the value of an attribute on the current element. This affects subsequent calls
    * of getAttribute() for that element. It also alters the value of the attribute in
    * the DOM if using the DOM.
    * @param name The name of the attribute to be set. 
    * @param value The new value of the attribute. Set this to null to remove the attribute.
    */
    
    public void setAttribute(String name, String value ) throws SAXException;

    /**
    * Make an attribute node for a given attribute of this element
    * @param name The attribute name
    */

    public AttributeInfo makeAttributeNode(Name attributeName) throws SAXException;

   /**
    * Search the NamespaceList for a given prefix, returning the corresponding URI.
    * @param prefix The prefix to be matched. To find the default namespace, supply ""
    * @return The URI corresponding to this namespace. If it is an unnamed default namespace,
    * return "".
    * @throws SAXException if the prefix has not been declared on this element or a containing
    * element.
    */

    public String getURIforPrefix(String prefix) throws SAXException;

    /**
    * Search the NamespaceList for a given URI, returning the corresponding prefix.
    * @param uri The URI to be matched. To find the default namespace, supply ""
    * @return The prefix corresponding to this URI. If not found, return null.
    */

    public String getPrefixForURI(String uri);

    /**
    * Make the set of all namespace nodes associated with this element.
    * @param owner The element node to own these namespace nodes
    * @param list a vector containing NamespaceInfo objects representing the namespaces
    * in scope for this element; the method appends nodes to this Vector, which should
    * initially be empty.
    * @param stop the ancestor node to stop at. Supply null to go all the way back to
    * the Document node
    */

    public void addNamespaceNodes(ElementInfo owner, Vector list, NodeInfo stop) throws SAXException;

    /**
    * Output all namespace nodes associated with this element.
    * @param out The relevant outputter
    */

    public void outputNamespaceNodes(Outputter out) throws SAXException;

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

    public boolean isFirstInGroup() throws SAXException;

    /**
     * Determine whether this element is the last in a consecutive
     * group. A consecutive group is a group of elements of the same
     * type subordinate to the same parent element; there can be intervening
     * character data, white space, or processing instructions, but no
     * elements of a different type.
     * @return True if this is the last child of its parent, or if the
     * next child is a different element type, or if this element 
     * is the root.
     * @exception SAXException Note that this method is supported when
     * using direct access to the document (using the Wanderer);
     * when processing serially using the Distributor, it is supported using lookahead
     * during end-tag processing only
     */

    public boolean isLastInGroup() throws SAXException;
    
    /**
     * Determine whether this element is the first child of its parent.
     * @return True if this element is the first child of its parent, or
     * if it is the root element.
     */
     
    public boolean isFirstChild() throws SAXException;

    /**
     * Determine whether this element is the last child element of its parent.
     * @return True if this element is the last child element of its parent, or
     * if it is the root element.
     * @exception SAXException Note that this method is only supported when
     * using direct access to the document (using the Wanderer); it throws an exception
     * if used when processing serially using the Distributor.
     */
     
    public boolean isLastChild() throws SAXException;

    
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
