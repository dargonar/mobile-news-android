package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.ElementWithAttributes;
import com.icl.saxon.expr.*;
import com.icl.saxon.handlers.NodeHandler;
import com.icl.saxon.output.*;
import com.icl.saxon.trace.*;  // e.g.

import org.xml.sax.*;
import org.w3c.dom.Node;

import java.util.*;
import java.io.*;
import java.text.*;

/**
* Abstract superclass for all element nodes in the stylesheet. <BR>
* Note: this class implements Locator. The element
* retains information about its own location in the stylesheet, which is useful when
* an XSL error is found. 
*/

public abstract class StyleElement extends ElementWithAttributes
        implements Locator, StaticContext {

    protected Vector attributeSets = null;
    private Vector extensionNamespaces = new Vector();
    private Vector excludedNamespaces = new Vector();
    protected String version = null;

    /**
    * Constructor creates an empty attribute list
    */

    public StyleElement() {}

    /**
    * Make this node a substitute for a temporary one previously added to the tree. See
    * StyleNodeFactory for details. "A node like the other one in all things but its class".
    * Note that at this stage, the node will not yet be known to its parent, though it will
    * contain a reference to its parent; and it will have no children.
    */

    public void substituteFor(StyleElement temp) throws SAXException {
        this.parent = temp.parent;
        this.attributeList = temp.attributeList;
        this.namespaceList = temp.namespaceList;
        this.fullName = temp.fullName;
        this.sequence = temp.sequence;
        this.attributeSets = temp.attributeSets;
        this.extensionNamespaces = temp.extensionNamespaces;
        this.excludedNamespaces = temp.excludedNamespaces;
        this.version = temp.version;
    }       

    /**
    * Determine whether this node is an instruction. The default implementation says it isn't.
    */

    public boolean isInstruction() {
        return false;
    }

    /**
    * Get the import precedence of this stylesheet element. 
    */

    public int getPrecedence() throws SAXException {
        return ((XSLStyleSheet)getDocumentElement()).getPrecedence();
    }

    /**
    * Process the attributes of this element and all its children
    */

    public void processAllAttributes() throws SAXException {
        processAttributes();
        NodeInfo child = (NodeInfo)getFirstChild();
        while (child != null) {
            if (child instanceof StyleElement) {
                ((StyleElement)child).processAllAttributes();          
            }
            child = (NodeInfo)child.getNextSibling();
        }
    }

    /**
    * Process the attribute list for the element. This is a wrapper method that calls
    * prepareAttributes (provided in the subclass) and traps any exceptions
    */

    public final void processAttributes() throws SAXException {
        try {
            prepareAttributes();
        } catch (SAXException err) {
            throw styleError(err);
        }
    }

    /**
    * Test that all attributes are included in the permitted list of attributes for the
    * element. More precisely, that each attribute is either in the permitted list of
    * attributes or is in a non-default namespace.
    * @param allowed An array of strings indicating the permitted attribute names
    * @throws SAXException if an attribute is present that isn't in the approved list.
    */

    public void allowAttributes(String[] allowed) throws SAXException {
        
        // we use a Bloom filter, setting three independent hash bits for each permitted
        // attribute; there is a small probability of a non-permitted attribute
        // being allowed through, but it doesn't matter

        // Note: in principle we could calculate the hash masks once per element
        // type, we are actually doing it once per instance.
        
        int hash1 = 0;
        int hash2 = 0;
        int hash3 = 0;
        for (int i=0; i<allowed.length; i++) {
            String att = allowed[i];
            int h = att.hashCode();
            hash1 |= (1 << (h % 31));
            hash2 |= (1 << (h % 29));
            hash3 |= (1 << (h % 23));
        }        

        for (int j=0; j<attributeList.getLength(); j++) {
            String att = attributeList.getName(j);
            if (att.indexOf(':') < 0) {     
                int h = att.hashCode();
                int m1 = (1 << (h % 31));
                int m2 = (1 << (h % 29));
                int m3 = (1 << (h % 23));
                if (((hash1 & m1) != m1) || ((hash2 & m2) != m2) || ((hash3 & m3) != m3)) {
                    //System.err.println("Failing hash = " + att.hashCode());
                    //System.err.println("att & hash1 = " + (hash1 & att.hashCode()));
                    throw styleError("Attribute " + att + " is not allowed on this element");
                }
            }
        }
    }
    

    /**
    * Set the attribute list for the element. This is called to process the attributes (note
    * the distinction from processAttributes in the superclass).
    * Must be supplied in a subclass
    */

    public abstract void prepareAttributes() throws SAXException;


    /**
    * Determine whether "xsl:" prefix is required on standard attributes: true for literal result
    * elements, false otherwise
    */

    public boolean requiresXSLprefix() {
        return false;
    }

    /**
    * Process the [xsl:]extension-element-prefixes attribute if there is one
    */

    protected void processExtensionElementAttribute() throws SAXException {
        String ext;
        if (requiresXSLprefix()) {
            Name aname = new Name("xsl", Namespace.XSLT, "extension-element-prefixes");
            ext = getAttributeValue(aname);
        } else {
            ext = getAttributeValue("extension-element-prefixes");
        }
        if (ext!=null) {
            StringTokenizer st = new StringTokenizer(ext);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                String uri = (s.equals("#default") ? "" : getURIforPrefix(s));
                extensionNamespaces.addElement(uri);
            }            
        }
    }

    /**
    * Process the [xsl:]exclude-result-prefixes attribute if there is one
    */

    protected void processExcludedNamespaces() throws SAXException {
        String ext;
        if (requiresXSLprefix()) {
            Name aname = new Name("xsl", Namespace.XSLT, "exclude-result-prefixes");
            ext = getAttributeValue(aname);
        } else {
            ext = getAttributeValue("exclude-result-prefixes");
        }
        if (ext!=null) {
            StringTokenizer st = new StringTokenizer(ext);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                String uri = (s.equals("#default") ? getURIforPrefix("") : getURIforPrefix(s));
                excludedNamespaces.addElement(uri);
            }            
        }
    }

    /**
    * Process the [xsl:]version attribute if there is one
    */

    protected void processVersionAttribute() throws SAXException {
        if (requiresXSLprefix()) {
            Name aname = new Name("xsl", Namespace.XSLT, "version");
            version = getAttributeValue(aname);
        } else {
            version = getAttributeValue("version");
        }
    }

    /**
    * Get the version number on this element, or inherited from its ancestors
    */

    public String getVersion() throws SAXException {
        return version;
    }

    /**
    * Determine whether forwards-compatible mode is enabled for this element
    */

    public boolean forwardsCompatibleModeIsEnabled() throws SAXException {
        if (version==null) {
            NodeInfo node = (NodeInfo)getParentNode();
            if (node instanceof StyleElement) {
                return ((StyleElement)node).forwardsCompatibleModeIsEnabled();
            } else {
                return true;
            }
        } 
        return !(version.equals("1.0"));
        // that's what the spec says - I would have expected "> 1.0", but we do what we're told
    }
        
    /**
    * Check whether a particular extension element namespace is defined on this node.
    * This checks this node only, not the ancestor nodes.
    * The implementation checks whether the prefix is included in the
    * [xsl:]extension-element-prefixes attribute.
    * @param namespace the namespace URI being tested
    */

    protected boolean definesExtensionElement(String namespace) throws SAXException {
        return (extensionNamespaces.indexOf(namespace)>=0);
    }

    /**
    * Check whether a namespace uri defines an extension element. This checks whether the
    * namespace is defined as an extension namespace on this or any ancestor node.
    * @param uri the namespace URI being tested
    */

    public boolean isExtensionNamespace(String uri) throws SAXException {
        NodeInfo anc = this;
        while (anc instanceof StyleElement) {
            if (((StyleElement)anc).definesExtensionElement(uri)) {
                return true;
            }
            anc = (NodeInfo)anc.getParentNode();
        }
        return false;
    }

    /**
    * Check whether this node excludes a particular namespace from the result.
    * This method checks this node only, not the ancestor nodes.
    * @param namespace the namespace URI being tested
    */

    protected boolean definesExcludedNamespace(String namespace) throws SAXException {
        return (excludedNamespaces.indexOf(namespace)>=0);
    }

    /**
    * Check whether a namespace uri defines an namespace excluded from the result.
    * This checks whether the namespace is defined as an excluded namespace on this
    * or any ancestor node.
    * @param uri the namespace URI being tested
    */

    public boolean isExcludedNamespace(String uri) throws SAXException {
        if (isExtensionNamespace(uri)) return true;
        if (uri.equals(Namespace.XSLT)) return true;
        NodeInfo anc = this;
        while (anc instanceof StyleElement) {
            if (((StyleElement)anc).definesExcludedNamespace(uri)) {
                return true;
            }
            anc = (NodeInfo)anc.getParentNode();
        }
        return false;
    }

    /**
    * Check that the element is valid. This is called once for each element, after
    * the entire tree has been built. As well as validation, it can perform first-time
    * initialisation. The default implementation does nothing; it is normally overriden
    * in subclasses.
    */

    public void validate() throws SAXException {}

    /**
    * Default preprocessing method does nothing. It is implemented for those top-level elements
    * that can be evaluated before the source document is available, for example xsl:key,
    * xsl:attribute-set, xsl:template, xsl:locale
    */

    public void preprocess() throws SAXException {}

    /**
    * Recursive walk through the stylesheet to validate all nodes
    */

    public void validateSubtree() throws SAXException {
        try {
            validate();
        } catch (SAXException err) {
            throw styleError(err);
        }
        NodeInfo child = (NodeInfo)getFirstChild();
        while (child != null) {
            if (child instanceof StyleElement) {
                ((StyleElement)child).validateSubtree();
            }
            child = (NodeInfo)child.getNextSibling();
        }
    }

    /**
    * Get the containing XSLStyleSheet node. This gets the principal style sheet, i.e. the
    * one originally loaded, that forms the root of the import/include tree
    */

    protected XSLStyleSheet getPrincipalStyleSheet() throws SAXException {
        XSLStyleSheet sheet = (XSLStyleSheet)getDocumentElement();
        while (true) {
            XSLStyleSheet next = sheet.getImporter();
            if (next==null) return sheet;
            sheet = next;
        }
    }

    /**
    * Get the PreparedStyleSheet object.
    * @return the PreparedStyleSheet to which this stylesheet element belongs
    */

    protected PreparedStyleSheet getPreparedStyleSheet() throws SAXException {
        return getPrincipalStyleSheet().getPreparedStyleSheet();
    }

    /**
    * Check that the stylesheet element is within a template body
    * @throws SAXException if not within a template body
    */

    public void checkWithinTemplate() throws SAXException {
        NodeInfo parent = (NodeInfo)getParentNode();
        
        // first check is a bit too broad, but it covers all extension elements, LREs, etc
        if (!(parent.getURI().equals(Namespace.XSLT))) return;
        if (parent instanceof XSLAttribute) return;
        if (parent instanceof XSLComment) return;
        if (parent instanceof XSLCopy) return;
        if (parent instanceof XSLElement) return;
        if (parent instanceof XSLFallback) return;
        if (parent instanceof XSLForEach) return;
        if (parent instanceof XSLIf) return;
        if (parent instanceof XSLMessage) return;
        if (parent instanceof XSLOtherwise) return;
        if (parent instanceof XSLParam) return;
        if (parent instanceof XSLProcessingInstruction) return;
        if (parent instanceof XSLTemplate) return;
        if (parent instanceof XSLVariable) return;
        if (parent instanceof XSLWhen) return;        
        if (parent instanceof XSLWithParam) return;
        if (parent instanceof AbsentExtensionElement) return;   
        throw styleError("Element must only be used within a template body");
    }

    /**
    * Convenience method to check that the stylesheet element is at the top level
    * @throws SAXException if not at top level
    */

    public void checkTopLevel() throws SAXException {
        if (!(getParentNode() instanceof XSLStyleSheet)) {
            throw styleError("Element must only be used at top level of stylesheet");
        }
    }

    /**
    * Convenience method to check that the stylesheet element is not at the top level
    * @throws SAXException if it is at the top level
    */

    public void checkNotTopLevel() throws SAXException {
        if (getParentNode() instanceof XSLStyleSheet) {
            throw styleError("Element must not be used at top level of stylesheet");
        }
    }

    /**
    * Convenience method to check that the stylesheet element is empty
    * @throws SAXException if it is not empty
    */

    public void checkEmpty() throws SAXException {
        if (getFirstChild()!=null) {
            throw styleError("Element must be empty");
        }
    }

    /**
    * Convenience method to report the absence of a mandatory attribute
    * @throws SAXException if the attribute is missing
    */

    public void reportAbsence(String attribute) throws SAXException {
        throw styleError("Element must have a \"" + attribute + "\" attribute");
    }

    /**
    * Process: called to do the real work of this stylesheet element. This method
    * must be implemented in each subclass.
    * @param context The context in the source XML document, giving access to the current node,
    * the current variables, etc.
    */

    public abstract void process(Context context) throws SAXException;

    /**
    * Process the children of this node in the stylesheet
    * @param context The context in the source XML document, giving access to the current node,
    * the current variables, etc.
    */

    public void processChildren(Context context) throws SAXException {

    	if (context.getController().isTracing()) { // e.g.
    	    TraceListener listener = context.getController().getTraceListener();

    	    NodeInfo node = (NodeInfo)getFirstChild();
    	    while (node!=null) {

        		listener.enter(node, context);

        		if (node instanceof TextInfo) {
        		    node.copy(context.getOutputter());
        		} else if (node instanceof StyleElement) {
        		    StyleElement snode = (StyleElement)node;
        		    try {
        			    snode.process(context);
        		    } catch (SAXException err) {
        			    throw snode.styleError(err);
        		    }                    
        		}

        		listener.leave(node, context);
        		node = (NodeInfo)node.getNextSibling();
    	    }

    	} else {

    	    NodeInfo node = (NodeInfo)getFirstChild();
    	    while (node!=null) {

        		if (node instanceof TextInfo) {
        		    node.copy(context.getOutputter());
        		} else if (node instanceof StyleElement) {
        		    StyleElement snode = (StyleElement)node;
        		    try {
        			    snode.process(context);
        		    } catch (SAXException err) {
        			    throw snode.styleError(err);
        		    }                    
        		}
        		node = (NodeInfo)node.getNextSibling();
    	    }

    	}
    }

    // diagnostic version of process()

    private void processX(Context context) throws SAXException {
        System.err.println("Processing " + context.getCurrentNode() + " using " + this);
        process(context);
    }

    /**
    * Modify the "select" expression to include any sort keys specified. Used in XSLForEach
    * and XSLApplyTemplates
    */

    protected Expression handleSortKeys(Expression select) throws SAXException {
        // handle sort keys if any

        SortedSelection sortExpression = null;
        Node child = getFirstChild();

        while(child!=null) {
            if (child instanceof XSLSort) {
                if (sortExpression==null) {
                    sortExpression = new SortedSelection(select);                
                } 
                sortExpression.addSortKey(
                    ((XSLSort)child).getSortKeyDefinition());               
            }
            child = child.getNextSibling();
        }
        if (sortExpression == null) {
            return new NodeListExpression(select);  // sorts into document order
        } else {
            return sortExpression;
        }
    }

    /**
    * Expand the stylesheet elements subordinate to this one, returning the result
    * as a string. The expansion must not generate any element or attribute nodes.
    * @param context The context in the source document
    */

    public String expandChildren(Context context) throws SAXException {
        Controller c = context.getController();
        TextFragment details = new TextFragment();
        c.setNewOutputDetails(details);
        processChildren(context);
        c.resetOutputDetails();
        return details.getText();
    }


    /**
    * Determine the list of attribute-sets associated with this element.
    * This is used for xsl:element, xsl:copy, xsl:attribute-set, and on literal
    * result elements
    */

    protected void findAttributeSets(String use) throws SAXException {

        attributeSets = new Vector();
        
        XSLStyleSheet stylesheet = getPrincipalStyleSheet();
        Vector toplevel = stylesheet.getTopLevel();

        StringTokenizer st = new StringTokenizer(use);
        while (st.hasMoreTokens()) {
            String asetname = st.nextToken();
            String fullname = (new Name(asetname, this, false)).getAbsoluteName();
            boolean found = false;

            // search for the named attribute set, using all of them if there are several with the
            // same name
            for (int i=0; i<toplevel.size(); i++) {
                if (toplevel.elementAt(i) instanceof XSLAttributeSet) {
                    XSLAttributeSet t = (XSLAttributeSet)toplevel.elementAt(i);
                    String tname = t.getAttributeSetName();
                    if (tname!=null && tname.equals(fullname)) {  
                        attributeSets.addElement(t);
                        found = true;
                    }
                }
            }
            if (!found) {
                throw styleError("No attribute-set exists named " + asetname);
            }            
        }
    }

    /**
    * Expand the attribute sets referenced in this element's use-attribute-sets attribute
    */

    protected void processAttributeSets(Context context) throws SAXException {
        if (attributeSets==null) return;
        for (int i=0; i<attributeSets.size(); i++) {
            XSLAttributeSet aset = (XSLAttributeSet)attributeSets.elementAt(i);
            aset.expand(context);
        }
    }

    /**
    * Construct an exception with diagnostic information
    */

    protected SAXException styleError(SAXException error) {
        if (error instanceof StyleException) return error;
        if (error.getException()!=null) {
            return new StyleException(error.getException(),
                                      positionMessage(error.getMessage()));
        }
        return new StyleException(error, positionMessage(error.getMessage()));
    }

    protected SAXException styleError(String message) {
        return new StyleException(positionMessage(message));
    }

    private String positionMessage(String message) {
        String name = getDisplayName();
        if (name.equals("")) {
            name = "element";
        }
        String systemId = getSystemId();
        return "At " + name +
               (systemId==null ? "" : " on line " + getLineNumber() + " of " + systemId) +
               ": " + message;
    }

    /**
    * Test whether this is a top-level element
    */

    public boolean isTopLevel() throws SAXException {
        return (getParentNode() instanceof XSLStyleSheet);
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    * @name The absolute name of the variable (prefixed by namespace URI)
    * @return a Binding for the variable
    * @throw SAXException if the variable has not been declared
    */

    public Binding bindVariable(String name) throws SAXException {
        Binding binding = getVariableBinding(name);
        if (binding==null) {
            throw styleError("Variable " + name + " has not been declared");
        }
        return binding;
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    * @name The absolute name of the variable (prefixed by namespace URI)
    * @return a Binding for the variable, or null if it has not been declared
    */

    public Binding getVariableBinding(String name) throws SAXException {
        NodeInfo curr = this;
        NodeInfo prev = this;

        // first search for a local variable declaration
        
        if (!isTopLevel()) {
            while (true) {
                curr = (NodeInfo)curr.getPreviousSibling();
                while (curr==null) {
                    curr = (NodeInfo)prev.getParentNode();
                    prev = curr;
                    if (curr.getParentNode() instanceof XSLStyleSheet) break;   // top level
                    curr = (NodeInfo)curr.getPreviousSibling();
                }
                if (curr.getParentNode() instanceof XSLStyleSheet) break;
                if (curr instanceof Binding) {
                    String varname = ((Binding)curr).getVariableName();
                    if (varname.equals(name)) {
                        return (Binding)curr;
                    }
                }
            }
        }
       
        // Now check for a global variable
        // we rely on the search following the order of decreasing import precedence.
        
        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=toplevel.size()-1; i>=0; i--) {
            Object child = toplevel.elementAt(i);
            if (child instanceof Binding && child != this) {
                String varname = ((Binding)child).getVariableName();
                if (varname.equals(name)) {
                    return (Binding)child;
                }
            }
        }
        
        return null;
    }

    /**
    * List the variables that are in scope for this stylesheet element. 
    * Designed for a debugger, not used by the processor.
    * @return two Enumeration of Strings, the global ones [0] and the local ones [1]
    */
    
    public Enumeration[] getVariableNames() throws SAXException {  // e.g.
        Hashtable local = new Hashtable();
        Hashtable global = new Hashtable();

        NodeInfo curr = this;
        NodeInfo prev = this;

        // first collect the local variable declarations
        
        if (!isTopLevel()) {
            while (true) {
                curr = (NodeInfo)curr.getPreviousSibling();
                while (curr==null) {
                    curr = (NodeInfo)prev.getParentNode();
                    prev = curr;
                    if (curr.getParentNode() instanceof XSLStyleSheet) break;   // top level
                    curr = (NodeInfo)curr.getPreviousSibling();
                }
                if (curr.getParentNode() instanceof XSLStyleSheet) break;
                if (curr instanceof Binding) {
                    String varname = ((Binding)curr).getVariableName();
        		    if (local.get(varname)==null) {
        			    local.put(varname, varname);
        		    }
                }
            }
        }

        // Now collect the global variables
        // we rely on the search following the order of increasing import precedence.
        
        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=0; i<toplevel.size(); i++) {
            Object child = toplevel.elementAt(i);
            if (child instanceof Binding && child != this) {
                String varname = ((Binding)child).getVariableName();
        		if (local.get(varname)==null) {
        		    global.put(varname, varname);
        		}
            }
        }

    	Enumeration info[] = new Enumeration[2];
    	info[0] = global.keys();
    	info[1] = local.keys();
        return info;
    }

    /**
    * Get a Function declared using a saxon:function element in the stylesheet
    * @param name the name of the function
    * @return the Function object represented by this saxon:function; or null if not found
    */

    public Function getStyleSheetFunction(Name name) throws SAXException {

        // we rely on the search following the order of decreasing import precedence.
        
        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=toplevel.size()-1; i>=0; i--) {
            Object child = toplevel.elementAt(i);
            if (child instanceof SAXONFunction &&
                    ((SAXONFunction)child).getFunctionName().equals(name)) {
                StyleSheetFunctionCall fc = new StyleSheetFunctionCall();
                fc.setFunction((SAXONFunction)child);
                return fc;
            }
        }
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
