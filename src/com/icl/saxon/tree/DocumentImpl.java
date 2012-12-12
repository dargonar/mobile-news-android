package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.Controller;
import com.icl.saxon.Context;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.EmbeddedStyleSheet;
import com.icl.saxon.PIGrabber;
import com.icl.saxon.KeyManager;
import com.icl.saxon.expr.*;
import com.icl.saxon.axis.AttributeEnumeration;
import com.icl.saxon.output.*;
import com.icl.saxon.pattern.Pattern;

import java.util.*;
import java.net.*;
import org.xml.sax.*;

import org.w3c.dom.*;

/**
  * A node in the XML parse tree representing the Document itself (or equivalently, the root
  * node of the Document).<P>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class DocumentImpl extends ParentNodeImpl
    implements DocumentInfo, Document, DOMImplementation {

    private static int nextDocumentNumber = 0;
    private static int nextSequenceNumber = 0;

    private ElementInfo documentElement;

    private Hashtable idTable = null;
    private int documentNumber;
    private Hashtable entityTable = new Hashtable();
    private String systemId;  // the base URI
    private StringBuffer characterBuffer;
    private LineNumberMap lineNumberMap;
    private BaseURIMap baseURIMap = new BaseURIMap();

    // list of indexes for keys. Each entry is a triple: KeyManager, Absolute Name of Key, Hashtable.
    // This reflects the fact that the same document may contain indexes for more than one stylesheet.

    private Object[] index = new Object[30];
    private int indexEntriesUsed = 0;

    public DocumentImpl() {
        synchronized(Boolean.TRUE) {                    
            documentNumber = nextDocumentNumber++;
        }
        parent = null;
    }

    /**
    * Set the character buffer
    */

    protected void setCharacterBuffer(StringBuffer buffer) {
        characterBuffer = buffer;
    }

    /**
    * Get the character buffer
    */

    public final StringBuffer getCharacterBuffer() {
        return characterBuffer;
    }

    /**
    * Set the top-level element of the document (variously called the root element or the
    * document element). Note that a DocumentImpl may represent the root of a result tree
    * fragment, in which case there is no document element.
    * @param e the top-level element
    */

    protected void setDocumentElement(ElementInfo e) {
        documentElement = e;
    }

    /**
    * Set the system id of this node
    */

    protected void setSystemId(String uri) {
        if (uri==null) uri = "*unknown.uri*";
        baseURIMap.setBaseURI(sequence, uri);
    }
        

    /**
    * Get the system id of this node
    */

    public String getSystemId() {
        return baseURIMap.getBaseURI(sequence);
    }

    /**
    * Set the system id of an element in the document
    */

    protected void setBaseURI(int seq, String uri) {
        if (uri==null) uri = "*unknown.uri*";
        baseURIMap.setBaseURI(seq, uri);
    }
        

    /**
    * Get the system id of an element in the document
    */

    protected String getBaseURI(int seq) {
        return baseURIMap.getBaseURI(seq);
    }


    /**
    * Set line numbering on
    */

    public void setLineNumbering() {
        lineNumberMap = new LineNumberMap();
        lineNumberMap.setLineNumber(sequence, 0);
    }

    /**
    * Set the line number for an element. Ignored if line numbering is off.
    */

    protected void setLineNumber(int sequence, int line) {
        if (lineNumberMap != null) {
            lineNumberMap.setLineNumber(sequence, line);
        }
    }

    /**
    * Get the line number for an element. Return -1 if line numbering is off.
    */

    protected int getLineNumber(int sequence) {
        if (lineNumberMap != null) {
            return lineNumberMap.getLineNumber(sequence);
        }
        return -1;
    }

    /**
    * Get the line number of this root node.
    * @return 0 always
    */

    public int getLineNumber() {
        return 0;
    }

    /**
    * Return the type of node.
    * @return NodeInfo.DOCUMENT (always)
    */

    public final short getNodeType() {
        return DOCUMENT;
    }

    /**
    * Get the name of this node, following the XPath rules 
    * @return null
    */

    public final Name getExpandedName() {
        return null;
    }

    /**
    * Get the name of this node, following the DOM rules 
    * @return "#document"
    */

    public final String getNodeName() {
        return "#document";
    }

    /**
    * Get next sibling - always null
    * @return null
    */

    public final Node getNextSibling() {
        return null;
    }

    /**
    * Get previous sibling - always null
    * @return null
    */

    public final Node getPreviousSibling()  {
        return null;
    }

    /**
     * Get the root (outermost) element.
     * @return the ElementInfo for the outermost element of the document.
     */
     
    public Element getDocumentElement() {
        return (ElementImpl)documentElement;
    }
    
    /**
    * Get the root (document) node
    * @return the DocumentInfo representing this document
    */

    public DocumentInfo getDocumentRoot() {
        return this;
    }

    /**
    * Get node number (level="single"). 
    * @return Always 1
    */

    public int getNodeNumber(Pattern pattern) {
        return 1;
    }

    /**
    * Get node number. 
    * @return Always 1
    */

    public int getNodeNumber() {
        return 1;
    }

    /**
    * Get node number (level="any"). 
    * @param pattern a Pattern that identifies which nodes should be counted (ignored)
    * @return Always 1
    */

    public int getNodeNumberAny(Pattern pattern) {
        return 1;
    }

    /**
    * Get a character string that uniquely identifies this node 
    * @return a string based on the document identifier
    */

    public String getSequentialKey() {
        return alphaKey(documentNumber);
    }

    /**
    * Get a unique number identifying this document
    */

    public int getDocumentNumber() {
        return documentNumber;
    }

    /**
    * Index all the ID attributes. This is done the first time the id() function
    * is used on this document
    */

    private void indexIDs() throws SAXException {
        if (idTable!=null) return;      // ID's are already indexed
        idTable = new Hashtable();

        NodeInfo curr = this;
        NodeInfo root = curr;
        while(curr!=null) {
            if (curr instanceof ElementInfo) {
                ElementInfo e = (ElementInfo)curr;
                AttributeList atts = e.getAttributeList();
                for (int i=0; i<atts.getLength(); i++) {
                    if (atts.getType(i).equals("ID")) {
                        // System.err.println("ID " + atts.getValue(i));
                        registerID(e, atts.getValue(i));
                    }
                }
            }
            curr = curr.getNextInDocument(root);
        }
    }

    /**
    * Register a unique element ID. Fails if there is already an element with that ID.
    * @param e The ElementInfo having a particular unique ID value
    * @param id The unique ID value
    */

    private void registerID(ElementInfo e, String id) throws SAXException {
        // the XPath spec (5.2.1) says ignore the second ID if it's not unique
        ElementInfo old = (ElementInfo)idTable.get(id);
        if (old==null) {
            idTable.put(id, e);
        }
        
    }

    /**
    * Get the element with a given ID.
    * @param id The unique ID of the required element, previously registered using registerID()
    * @return The ElementInfo for the given ID if one has been registered, otherwise null.
    */

    public ElementInfo selectID(String id) throws SAXException {
        if (idTable==null) indexIDs();
        return (ElementInfo)idTable.get(id);
    }

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param absname The absolute name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value. If no index
    * has been built, returns null.
    * @throws SAXExcetpion If the index is under construction, throws an
    * exception, as this implies a key defined in terms of itself.
    */

    public synchronized Hashtable getKeyIndex(KeyManager keymanager, String absname) throws SAXException {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager && ((String)index[k+1]).equals(absname)) {
                Object ix = index[k+2];
                if (ix instanceof Hashtable) {
                    return (Hashtable)index[k+2];
                } else {
                    throw new SAXException("Circular reference to key definition");
                }
            }
        }
        return null;
    }

    /**
    * Set the index for a given key. The method is synchronized because the same document
    * can be used by several stylesheets at the same time.
    * @param keymanager The key manager managing this key
    * @param absname The absolute name of the key (unique with the key manager)
    * @param keyindex the index, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value. Or the String
    * "under construction", indicating that the index is being built.
    */

    public synchronized void setKeyIndex(KeyManager keymanager, String absname, Object keyindex) throws SAXException {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager && ((String)index[k+1]).equals(absname)) {
                index[k+2] = keyindex;
                return;
            }
        }

        if (indexEntriesUsed+3 >= index.length) {
            Object[] index2 = new Object[indexEntriesUsed*2];
            System.arraycopy(index, 0, index2, 0, indexEntriesUsed);
            index = index2;
        }
        index[indexEntriesUsed++] = keymanager;
        index[indexEntriesUsed++] = absname;
        index[indexEntriesUsed++] = keyindex;
    }

    /**
    * Set an unparsed entity URI associated with this document. For system use only, while
    * building the document.
    */

    protected void setUnparsedEntity(String name, String uri) {
        entityTable.put(name, uri);
    }

    /**
    * Get the unparsed entity with a given name
    * @param name the name of the entity
    * @return the URI of the entity if there is one, or empty string if not
    */

    public String getUnparsedEntity(String name) {
        String uri = (String)entityTable.get(name);
        return (uri==null ? "" : uri);
    }

    /**
    * Get the URIs of the stylesheet associated with this document by means of an xml-stylesheet
    * processing instruction.
    * @param media The required medium, or null to match any medium
    * @param title The required title, or null to match the preferred stylesheet
    * @return null if there is no such processing instruction
    * @throws SAXException if there is such a processing instruction and it is invalid
    */

    public String[] getAssociatedStylesheets(String media, String title) throws SAXException {

        PIGrabber grabber = new PIGrabber();
        grabber.setCriteria(media, title, null);
        grabber.setBaseURI(getSystemId());
        
        NodeInfo[] children = getAllChildNodes();

        for (int i=0; i<children.length; i++) {
            NodeInfo node = children[i];
            if (node instanceof ProcInstInfo) {
                grabber.processingInstruction(node.getDisplayName(), node.getValue());
            }
            if (node instanceof ElementInfo) {
                break;
            }
        }

        return grabber.getStylesheetURIs();
    }

    /**
    * Prepare an embedded stylesheet within this document
    * @param id The id of the required embedded stylesheet
    * @return the prepared Stylesheet if there is one, or null.
    */

    public PreparedStyleSheet getEmbeddedStylesheet(String id) throws SAXException {

        Name stylesheet = Name.reconstruct("xsl", Namespace.XSLT, "stylesheet");
        Name transform = Name.reconstruct("xsl", Namespace.XSLT, "transform");
        NodeInfo node = this;
        while (node!=null) {                
            String nodeid = node.getAttributeValue("id");
            if (nodeid!=null && nodeid.equals(id) &&
                    (stylesheet.isNameOf(node) || transform.isNameOf(node))) {
                PreparedStyleSheet sheet = EmbeddedStyleSheet.build((ElementInfo)node);
                return sheet;
            }
            node = node.getNextInDocument(this);
        }
        return null;
    }


    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        NodeInfo next = (NodeInfo)getFirstChild();
        while (next!=null) {
            next.copy(out);
            next = (NodeInfo)next.getNextSibling();
        }
    }

    /**
    * Generate a path to this node
    */

    public String getPath() {
        return "/";
    }

    /**
    * Diagnostic string representation
    */

    public String toString() {
        return "Document node";
    }

    // DOM methods

    /**
     *  The Document Type Declaration (see <code>DocumentType</code> ) 
     * associated with this document. For HTML documents as well as XML 
     * documents without a document type declaration this returns 
     * <code>null</code> . The DOM Level 2 does not support editing the 
     * Document Type Declaration, therefore <code>docType</code> cannot be 
     * altered in any way, including through the use of methods, such as 
     * <code>insertNode</code> or <code>removeNode</code> , which are 
     * inherited from the <code>Node</code> interface.
     */
     
    public DocumentType getDoctype() {
        return null;
    }

    /**
     *  The <code>DOMImplementation</code> object that handles this document. 
     * A DOM application may use objects from multiple  implementations.
     */
     
    public DOMImplementation getImplementation() {
        return this;
    }

    /**
     *  Creates an element of the type specified. Note that the instance 
     * returned implements the <code>Element</code> interface, so attributes 
     * can be specified directly  on the returned object.
     */

    public Element createElement(String tagName) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Creates an empty <code>DocumentFragment</code> object. 
     * @return  A new <code>DocumentFragment</code> .
     */
    public DocumentFragment createDocumentFragment() {
        return null;
    }

    /**
     *  Creates a <code>Text</code> node given the specified string.
     * @param data  The data for the node.
     * @return  The new <code>Text</code> object.
     */
     
    public Text createTextNode(String data) {
        return null;
    }

    /**
     *  Creates a <code>Comment</code> node given the specified string.
     * @param data  The data for the node.
     * @return  The new <code>Comment</code> object.
     */
    public Comment createComment(String data) {
        return null;
    }

    /**
     *  Creates a <code>CDATASection</code> node whose value  is the specified 
     * string.
     * @param data  The data for the <code>CDATASection</code> contents.
     * @return  The new <code>CDATASection</code> object.
     * @exception DOMException
     *    NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
     
    public CDATASection createCDATASection(String data) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Creates a <code>ProcessingInstruction</code> node given the specified 
     * name and data strings.
     * @param target  The target part of the processing instruction.
     * @param data  The data for the node.
     * @return  The new <code>ProcessingInstruction</code> object.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified target contains an 
     *   illegal character.
     *   <br> NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
     
    public ProcessingInstruction createProcessingInstruction(String target, 
                                                             String data)
                                                             throws DOMException {
        return null;
    }

    /**
     *  Creates an <code>Attr</code> of the given name. Note that the 
     * <code>Attr</code> instance can then be set on an <code>Element</code> 
     * using the <code>setAttributeNode</code> method. 
     * <br> To create an attribute with a qualified name and namespace URI, use
     *  the <code>createAttributeNS</code> method.
     * @param name  The name of the attribute.
     * @return  A new <code>Attr</code> object with the <code>nodeName</code> 
     *   attribute set to <code>name</code> , and <code>localName</code> , 
     *   <code>prefix</code> , and <code>namespaceURI</code> set to 
     *   <code>null</code> .
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     */
     
    public Attr createAttribute(String name) throws DOMException {
        return null;
    }

    /**
     *  Creates an <code>EntityReference</code> object. In addition, if the 
     * referenced entity is known, the child list of the 
     * <code>EntityReference</code> node is made the same as that of the 
     * corresponding <code>Entity</code> node. If any descendant of the 
     * <code>Entity</code> node has an unbound  namespace prefix , the 
     * corresponding descendant of the created <code>EntityReference</code> 
     * node is also unbound; (its <code>namespaceURI</code> is 
     * <code>null</code> ). The DOM Level 2 does not support any mechanism to 
     * resolve namespace prefixes.
     * @param name  The name of the entity to reference. 
     * @return  The new <code>EntityReference</code> object.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     *   <br> NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
     
    public EntityReference createEntityReference(String name) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Returns a <code>NodeList</code> of all the <code>Elements</code> with 
     * a given tag name in the order in which they are encountered in a 
     * preorder traversal of the <code>Document</code> tree. 
     * @param tagname  The name of the tag to match on. The special value "*" 
     *   matches all tags.
     * @return  A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code> .
     */
     
    public NodeList getElementsByTagName(String tagname) {
        Vector v = new Vector();
        NodeInfo next = this;
        while(next!=null) {
            if (next instanceof ElementInfo) {
                if (tagname.equals("*") || tagname.equals(next.getDisplayName())) {
                    v.addElement(next);
                }
            }
            next = next.getNextInDocument(this);
        }
        return new NodeSetExtent(v);
    }
        

    /**
     *  Imports a node from another document to this document.
     * @exception DOMException
     *    NOT_SUPPORTED_ERR: Raised if the type of node being imported is not 
     *   supported.
     * @since DOM Level 2
     */
     
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Creates an element of the given qualified name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the element to create.
     * @param qualifiedName  The  qualified name of the element type to 
     *   instantiate.
     * @return  A new <code>Element</code> object with the following 
     *   attributes: Attribute Value<code>Node.nodeName</code>
     *   <code>qualifiedName</code><code>Node.namespaceURI</code>
     *   <code>namespaceURI</code><code>Node.prefix</code> prefix, extracted 
     *   from <code>qualifiedName</code> , or <code>null</code> if there is no
     *    prefix<code>Node.localName</code> local name , extracted from 
     *   <code>qualifiedName</code><code>Element.tagName</code>
     *   <code>qualifiedName</code>
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code> or an empty string, 
     *   or if the <code>qualifiedName</code> has a prefix that is "xml" and 
     *   the <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/XML/1998/namespace "  .
     * @since DOM Level 2
     */
     
    public Element createElementNS(String namespaceURI, 
                                   String qualifiedName)
                                   throws DOMException
    {
        return null;
    }

    /**
     *  Creates an attribute of the given qualified name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to create.
     * @param qualifiedName  The  qualified name of the attribute to 
     *   instantiate.
     * @return  A new <code>Attr</code> object with the following attributes: 
     *   Attribute Value<code>Node.nodeName</code> qualifiedName
     *   <code>Node.namespaceURI</code><code>namespaceURI</code>
     *   <code>Node.prefix</code> prefix, extracted from 
     *   <code>qualifiedName</code> , or <code>null</code> if there is no 
     *   prefix<code>Node.localName</code> local name , extracted from 
     *   <code>qualifiedName</code><code>Attr.name</code>
     *   <code>qualifiedName</code>
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code> or an empty string, 
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and the 
     *   <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/XML/1998/namespace ", or if the 
     *   <code>qualifiedName</code> is "xmlns" and the 
     *   <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/2000/xmlns/ ".
     * @since DOM Level 2
     */
     
    public Attr createAttributeNS(String namespaceURI, 
                                  String qualifiedName)
                                  throws DOMException {
        return null;
    }

    /**
     *  Returns a <code>NodeList</code> of all the <code>Elements</code> with 
     * a given  local name and namespace URI in the order in which they are 
     * encountered in a preorder traversal of the <code>Document</code> tree.
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
     *  Returns the <code>Element</code> whose <code>ID</code> is given by 
     * <code>elementId</code> . If no such element exists, returns 
     * <code>null</code> . Behavior is not defined if more than one element 
     * has this <code>ID</code> .  The DOM implementation must have 
     * information that says which attributes are of type ID. Attributes with 
     * the name "ID" are not of type ID unless so defined. Implementations 
     * that do not know whether attributes are of type ID or not are expected 
     * to return <code>null</code> .
     * @param elementId  The unique <code>id</code> value for an element.
     * @return  The matching element.
     * @since DOM Level 2
     */
     
    public Element getElementById(String elementId) {
        try {
            return (ElementImpl)selectID(elementId);
        } catch (SAXException err) {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods to implement the DOMImplementation interface
    /////////////////////////////////////////////////////////////////////////

     /**
     *  Test if the DOM implementation implements a specific feature.
     * @param feature  The name of the feature to test (case-insensitive).
     * @param version  This is the version number of the feature to test.
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
     
    public boolean hasFeature(String feature, String version) {
        return feature.equalsIgnoreCase("xml");
    }
                                

    /**
     *  Creates an empty <code>DocumentType</code> node. 
     * @param qualifiedName  The  qualified name of the document type to be 
     *   created. 
     * @param publicId  The external subset public identifier.
     * @param systemId  The external subset system identifier.
     * @return  A new <code>DocumentType</code> node with 
     *   <code>Node.ownerDocument</code> set to <code>null</code> .
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed.
     * @since DOM Level 2
     */
     
    public DocumentType createDocumentType(String qualifiedName, 
                                           String publicId, 
                                           String systemId)
                                           throws DOMException
    {
        disallowUpdate();
        return null;
    }

    /**
     *  Creates an XML <code>Document</code> object of the specified type with 
     * its document element. 
     * @param namespaceURI  The  namespace URI of the document element to 
     *   create.
     * @param qualifiedName  The  qualified name of the document element to be 
     *   created.
     * @param doctype  The type of document to be created or <code>null</code>. 
     * @return  A new <code>Document</code> object.
     * @exception DOMException
     * @since DOM Level 2
     */
    public Document createDocument(String namespaceURI, 
                                   String qualifiedName, 
                                   DocumentType doctype)
                                   throws DOMException
    {
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
// The Original Code is: all this file except PB-SYNC section. 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions marked PB-SYNC are Copyright (C) Peter Bryant (pbryant@bigfoot.com). All Rights Reserved. 
//
// Contributor(s): Michael Kay, Peter Bryant. 
//
