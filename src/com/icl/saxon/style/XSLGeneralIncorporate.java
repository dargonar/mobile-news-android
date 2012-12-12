package com.icl.saxon.style;
import com.diventi.mobipaper.MobiPaperApp;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.Stripper;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.expr.*;
import com.icl.saxon.trax.URIResolver;
import org.xml.sax.*;
import org.w3c.dom.Node;
import java.io.*;
import java.net.*;


/**
* Abstract class to represent xsl:include or xsl:import element in the stylesheet.<BR>
* The xsl:include and xsl:import elements have mandatory attribute href
*/

public abstract class XSLGeneralIncorporate extends StyleElement {

    String href;
    DocumentInfo includedDoc;

    /**
    * isImport() returns true if this is an xsl:import statement rather than an xsl:include
    */

    public abstract boolean isImport() throws SAXException;

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"href"};
        allowAttributes(allowed);

        href = attributeList.getValue("href");
        if (href==null)
            reportAbsence("href");
    }

    public void validate() throws SAXException {
        // no action. The node will never be validated, because it replaces itself
        // by the contents of the included file.
    }

    public XSLStyleSheet getIncludedStyleSheet(XSLStyleSheet importer, int precedence)
                throws SAXException {

        //try {
            XSLStyleSheet thisSheet = (XSLStyleSheet)getParentNode();
            DocumentInfo thisDoc = getDocumentRoot();
            URIResolver resolver = getPreparedStyleSheet().getURIResolver();
        
            resolver.setURI(getSystemId(), href);
            InputSource incSource = resolver.getInputSource();
            XMLReader parser = resolver.getXMLReader();

            if (incSource==null) {
                Node node = resolver.getDOMNode();
                if (node==null) {
                    throw new SAXException("URI Resolver for stylesheet modules must return " +
                                            "either a SAX InputSource or a DOM Node");
                }
                parser = new DOMDriver();
                ((DOMDriver)parser).setStartNode(node);
                incSource = new InputSource();     // dummy
                incSource.setSystemId(resolver.getURI());
            }

            if (parser==null) {
                parser = getPreparedStyleSheet().getXMLReader();
            }
                
            // check for recursion
        
            XSLStyleSheet anc = thisSheet;
            while(anc!=null) {            
                if (incSource.getSystemId().equals(anc.getSystemId())) {
                    throw styleError("A stylesheet cannot " + getLocalName() + " itself");
                }
                anc = anc.getImporter();
            }

            // load the included stylesheet

            Stripper styleStripper = new Stripper();
            styleStripper.setPreserveSpace(new AnyNameTest(), false);
            styleStripper.setPreserveSpace(new Name("xsl", Namespace.XSLT, "text"), true);
        
            Builder builder = new Builder();
            builder.setStripper(styleStripper);
            builder.setNodeFactory(new StyleNodeFactory());
            builder.setDiscardCommentsAndPIs(true);
            builder.setXMLReader(parser);
            builder.setLineNumbering(true);

            
            try {
              incSource = new InputSource( MobiPaperApp.getContext().getAssets().open("functions.xsl") );
            } catch (IOException e) {
              throw styleError("no puedo abrir functions en assets");
            }
            
            includedDoc = builder.build(incSource);

            // allow the included document to use "Literal Result Element as Stylesheet" syntax

            ElementInfo outermost = (ElementInfo)includedDoc.getDocumentElement();
            if (outermost instanceof LiteralResultElement) {
                ((LiteralResultElement)outermost).makeStyleSheet();
                outermost = (ElementInfo)includedDoc.getDocumentElement();
            }
        
            if (!(outermost instanceof XSLStyleSheet)) {
                throw styleError("Included document " + href + " is not a stylesheet");
            }
            XSLStyleSheet incSheet = (XSLStyleSheet)outermost;
      
            incSheet.setPrecedence(precedence);
            incSheet.setImporter(importer);
            incSheet.spliceIncludes();          // resolve any nested includes;
                    
            return incSheet;
            
        //} catch (java.io.IOException err) {
        //    throw new SAXException(err);
        //}
    }

    public void process( Context context ) throws SAXException
    {            
        // no action. The node will never be processed, because it replaces itself
        // by the contents of the included file.
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
