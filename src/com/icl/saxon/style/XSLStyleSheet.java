package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Stripper;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.trace.*;  // e.g.

import org.xml.sax.*;
import java.util.*;
import java.io.*;

/**
* An xsl:stylesheet or xsl:transform element in the stylesheet.<BR>
*/

public class XSLStyleSheet extends StyleElement {

    protected final static Name SAXONTRACE = Name.reconstruct("saxon", Namespace.SAXON, "trace");

                // true if diagnostic trace set
    //private boolean tracing = false;

                // true if this stylesheet was included by xsl:include, false if it is the
                // principal stylesheet or if it was imported
    private boolean wasIncluded = false;

                // the import precedence for top-level elements in this stylesheet
    private int precedence = 0;

                // the lowest precedence of any stylesheet imported by this one
    private int minImportPrecedence = 0;
    
                // the StyleSheet that included or imported this one; null for the principal stylesheet
    private XSLStyleSheet importer = null;
    
                // the PreparedStyleSheet object used to load this stylesheet
    private PreparedStyleSheet stylesheet;
    
                // the top-level elements in this logical stylesheet (after include/import)
    private Vector topLevel;
    
                // definitions of strip/preserve space action    
    private Stripper stripper = new Stripper();
    
                // definitions of template rules    
    private RuleManager ruleManager = new RuleManager();
    
                // definitions of keys
    private KeyManager keyManager = new KeyManager();
    
                // definitions of decimal formats
    private DecimalFormatManager decimalFormatManager = new DecimalFormatManager();
    
                // definitions of preview elements
    private PreviewManager previewManager = null;
    
                // media type (MIME type) of principal output
    private String mediaType;
    
                // flag that indicates whether namespace aliases are in use
    private boolean usesAliases = false;

                // count of the number of global parameters and variables
    private int numberOfVariables = 0;

                // count of the maximum umber of local variables in any template
    private int largestStackFrame = 0;


    /**
    * Create link to the owning PreparedStyleSheet object
    */

    public void setPreparedStyleSheet(PreparedStyleSheet sheet) {
        stylesheet = sheet;
    }
    
    /**
    * Get the owning PreparedStyleSheet object
    */

    public PreparedStyleSheet getPreparedStyleSheet() {
        if (importer!=null) return importer.getPreparedStyleSheet();
        return stylesheet;
    }

    /**
    * Get the RuleManager which handles template rules
    */

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    /**
    * Get the Stripper which handles whitespace stripping definitions
    */

    public Stripper getStripper() {
        return stripper;
    }
    
    /**
    * Get the KeyManager which handles key definitions
    */
    
    public KeyManager getKeyManager() {
        return keyManager;
    }

    /**
    * Get the DecimalFormatManager which handles decimal-format definitions
    */

    public DecimalFormatManager getDecimalFormatManager() {
        return decimalFormatManager;
    }

    /**
    * Get the PreviewManager which handles saxon:preview element definitions
    * @return null if there are no saxon:preview elements
    */

    public PreviewManager getPreviewManager() {
        return previewManager;
    }

    /**
    * Set the preview manager
    */

    public void setPreviewManager(PreviewManager pm) {
        previewManager = pm;
    }

    /**
    * Set the import precedence of this stylesheet
    */

    public void setPrecedence(int prec) {
        precedence = prec;
    }

    /**
    * Get the import precedence of this stylesheet
    */

    public int getPrecedence() {
        if (wasIncluded) return importer.getPrecedence();
        return precedence;
    }

    /**
    * Get the minimum import precedence of this stylesheet, that is, the lowest precedence
    * of any stylesheet imported by this one
    */

    public int getMinImportPrecedence() {
        return minImportPrecedence;
    }

    /**
    * Get the media type (MIME type) of the principal output of this stylesheet
    */

    public String getMediaType() {
        return mediaType;
    }

    /**
    * Set the StyleSheet that included or imported this one.
    */

    public void setImporter(XSLStyleSheet importer) {
        this.importer = importer;
    }

    /**
    * Get the StyleSheet that included or imported this one.
    * @return null if this is the principal stylesheet
    */

    public XSLStyleSheet getImporter() {
        return importer;
    }

    /**
    * Indicate that this stylesheet was included (by its "importer") using an xsl:include
    * statement as distinct from xsl:import
    */

