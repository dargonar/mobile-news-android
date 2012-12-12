package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.*;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import java.util.*;

/**
  * Class StyleNodeFactory. <br>
  * A Factory for nodes in the stylesheet tree. <br>
  * Currently only allows Element nodes to be user-constructed.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 23 June 1999
  */

public class StyleNodeFactory implements NodeFactory {

    Hashtable XSLstyles = new Hashtable();
    Hashtable SAXONstyles = new Hashtable();
    Hashtable UserStyles = new Hashtable();

    public StyleNodeFactory() {
	    XSLstyles.put("apply-imports",     XSLApplyImports.class);
	    XSLstyles.put("apply-templates",   XSLApplyTemplates.class);
        XSLstyles.put("attribute",         XSLAttribute.class);  
        XSLstyles.put("attribute-set",     XSLAttributeSet.class);  
        XSLstyles.put("call-template",     XSLCallTemplate.class);  
        XSLstyles.put("choose",            XSLChoose.class);
        XSLstyles.put("comment",           XSLComment.class);
        XSLstyles.put("copy",              XSLCopy.class);
        XSLstyles.put("copy-of",           XSLCopyOf.class);
        XSLstyles.put("decimal-format",    XSLDecimalFormat.class);
        XSLstyles.put("element",           XSLElement.class); 
        XSLstyles.put("fallback",          XSLFallback.class);        
        XSLstyles.put("for-each",          XSLForEach.class);        
        XSLstyles.put("if",                XSLIf.class);
        XSLstyles.put("import",            XSLImport.class);
        XSLstyles.put("include",           XSLInclude.class);
        XSLstyles.put("key",               XSLKey.class);
        XSLstyles.put("message",           XSLMessage.class);
        XSLstyles.put("number",            XSLNumber.class);
        XSLstyles.put("namespace-alias",   XSLNamespaceAlias.class);
        XSLstyles.put("otherwise",         XSLOtherwise.class);
        XSLstyles.put("output",            XSLOutput.class);
        XSLstyles.put("param",             XSLParam.class);
        XSLstyles.put("preserve-space",    XSLPreserveSpace.class);
        XSLstyles.put("processing-instruction",  XSLProcessingInstruction.class);
        XSLstyles.put("sort",              XSLSort.class);     
        XSLstyles.put("strip-space",       XSLPreserveSpace.class);
        XSLstyles.put("stylesheet",        XSLStyleSheet.class);
        XSLstyles.put("template",          XSLTemplate.class);
        XSLstyles.put("text",              XSLText.class);
        XSLstyles.put("transform",         XSLStyleSheet.class);
        XSLstyles.put("value-of",          XSLValueOf.class);
        XSLstyles.put("variable",          XSLVariable.class);
        XSLstyles.put("with-param",        XSLWithParam.class);
        XSLstyles.put("when",              XSLWhen.class);

        SAXONstyles.put("assign", 	       SAXONAssign.class);
        SAXONstyles.put("doctype", 	       SAXONDoctype.class);
        SAXONstyles.put("entity-ref",      SAXONEntityRef.class);
        SAXONstyles.put("function",        SAXONFunction.class);
        SAXONstyles.put("group",           SAXONGroup.class);
        SAXONstyles.put("handler",         SAXONHandler.class);
        SAXONstyles.put("item",            SAXONItem.class);
        SAXONstyles.put("output",          SAXONOutput.class); 
        SAXONstyles.put("preview",         SAXONPreview.class); 
        SAXONstyles.put("return",          SAXONReturn.class); 
        //SAXONstyles.put("set-attribute",   SAXONSetAttribute.class); 
        SAXONstyles.put("while",           SAXONWhile.class);        
    }
    
    /**
    * Create an Element node
    * @param tag The element name
    * @param attlist the attribute list
    */

