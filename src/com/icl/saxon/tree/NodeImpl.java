package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.Context;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.pattern.NodeTestPattern;
import com.icl.saxon.pattern.NamedNodePattern;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.output.Outputter;

import java.util.Vector;
import java.util.Hashtable;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.w3c.dom.*;



/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top-level class in the implementation class hierarchy; it essentially contains
  * all those methods that can be defined using other primitive methods, without direct access
  * to data.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

abstract class NodeImpl implements NodeInfo {
    
    protected static NodeInfo[] emptyArray = new NodeInfo[0];

    protected ParentNodeImpl parent;
    protected int index;

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNode(NodeInfo other) {
        // default implementation: differs for attribute and namespace nodes
        return this==other;
    }

    /**
    * Get the base URL for the node. Default implementation for child nodes.
    */

    public String getSystemId() {
        return parent.getSystemId();
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    * This is the default implementation for child nodes.
    */

    public long getSequenceNumber() {
        NodeInfo prev = this;
        for (int i=0;; i++) {
            if (prev instanceof ParentNodeImpl) {
                return prev.getSequenceNumber() + 0x10000 + i;
                // note the 0x10000 is to leave room for namespace and attribute nodes.
            }
            prev = prev.getPreviousInDocument();
        }
        
    }

    /**
    * Get the name of this node, following the DOM rules 
    * @return The name of the node. For an element this is the element name, for an attribute
    * it is the attribute name, as a QName. Other node types return conventional names such
    * as "#text" or "#comment"
    */

    public String getNodeName() {
        // default implementation
        return getDisplayName();
    }

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return null.
    */

    public String getPrefix() {
        return null;
    }

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, or for
    * an element or attribute in the default namespace, return an empty string.
    */

    public String getURI() {
        Name name = getExpandedName();
        if (name==null) return "";
        return name.getURI();
    }
    
    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node. 
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        Name name = getExpandedName();
        if (name==null) return "";
        return name.getDisplayName();
    }

    /**
    * Get the absolute name of this node. For elements and attributes this is [uri^]localname.
    * For other nodes, it is the same as the display name
    * @return The absolute name of this node. 
    * For a node with no name, return an empty string.
    */

    public String getAbsoluteName() {
        Name name = getExpandedName();
        if (name==null) return "";
        return name.getAbsoluteName();
    }

    /**
    * Get the local name of this node. 
    * @return The local name of this node. 
    * For a node with no name, return an empty string.
    */

    public String getLocalName() {
        Name name = getExpandedName();
        if (name==null) return "";
        return name.getLocalName();
    }

    /**
    * Test if the name of the node (including namespaces) is equivalent to the given name
    */