    public void setWasIncluded() {
        wasIncluded = true;
    }

    /**
    * Determine whether this stylesheet was included (by its "importer") using an xsl:include
    * statement as distinct from xsl:import.
    */

    public boolean wasIncluded() {
        return wasIncluded;
    }

    /**
    * Get the top level elements in this stylesheet, after applying include/import
    */

    public Vector getTopLevel() {
        return topLevel;
    }

    /**
    * Allocate a slot number for a global variable or parameter
    */

    public int allocateSlotNumber() {
        return numberOfVariables++;
    }

    /**
    * Ensure there is enuogh space for local variables or parameters in any template
    */

    public void allocateLocalSlots(int n) {
        if (n > largestStackFrame) {
            largestStackFrame = n;
        }
    }

    /**
    * Prepare the attributes on the stylesheet element
    */

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"version", "id", "extension-element-prefixes", "exclude-result-prefixes" };
        allowAttributes(allowed);
    }

    /**
    * Process the version attribute - mandatory on this element
    */

    protected void processVersionAttribute() throws SAXException {
        version = getAttributeValue("version");
        if (version==null) {
            reportAbsence("version");
        }
    }

    /**
    * Get the declared namespace alias for a given namespace URI if there is one. If there is
    * more than one, we get the last.
    * @param uri The uri used in the stylesheet. 
    * @return The XSLNamespaceAlias element for this namespace, if there is one, or null if not
    */

    protected XSLNamespaceAlias getNamespaceAlias(String uri) throws SAXException {

        if (!usesAliases) return null;      // optimisation for the common case
        
        Vector children = topLevel;
        for (int i=children.size()-1; i>=0; i--) {
            Object child = children.elementAt(i);
            if (child instanceof XSLNamespaceAlias) {
                XSLNamespaceAlias xna = (XSLNamespaceAlias)child;
                if (xna.getStylesheetURI().equals(uri)) {
                    return xna;
                }
            }
        }
        return null;
    }


    /**
    * Validate this element
    */
    
    public void validate() throws SAXException {
        if (!isDocumentElement())
            throw new SAXException(getDisplayName() + " must be the outermost element");
    }

    /**
    * Preprocess does all the processing possible before the source document is available.
    * It is done once per stylesheet, so the stylesheet can be reused for multiple source
    * documents.
    */

    public void preprocess() throws SAXException {

        // process any xsl:include and xsl:import elements

        spliceIncludes();

        // process the attributes of every node in the tree

        processAllAttributes();

        // Validate the whole logical style sheet (i.e. with included and imported sheets)
                     
        validate();
        for (int i=0; i<topLevel.size(); i++) {
            Object node = topLevel.elementAt(i);
            if (node instanceof StyleElement) {
                ((StyleElement)node).validateSubtree();
            }
        }

        // Preprocess definitions of top-level elements.
        
        for (int i=0; i<topLevel.size(); i++) {
            Object s = topLevel.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    ((StyleElement)s).preprocess();
                } catch (SAXException err) {
                    throw ((StyleElement)s).styleError(err);
                }                    
            }
        }

        // Establish the media type for the output

        updateOutputDetails(new OutputDetails());
    }

    /**
    * Process the attributes of every node in the stylesheet
    */

    public void processAllAttributes() throws SAXException {
        prepareAttributes();
        Vector children = topLevel;
        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    ((StyleElement)s).processAllAttributes();
                } catch (SAXException err) {
                    throw ((StyleElement)s).styleError(err);
                }                    
            }
        }
    }

    /**
    * Allocate space in bindery for all the variables needed
    * This has to be done early to accommodate preview mode
    */

    public void initialiseBindery(Bindery bindery) throws SAXException {
       // ensure enough slots are available for global variables and for the largest stackframe

        bindery.allocateGlobals(numberOfVariables);
        bindery.allocateLocals(largestStackFrame);
    }

    /**
    * Update an output details object using the xsl:output elements in the stylesheet.
    */

    public void updateOutputDetails(OutputDetails details) throws SAXException {       
        Vector children = topLevel;
        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof XSLOutput) {
                try {
                    ((XSLOutput)s).setDetails(details);
                } catch (SAXException err) {
                    throw ((StyleElement)s).styleError(err);
                }                    
            }
        }
        mediaType = details.getMediaType();
    }

    /**
    * Process() is called once the source document is available. It activates those top-level
    * stylesheet elements that were not dealt with at preprocessing stage, notably
    * global variables and parameters
    */

    public void process( Context context ) throws SAXException {

        Controller sourceController = context.getController();

        String traceAtt = getAttributeValue(SAXONTRACE);
        if (traceAtt!=null && traceAtt.equals("yes")) {
            sourceController.setTraceListener(new com.icl.saxon.trace.SimpleTraceListener());
        }

        // ensure enough slots are available for global variables and for the largest stackframe

        initialiseBindery(context.getBindery());    // possibly redundant

        // process all the top-level elements

        Vector children = topLevel;

        boolean tracing = sourceController.isTracing();
        TraceListener listener = null;

    	if (tracing) { // e.g.
    	    listener = sourceController.getTraceListener();
    	    for (int i=0; i<children.size(); i++) {
        		Object s = children.elementAt(i);
        		listener.toplevel((NodeInfo)s);
    	    }
    	}

        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    if (tracing && !(s instanceof XSLTemplate)) {
                        listener.enter((StyleElement)s, context);                                                    
                        ((StyleElement)s).process(context);
                        listener.leave((StyleElement)s, context);
                    } else {
                        ((StyleElement)s).process(context);
                    }
                } catch (SAXException err) {
                    throw ((StyleElement)s).styleError(err);
                }
            } 
        }
    }

    /**
    * Process xsl:include and xsl:import elements.
    */

    public void spliceIncludes() throws SAXException {
        NodeInfo[] children = getAllChildNodes();
        int n = children.length;
        boolean foundNonImport = false;
        topLevel = new Vector(n);
        minImportPrecedence = precedence;
        StyleElement previousElement = this;
        
        for (int i=0; i<n; i++) {
            Object s = children[i];
            if (s instanceof TextInfo) {
                // in an embedded stylesheet, white space nodes may still be there
                if (!((TextInfo)s).isWhite()) {
                    throw previousElement.styleError(
                        "No character data is allowed between top-level elements");
                }
                
            } else {
                if (s instanceof XSLNamespaceAlias) {
                    usesAliases = true;
                }
                previousElement = (StyleElement)s;
                if (s instanceof XSLGeneralIncorporate) {
                    XSLGeneralIncorporate xslinc = (XSLGeneralIncorporate)s;
                    xslinc.processAttributes();
                
                    if (xslinc.isImport()) {
                        if (foundNonImport) throw xslinc.styleError("xsl:import elements must come first");
                    } else {
                        foundNonImport = true;
                    }

                    // get the included stylesheet. This follows the URL, builds a tree, and splices
                    // in any indirectly-included stylesheets.
                
                    XSLStyleSheet inc = xslinc.getIncludedStyleSheet(this, precedence);

                    // after processing the imported stylesheet and any others it brought in,
                    // adjust the import precedence of this stylesheet if necessary

                    if (xslinc.isImport()) {
                        precedence = inc.getPrecedence() + 1;
                    } else {
                        precedence = inc.getPrecedence();
                        inc.setWasIncluded();
                    }

                    // see if included stylesheet uses namespace aliases

                    if (inc.usesAliases) {
                        usesAliases = true;
                    }

                    // copy the top-level elements of the included stylesheet into the top level of this
                    // stylesheet. Normally we add these elements at the end, in order, but if the precedence
                    // of an element is less than the precedence of the previous element, we promote it.
                    // This implements the requirement in the spec that when xsl:include is used to
                    // include a stylesheet, any xsl:import elements in the included document are moved
                    // up in the including document to after any xsl:import elements in the including
                    // document.
               
                    Vector incchildren = inc.topLevel;
                    for (int j=0; j<incchildren.size(); j++) {
                        StyleElement elem = (StyleElement)incchildren.elementAt(j);
                        int last = topLevel.size() - 1;
                        if (last < 0 || elem.getPrecedence() >= ((StyleElement)topLevel.elementAt(last)).getPrecedence()) {
                            topLevel.addElement(elem);
                        } else {
                            while (last >=0 && elem.getPrecedence() < ((StyleElement)topLevel.elementAt(last)).getPrecedence()) {
                                last--;
                            }
                            topLevel.insertElementAt(elem, last+1);
                        }
                    }
                } else {
                    foundNonImport = true;
                    topLevel.addElement(s);
                }
            }
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