    public ElementImpl makeElementNode(
                        NodeInfo parent,
                        Name elname,
                        AttributeCollection attlist,
                        String[] namespaces,
                        int namespacesUsed,
                        Locator locator,
                        int sequence) throws SAXException
    {

        String uri = elname.getURI();
        String localname = elname.getLocalName();
        String abs = elname.getAbsoluteName();
        boolean toplevel = (parent instanceof XSLStyleSheet);

        String baseURI = null;
        int lineNumber = -1;

        if (locator!=null) {
            baseURI = locator.getSystemId();
            lineNumber = locator.getLineNumber();
        }

        Class assumedClass = LiteralResultElement.class;

        // We can't work out the final class of the node until we've examined its attributes
        // such as version and extension-element-prefixes; but we can have a good guess, and
        // change it later if need be.

        if (uri.equals(Namespace.XSLT)) {
            Class c = (Class)XSLstyles.get(localname);
            if (c!=null) {
                assumedClass = c;
            }
        }

        if (uri.equals(Namespace.SAXON)) {
            Class c = (Class)SAXONstyles.get(localname);
            if (c!=null) {
                assumedClass = c;
            }
        }

        StyleElement temp;
        try {
            temp = (StyleElement)assumedClass.newInstance();
        } catch (java.lang.InstantiationException err1) {
            throw new SAXException("Failed to create instance of " + assumedClass.getName(), err1);
        } catch (java.lang.IllegalAccessException err2) {
            throw new SAXException("Failed to access class " + assumedClass.getName(), err2);
        }

        temp.setNamespaceDeclarations(namespaces, namespacesUsed);

        try {
            temp.initialise(elname, attlist, parent, baseURI, lineNumber, sequence);
            temp.processExtensionElementAttribute();
            temp.processExcludedNamespaces();
            temp.processVersionAttribute();
        } catch (SAXException err) {
            throw temp.styleError(err);
        }

        // Now we work out what class of element we really wanted, and change it if necessary

        SAXException reason = null;
        Class actualClass = LiteralResultElement.class;

        if (uri.equals(Namespace.XSLT)) {
            actualClass = (Class)XSLstyles.get(localname);
            if (actualClass==null) {
                if (temp.forwardsCompatibleModeIsEnabled()) {
                    reason = new SAXException("Unknown XSL element");
                    actualClass = AbsentExtensionElement.class;
                } else {
                    throw temp.styleError("Unknown XSL element");
                }
            }
        } else if ((temp.isExtensionNamespace(uri) && !toplevel) ||
                (toplevel && uri.equals(Namespace.SAXON))) {
            if (uri.equals(Namespace.SAXON)) {
                actualClass = (Class)SAXONstyles.get(localname);
                if (actualClass==null) {
                    reason = temp.styleError("Unknown SAXON extension element");
                    actualClass = AbsentExtensionElement.class;
                }
            } else {
                actualClass = (Class)UserStyles.get(abs);
                if (actualClass==null) {
                    try {
                        ExtensionElementFactory factory = getFactory(uri);
                        actualClass = factory.getExtensionClass(localname);
                        UserStyles.put(abs, actualClass);             // for quicker access next time
                    } catch (SAXException err) {
                        
                        // if we can't instantiate an extension element, we don't give up
                        // immediately, because there might be an xsl:fallback defined. We
                        // create a surrogate element called AbsentExtensionElement, and
                        // save the reason for failure just in case there is no xsl:fallback
                        
                        actualClass = AbsentExtensionElement.class;
                        reason = err;
                    }                    
                }
            }
        }
        
        StyleElement node;
        if (!actualClass.equals(assumedClass)) {
            try {
                node = (StyleElement)actualClass.newInstance();
                if (reason!=null) {
                    ((AbsentExtensionElement)node).setReason(reason);
                }
            } catch (java.lang.InstantiationException err1) {
                throw new SAXException("Failed to create instance of " + actualClass.getName(), err1);
            } catch (java.lang.IllegalAccessException err2) {
                throw new SAXException("Failed to access class " + actualClass.getName(), err2);
            }
            node.substituteFor(temp);   // replace temporary node with the new one
        } else {
            node = temp;    // the original LiteralResultElement will do the job
        }

        return node;
    }

    /**
    * Get the factory class for user extension elements
    */

    private ExtensionElementFactory getFactory(String uri) throws SAXException {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash<0 || lastSlash==uri.length()-1) {
            throw new SAXException("Extension element namespace " + uri +
                " must identify an ElementExtensionFactory class");
        }
        String factoryClass = uri.substring(lastSlash+1);
        ExtensionElementFactory factory;

        try {
            factory = (ExtensionElementFactory)Loader.getInstance(factoryClass);
        } catch (ClassCastException err) {
            throw new SAXException("Class " + factoryClass + " is not an ExtensionElementFactory");
        }
        return factory;
    }

    /**
    * Method to support extension-element-available() function when used with XSL prefix
    */

    public boolean isXSLElement(String localname) {
        Class c = (Class)XSLstyles.get(localname);
        StyleElement node;
        if (c==null) return false;
        try {
            node = (StyleElement)c.newInstance();
        } catch (Throwable err1) {
            return false;
        }
        return node.isInstruction();
    }

    /**
    * Method to support extension-element-available() function when used with SAXON prefix
    */

    public boolean isSAXONElement(String localname) {
        return (SAXONstyles.get(localname)!=null);
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