    public boolean hasName(Name name) {
        Name thisname = getExpandedName();
        if (thisname==null) return false;
        return thisname.equals(name);
    }
    
    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return parent.getLineNumber();
    }

    /**
    * Get the column number of the node. This is not currently maintained, so return -1
    */

    public int getColumnNumber() {
        return -1;
    }

    /**
    * Get the public identifier of the document entity containing this node. This
    * is not currently maintained: return null
    */

    public String getPublicId() {
        return null;
    }

    /**
    * Get the index of this node, i.e. its position among its siblings
    */

    public final int getIndex() {
        return index;
    }

    /**
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    public final Node getParentNode()  {
        return parent;
    }

    /**
    * Get the previous sibling of the node
    * @return The previous sibling node. Returns null if the current node is the first
    * child of its parent.
    */
    
    public Node getPreviousSibling()  {
        return parent.getNthChild(index-1);
    }


   /**
    * Get next sibling node
    * @return The next sibling node of the required type. Returns null if the current node is the last
    * child of its parent.
    */

    public Node getNextSibling()  {
        return parent.getNthChild(index+1);
    }    

    /**
    * Get first child - default implementation used for leaf nodes
    * @return null
    */

    public Node getFirstChild()  {
        return null;
    }

    /**
    * Get last child - default implementation used for leaf nodes
    * @return null
    */

    public Node getLastChild()  {
        return null;
    }

    /**
    * Get the number of children.
    */

    public int getNumberOfChildren() {
        return 0;
    }

    /**
    * Get all child nodes of the node - default implementation used for leaf nodes
    * @return an empty array
    */

    public NodeInfo[] getAllChildNodes()  {
        return emptyArray;
    }

    /**
    * Determine whether the node is of a given type. <P>
    * Note, this can also be done by testing the node using "instanceof". But this is
    * inconvenient when passing the class as a parameter to another routine.
    * @param nodeType One of the specific node types such as ELEMENT or TEXT, or the general
    * node type NODE
    * @return true if the node is an instance of the specified node type
    */

    public final boolean isa(int nodeType) {
        return (nodeType==NODE || nodeType==getNodeType());
    }
    
    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param name the name of an attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue( Name name ) {
        return null;
    }


    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param name the name of an attribute. This must be an unqualified attribute name,
     * i.e. one with no namespace prefix.
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue( String name ) {
        return null;
    }

     /**
     * Get the nearest ancestor element with a given name
     * @param name The name of the required ancestor. 
     * @return The NodeInfo for the nearest ancestor with the
     * given tag; null if there is no such ancestor
     */

    public final ElementInfo getAncestor(Name name) {
        NodeImpl ancestor = (NodeImpl)this.getParentNode();
        while (ancestor!=null && (ancestor instanceof ElementInfo) &&
                              !ancestor.hasName(name)) {
            ancestor = (NodeImpl)ancestor.getParentNode();
        }
        if (ancestor instanceof DocumentInfo) return null;
        return (ElementInfo)ancestor;
    }

     /**
     * Get the nearest ancestor node that matches the given pattern
     * @param pattern A pattern that the ancestor node must satisfy.
     * @return The NodeInfo for the nearest ancestor node that matches the
     * given pattern; null if there is no such ancestor
     */

    public final NodeInfo getAncestor(Pattern pat, Context c) throws SAXException
    {
        NodeInfo p = (NodeInfo)getParentNode();
        while (p!=null) {
            if (pat.matches(p, c)) return p;
            else p=(NodeInfo)p.getParentNode();
        }
        return null;
    }
    
    /**
     * Determine whether this node is the outermost element.
     * @return True if this element is the document (outermost) element.
     */
     
    public boolean isDocumentElement() {
        return (this instanceof ElementInfo && getParentNode() instanceof DocumentInfo);
    }

    /**
    * Determine whether this node is an ancestor of another node
    * @param other the other node (the putative descendant of this node)
    * @return true of this node is an ancestor of the other node
    */

    public boolean isAncestor(NodeInfo other) {
        NodeInfo parent = (NodeInfo)other.getParentNode();
        if (parent==null) return false;
        if (parent==this) return true;
        return isAncestor(parent);
    }

    /**
     * Get the outermost element.
     * @return the ElementInfo for the outermost element of the document. If the document is
     * not well-formed, this returns the last element child of the root if there is one, otherwise
     * null.
     */
     
    public Element getDocumentElement() {
        return ((DocumentImpl)getDocumentRoot()).getDocumentElement();

    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        NodeImpl p = this;
        while (true) {
            p = (NodeImpl)p.getParentNode();
            if (p instanceof DocumentInfo) return (DocumentInfo)p;
        }
    }

    /**
    * Get the next sibling node that matches a given pattern.
    * @param pattern The match-pattern that the required sibling must match
    * @return The NodeInfo object describing the next node at the same level.
    * Returns null if there is no subsequent child of the same parent that matches the supplied
    * pattern.
    */
    
    public final NodeInfo getNextSibling(Pattern pattern, Context c) throws SAXException {
        NodeInfo next=(NodeInfo)getNextSibling();
        while (next!=null) {
            if (pattern.matches(next, c)) return next;
            next=(NodeInfo)next.getNextSibling();
        } 
        return null;
    }

    /**
    * Get the next node in document order
    * @param anchor: the scan stops when it reaches a node that is not a descendant of the specified
    * anchor node
    * @return the next node in the document, or null if there is no such node
    */

    public NodeInfo getNextInDocument(NodeInfo anchor) {
        // find the first child node if there is one; otherwise the next sibling node
        // if there is one; otherwise the next sibling of the parent, grandparent, etc, up to the anchor element.
        // If this yields no result, return null.

        NodeInfo next = (NodeInfo)getFirstChild();
        if (next!=null) return next;
        if (this==anchor) return null;
        next = (NodeInfo)getNextSibling();
        if (next!=null) return next;
        NodeInfo parent = this;
        while (true) {
            parent = (NodeInfo)parent.getParentNode();
            if (parent==null) return null;
            if (parent==anchor) return null;
            next = (NodeInfo)parent.getNextSibling();
            if (next!=null) return next;
        }
    }

    /**
    * Get the first child node matching a given pattern
    * @param pattern the pattern to be matched
    * @return the the first child node of the required type, or null if there is no such
    * child
    */

    public final NodeInfo getFirstChild(Pattern pattern, Context c) throws SAXException {
        NodeInfo n = (NodeInfo)getFirstChild();
        while (n!=null) {
            if (pattern.matches(n, c)) return n;
            n = (NodeInfo)n.getNextSibling();
        }
        return null;
    }

    /**
    * Get the previous sibling of the node that matches a given pattern.
    * @param pattern The match-pattern that the required sibling must match
    * @return The NodeInfo object describing the previous node at the same level that
    * matches the pattern. Returns null if there is no previous child of the same
    * parent that matches the supplied pattern.
    */
    
    public final NodeInfo getPreviousSibling(Pattern pattern, Context c) throws SAXException {
        NodeInfo prev=(NodeInfo)getPreviousSibling();
        while (prev!=null) {
            if (pattern.matches(prev, c)) return prev;
            prev=(NodeInfo)prev.getPreviousSibling();
        } 
        return null;
    }

    /**
    * Get the previous node in document order
    * @return the previous node in the document, or null if there is no such node
    */

    public NodeInfo getPreviousInDocument() {

        // finds the last child of the previous sibling if there is one;
        // otherwise the previous sibling element if there is one;
        // otherwise the parent, up to the anchor element.
        // If this reaches the document root, return null.

        NodeInfo prev = (NodeInfo)getPreviousSibling();
        if (prev!=null) {
            return ((NodeImpl)prev).getLastDescendantOrSelf();
        }
        return (NodeInfo)getParentNode();
    }

    private NodeInfo getLastDescendantOrSelf() {
        NodeImpl last = (NodeImpl)getLastChild();
        if (last==null) return this;
        return last.getLastDescendantOrSelf();
    }

    /**
    * Get the previous node in document order
    * @param pattern: identifies a pattern the required node
    * @return the previous node in the document of the required type, or null if there is no
    * such node
    */

    public final NodeInfo getPreviousInDocument(Pattern pattern, Context c) throws SAXException {
        NodeInfo next = this;
        while(true) {
            next = (NodeInfo)next.getPreviousInDocument();
            if (next==null) return null;
            if (pattern.matches(next, c)) return next;
        }
    }

    /**
    * Get the last child node matching a given pattern
    * @param pattern the pattern to be matched
    * @return the the last child node of the required type, or null if there is no such
    * child
    */

    public final NodeInfo getLastChild(Pattern pattern, Context c) throws SAXException{
        NodeInfo n = (NodeInfo)getLastChild();
        while (n!=null) {
            if (pattern.matches(n, c)) return n;
            n = (NodeInfo)n.getPreviousSibling();
        }
        return null;
    }

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws SAXException {
        out.writeContent(getValue());   // default implementation
    }
    
    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL.
    * @param context Used for remembering previous result, for performance
    */

    public int getNumberSimple(Context context) {
        int i=1;
        int type = getNodeType();
        Name name = getExpandedName();
        NodeInfo prev = (NodeInfo)getPreviousSibling();

        if (prev!=null && prev.getNodeType()==type && prev.hasName(name)) {
            int memo = context.getRememberedNumber(prev);
            if (memo>0) {
                context.setRememberedNumber(this, ++memo);
                return memo;
            }
        }
        
        while (prev!=null) {
            if (prev.getNodeType()==type && (name==null || prev.hasName(name))) i++;
            prev = (NodeInfo)prev.getPreviousSibling();
        }
        context.setRememberedNumber(this, i);
        return i;
    }

    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL. This version doesn't require
    * the context, and therefore doesn't remember previous results
    */

    public int getNumberSimple()  {
        int i=1;
        int type = getNodeType();
        Name name = getExpandedName();
        NodeInfo prev = (NodeInfo)getPreviousSibling();

        while (prev!=null) {
            if (prev.getNodeType()==type && (name==null || prev.hasName(name))) i++;
            prev = (NodeInfo)prev.getPreviousSibling();
        }

        return i;
    }

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

    public int getNumberSingle(Pattern count, Pattern from, Context context) throws SAXException {

        if (count==null && from==null) {
            return getNumberSimple(context);
        }
        
        if (count==null) {
            if (getExpandedName()==null) {
                count = new NodeTestPattern(getNodeType());
            } else {
                count = new NamedNodePattern(getNodeType(), getExpandedName());
            }
        }

        NodeInfo searchFrom;
        if (from==null) {
            searchFrom = getDocumentRoot();
        } else {
            searchFrom = getAncestor(from, context);
        }
        
        NodeInfo curr = this;
        while (!count.matches(curr, context)) {
            curr = (NodeInfo)curr.getParentNode();
            if (curr==null || curr==searchFrom) return 0;
        }
        
        int num = 0;
        NodeInfo prev = curr;
        while (prev!=null) {
            num++;
            prev = (NodeInfo)prev.getPreviousSibling(count, context);
        }

        return num;
    }

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

    public int getNumberAny(Pattern count, Pattern from, Context context) throws SAXException {
            
        if (count==null) {
            if (getExpandedName()==null) {
                count = new NodeTestPattern(getNodeType());
            } else {
                count = new NamedNodePattern(getNodeType(), getExpandedName());
            }
        }

        NodeInfo curr = this;
        int num = 0;

        while(true) {
            if (curr==null) break;
            if (from!=null && from.matches(curr, context)) break;
            if (count.matches(curr, context)) num++;
            curr = (NodeInfo)curr.getPreviousInDocument();
        }

        return num;
    }

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

    public Vector getNumberMulti(Pattern count, Pattern from, Context context) throws SAXException {
    
        Vector v = new Vector();

        if (count==null) {
            if (getExpandedName()==null) {
                count = new NodeTestPattern(getNodeType());
            } else {
                count = new NamedNodePattern(getNodeType(), getExpandedName());
            }
        }

        NodeInfo curr = this;

        while(true) {
            if (count.matches(curr, context)) {
                int num = curr.getNumberSingle(count, null, context);
                v.insertElementAt(new Integer(num), 0);
            }
            curr = (NodeInfo)curr.getParentNode();
            if (curr==null) break;
            if (from!=null && from.matches(curr, context)) break;
        }

        return v;
    }

    /**
    * Get a character string that uniquely identifies this node and that collates nodes
    * into document order
    * @return a string. The string is always interned so keys can be compared using "==".
    */

    public String getSequentialKey() {
        NodeInfo curr = this;
        StringBuffer key = new StringBuffer();
        while(!(curr instanceof DocumentInfo)) {
            key.insert(0, alphaKey(curr.getIndex()));
            curr = (NodeInfo)curr.getParentNode();   
        }
        key.insert(0, curr.getSequentialKey());
        return key.toString();
    }

    /**
    * Construct an alphabetic key from an positive integer; the key collates in the same sequence
    * as the integer
    * @param value The positive integer key value (negative values are treated as zero).
    */

    protected static String alphaKey(int value) {
        if (value<1) return "a";
        if (value<10) return "b" + value;
        if (value<100) return "c" + value;
        if (value<1000) return "d" + value;
        if (value<10000) return "e" + value;
        if (value<100000) return "f" + value;
        if (value<1000000) return "g" + value;
        if (value<10000000) return "h" + value;
        if (value<100000000) return "i" + value;
        if (value<1000000000) return "j" + value;
        return "k" + value;
    }

    /**
    * Create a string that identifies the node (for diagnostic purposes only)
    */

    public String toString() {
        String s;
        s = getDisplayName();
        if (s=="") {
            NodeInfo n = (NodeInfo)getParentNode();
            if (n != null) s = n.getExpandedName() + "/";
            s += "\"" + getValue().trim() + "\"";
        }
        s = s + "(" + getIndex() + ")";
        return s;
    }

    /**
    * Remove this node from the tree. For system use only.
    * When one or more nodes have been removed, renumberChildren()
    * must be called to adjust the numbering of remaining nodes.
    * PRECONDITION: The node must have a parent node.
    */

    public void removeNode() throws SAXException {
        parent.removeChild(index);
    }

    /**
    * Translate numeric node type to a string representation
    */

    public static String getNodeTypeName(int type) {
        switch(type) {
            case NODE:      return "NODE";
            case ELEMENT:   return "ELEMENT";
            case TEXT:      return "TEXT";
            case ATTRIBUTE: return "ATTRIBUTE";
            case DOCUMENT:  return "DOCUMENT";
            case PI:        return "PI";
            case COMMENT:   return "COMMENT";
            default:        return "NODE";
        }
    }

    // implement DOM Node methods

    /**
    * Get the node value as defined in the DOM. This is not the same as the XPath string-value.
    */

    public String getNodeValue() {
        // default implementation
        return getValue();
    }

    /**
    * Set the node value. DOM method: always fails
    */

    public void setNodeValue(String nodeValue) throws DOMException {
        disallowUpdate();
    }

    /**
     * Return a <code>NodeList</code> that contains all children of this node. If 
     * there are no children, this is a <code>NodeList</code> containing no 
     * nodes.
     */
     
    public NodeList getChildNodes() {
        return new NodeSetExtent(getAllChildNodes());
    }

    /**
     * Return a <code>NamedNodeMap</code> containing the attributes of this node (if 
     * it is an <code>Element</code> ) or <code>null</code> otherwise. (DOM method)
     */
     
    public NamedNodeMap getAttributes() {
        // default implementation
        return null;
    }

    /**
     * Return the <code>Document</code> object associated with this node. (DOM mehod)
     */
     
    public Document getOwnerDocument() {
        return (Document)(DocumentImpl)getDocumentRoot();
    }

    /**
     * Insert the node <code>newChild</code> before the existing child node 
     * <code>refChild</code>. DOM method: always fails.
     * @param newChild  The node to insert.
     * @param refChild  The reference node, i.e., the node before which the 
     *   new node must be inserted.
     * @return  The node being inserted.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */
     
    public Node insertBefore(Node newChild, 
                             Node refChild)
                             throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Replace the child node <code>oldChild</code> with 
     * <code>newChild</code> in the list of children, and returns the 
     * <code>oldChild</code> node. Always fails.
     * @param newChild  The new node to put in the child list.
     * @param oldChild  The node being replaced in the list.
     * @return  The node replaced.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */
     
    public Node replaceChild(Node newChild, 
                             Node oldChild)
                             throws DOMException{
        disallowUpdate();
        return null;
    }
    
    /**
     * Remove the child node indicated by <code>oldChild</code> from the 
     * list of children, and returns it. DOM method: always fails.
     * @param oldChild  The node being removed.
     * @return  The node removed.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */
     
    public Node removeChild(Node oldChild) throws DOMException {
        disallowUpdate();
        return null;
    }
    
    /**
     *  Adds the node <code>newChild</code> to the end of the list of children 
     * of this node. DOM method: always fails.
     * @param newChild  The node to add.
     * @return  The node added.
     * @exception DOMException
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */
     
    public Node appendChild(Node newChild) throws DOMException {
        disallowUpdate();
        return null;
    }
    
    /**
     * Determine whether the node has any children.
     * @return  <code>true</code> if the node has any children, 
     *   <code>false</code> if the node has no children.
     */
     
    public boolean hasChildNodes() {
        return getFirstChild() != null;         
    }

    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy 
     * constructor for nodes. The duplicate node has no parent. Not
     * implemented: always returns null. (Because trees are read-only, there
     * would be no way of using the resulting node.)
     * @param deep  If <code>true</code> , recursively clone the subtree under 
     *   the specified node; if <code>false</code> , clone only the node 
     *   itself (and its attributes, if it is an <code>Element</code> ).  
     * @return  The duplicate node.
     */
     
    public Node cloneNode(boolean deep) {
        // Not implemented 
        return null;
    }

    /**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree 
     * underneath this <code>Node</code>, including attribute nodes, into a 
     * "normal" form where only structure (e.g., elements, comments, 
     * processing instructions, CDATA sections, and entity references) 
     * separates <code>Text</code> nodes, i.e., there are neither adjacent 
     * <code>Text</code> nodes nor empty <code>Text</code> nodes.
     * @since DOM Level 2
     */
     
    public void normalize() {
        // null operation; nodes are always normalized
    }

    /**
     *  Tests whether the DOM implementation implements a specific feature and 
     * that feature is supported by this node.
     * @param feature  The name of the feature to test. This is the same name 
     *   which can be passed to the method <code>hasFeature</code> on 
     *   <code>DOMImplementation</code> .
     * @param version  This is the version number of the feature to test. In 
     *   Level 2, version 1, this is the string "2.0". If the version is not 
     *   specified, supporting any version of the feature will cause the 
     *   method to return <code>true</code> .
     * @return  Returns <code>true</code> if the specified feature is supported
     *    on this node, <code>false</code> otherwise.
     * @since DOM Level 2
     */
     
    public boolean supports(String feature, 
                            String version) {
        return false;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is 
     * unspecified.
     * <br> This is not a computed value that is the result of a namespace 
     * lookup based on an examination of the namespace declarations in scope. 
     * It is merely the namespace URI given at creation time.
     * <br> For nodes of any type other than <code>ELEMENT_NODE</code> and 
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 
     * method, such as <code>createElement</code> from the 
     * <code>Document</code> interface, this is always <code>null</code> . 
     * Per the  Namespaces in XML Specification  an attribute does not 
     * inherit its namespace from the element it is attached to. If an 
     * attribute is not explicitly given a namespace, it simply has no 
     * namespace.
     * @since DOM Level 2
     */
     
    public String getNamespaceURI() {
        String uri = getURI();
        return (uri=="" ? null : uri);
    }

    /**
    * Set the namespace prefix of this node. Always fails.
    */
    
    public void setPrefix(String prefix)
                            throws DOMException {
        disallowUpdate();
    }

    /**
    * Internal method used to indicate that update operations are not allowed
    */

    protected void disallowUpdate() throws DOMException {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                               "The Saxon DOM cannot be updated");
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes, 
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
     
    public boolean hasAttributes() {
        return false;
    }

     /**
     * Test if the DOM implementation implements a specific feature.
     * @param feature  The name of the feature to test (case-insensitive).
     * @param version  This is the version number of the feature to test.
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
     
    public boolean isSupported(String feature, String version) {
        return feature.equalsIgnoreCase("xml");
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
