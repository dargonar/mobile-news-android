package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.Stripper;
import com.icl.saxon.tree.NodeFactory;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.style.*;
import com.icl.saxon.output.*;

import org.xml.sax.SAXException;
import org.xml.sax.*;
import org.xml.sax.helpers.ParserAdapter;
import java.util.*;
import java.io.*;

import com.icl.saxon.trax.Templates;
import com.icl.saxon.trax.Transformer;
import com.icl.saxon.trax.serialize.OutputFormat;
import com.icl.saxon.trax.serialize.QName;
import com.icl.saxon.trax.URIResolver;

/**
  * This <B>PreparedStyleSheet</B> class represents a StyleSheet that has been
  * prepared for execution (or "compiled").
  */
  
public class PreparedStyleSheet implements com.icl.saxon.trax.Templates {

    private DocumentInfo styleDoc;
    private XMLReader styleParser;
    private URIResolver uriResolver = new StandardURIResolver();

    /**
    * Default Constructor
    */

    public PreparedStyleSheet() {}

    /**
    * Set the parser to be used for parsing the stylesheet
    * @deprecated in Saxon 5.2: use setXMLReader() instead.
    */

    public void setParser(Parser parser) {
        styleParser = new ParserAdapter(parser);
    }

    /**
    * Set the parser to be used for parsing the stylesheet
    */

    public void setXMLReader(XMLReader parser) {
        styleParser = parser;
    }


    /**
    * Get the XML parser used for parsing this stylesheet. Returns null unless a parser
    * has been set using setXMLReader(),
    */

    public XMLReader getXMLReader() {
        return styleParser;
    }

    /**
    * Set the URI resolver to be used
    */

    public void setURIResolver(URIResolver resolver) {
        uriResolver = resolver;
    }

    /**
    * Get the URIResolver in use
    */

    public URIResolver getURIResolver() {
        return uriResolver;
    }

    /**
    * Prepare a stylesheet from an InputSource
    */

    public void prepare(InputSource styleSource) throws SAXException {

        Stripper styleStripper = new Stripper();
        styleStripper.setPreserveSpace(new AnyNameTest(), false);
        styleStripper.setPreserveSpace(new Name("xsl", Namespace.XSLT, "text"), true);
        
        Builder styleBuilder = new Builder();
        styleBuilder.setStripper(styleStripper);
        styleBuilder.setNodeFactory(new StyleNodeFactory());
        styleBuilder.setDiscardCommentsAndPIs(true);
        styleBuilder.setLineNumbering(true);
        
        // build the stylesheet document
   
        styleBuilder.setXMLReader(styleParser);
        DocumentInfo doc = styleBuilder.build(styleSource);
        setStyleSheetDocument(doc);

    }

    /**
    * Create a PreparedStyleSheet from a supplied DocumentInfo
    * Note: the document must have been built using the StyleNodeFactory
    */

    public void setStyleSheetDocument(DocumentInfo doc) throws SAXException {

        styleDoc = doc;

        // If top-level node is a literal result element, stitch it into a skeleton stylesheet

        StyleElement topnode = (StyleElement)styleDoc.getDocumentElement();
        if (topnode instanceof LiteralResultElement) {
            styleDoc = ((LiteralResultElement)topnode).makeStyleSheet();
        }
            
        if (!(styleDoc.getDocumentElement() instanceof XSLStyleSheet)) {
            throw new SAXException("Top-level element of stylesheet is not xsl:stylesheet or xsl:transform or literal result element");
        }

        XSLStyleSheet top = (XSLStyleSheet)styleDoc.getDocumentElement();
       
        // Preprocess the stylesheet, performing validation and preparing template definitions

        top.setPreparedStyleSheet(this);
        top.preprocess();
    }

    /**
    * <p>TRAX method: make a Transformer from this Templates object.</p>
    * <p>Equivalent to makeStyleSheetInstance()</p>
    */

    public Transformer newTransformer() {
        Controller c = new Controller();
        c.setPreparedStyleSheet(this);
        try {
            c.setOutputDetails(getOutputDetails());
        } catch (SAXException err) {}
        return c;
    }        

    /**
    * Get the root node of the stylesheet document
    */

    public DocumentInfo getStyleSheetDocument() {
        return styleDoc;
    }

    /**
    * TRAX method to get an OutputFormat object
    * @return null (currently)
    */

    /**
    * Get the properties for xsl:output.  TRAX method. The object returned will 
    * be a clone of the internal values, and thus it can be mutated 
    * without mutating the Templates object, and then handed in to 
    * the process method.
    * @return A OutputProperties object that may be mutated.
    * 
    * @see com.icl.saxon.trax.serialize.OutputFormat
    */
   
    public OutputFormat getOutputFormat() {
        OutputDetails details;
        OutputFormat format = new OutputFormat();
        try {
            details = getOutputDetails();
        } catch (SAXException err) {
            return format;
        }
        format.setMethod(details.getMethod());
        format.setVersion(details.getVersion());
        format.setIndenting(details.getIndent()!=null &&
                            details.getIndent().equals("yes"));
        format.setEncoding(details.getEncoding());
        format.setMediaType(details.getMediaType());
        format.setDoctypePublicId(details.getDoctypePublic());
        format.setDoctypeSystemId(details.getDoctypeSystem());
        format.setOmitXMLDeclaration(details.getOmitDeclaration()!=null &&
                            details.getOmitDeclaration().equals("yes"));
        // format.setStandalone(details.getStandalone());
        Vector cdata = details.getCdataElements();
        QName[] qnames = new QName[cdata.size()];
        for (int i=0; i<cdata.size(); i++) {
            qnames[i] =
                new QName(   ((Name)cdata.elementAt(i)).getURI(),
                             ((Name)cdata.elementAt(i)).getPrefix(),
                             ((Name)cdata.elementAt(i)).getLocalName() );
        }
        format.setCDataElements(qnames);
        return format;
    }

    /**
    * Determine details of the output format of this stylesheet, as defined in its
    * &lt;xsl:output&gt; element(s). This is a Saxon native method functionally equivalent
    * to the TRAX method getOutputFormat().
    * @return an OutputDetails object giving information about the output format requested
    */

    public OutputDetails getOutputDetails() throws SAXException {
        OutputDetails details = new OutputDetails();
        ((XSLStyleSheet)styleDoc.getDocumentElement()).updateOutputDetails(details);
        return details;
    }

    /**
    * Determine the media type of the output of this stylesheet. Convenience method
    * retained for compatibility: no longer necessary since getOutputDetails() is available.
    * @return the media-type defined explicitly via xsl:output, or the media-type deduced
    * from the method defined in xsl:output, or "application/xml" by default
    */

    public String getMediaType() throws SAXException {
        return getOutputDetails().getMediaType();
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
